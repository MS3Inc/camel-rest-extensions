package com.ms3_inc.camel.extensions.rest;

/*-
 * Copyright 2019-2020 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import com.atlassian.oai.validator.OpenApiInteractionValidator;
import com.atlassian.oai.validator.model.Request;
import com.atlassian.oai.validator.model.SimpleRequest;
import com.atlassian.oai.validator.report.SimpleValidationReportFormat;
import com.atlassian.oai.validator.report.ValidationReport;
import com.ms3_inc.camel.extensions.rest.exception.BadRequestException;
import org.apache.camel.AsyncCallback;
import org.apache.camel.CamelException;
import org.apache.camel.Exchange;
import org.apache.camel.support.AsyncProcessorSupport;
import org.apache.camel.support.MessageHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.MultiValueMap;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class SwaggerRequestValidator extends AsyncProcessorSupport {
    private final Logger LOGGER = LoggerFactory.getLogger(getClass());
    private final OpenApiInteractionValidator validator;

    public SwaggerRequestValidator(String specPath) {
        validator = OpenApiInteractionValidator
                .createFor(specPath)
                .build();
    }

    public SwaggerRequestValidator(String specPath, String basePath) {
        validator = OpenApiInteractionValidator
                .createFor(specPath)
                .withBasePathOverride(basePath)
                .build();
    }

    @Override
    public boolean process(Exchange exchange, AsyncCallback callback) {
        LOGGER.debug("Trying validation");

        try {
            ValidationReport report = validator.validateRequest(fromExchange(exchange));
            if (report.hasErrors()) {
                LOGGER.debug(report.toString());
                exchange.setException(new BadRequestException(fromReport(report)));
            }

            LOGGER.debug("Validating complete");
        } catch (Exception ex) {
            exchange.setException(new CamelException(ex));
        } finally {
            callback.done(true);
        }

        return true;
    }

    private static OperationResult fromReport(ValidationReport report) {
        List<OperationResult.Message> answer = new ArrayList<>(1);

        // separates the report into a "Validation failed." line for details and the rest for diagnostics
        String[] result = SimpleValidationReportFormat.getInstance().apply(report).split(System.lineSeparator(), 2);
        answer.add(new OperationResult.Message(OperationResult.Level.ERROR, null, result[0], result[1]));

        return new OperationResult(answer);
    }

    private static Request fromExchange(Exchange exchange) {
        final Request.Method method = Request.Method.valueOf(exchange.getMessage().getHeader(Exchange.HTTP_METHOD, String.class));
        final String path = exchange.getMessage().getHeader(Exchange.HTTP_URI, String.class);
        final String query = exchange.getMessage().getHeader(Exchange.HTTP_QUERY, String.class);
        final String body = MessageHelper.extractBodyAsString(exchange.getMessage());
        final SimpleRequest.Builder requestBuilder = new SimpleRequest.Builder(method, path);

        if (!body.isEmpty()) {
            requestBuilder.withBody(body);
        }

        for (Map.Entry<String, Object> header : exchange.getMessage().getHeaders().entrySet()) {
            if (!header.getKey().startsWith("Camel") && header.getValue() instanceof String) {
                requestBuilder.withHeader(header.getKey(), (String) header.getValue());
            }
        }

        MultiValueMap<String, String> queryParams = UriComponentsBuilder.newInstance()
                .query(query)
                .build()
                .getQueryParams();
        for (Map.Entry<String, List<String>> queryParamEntry : queryParams.entrySet()) {
            requestBuilder.withQueryParam(queryParamEntry.getKey(), queryParamEntry.getValue());
        }

        return requestBuilder.build();
    }
}