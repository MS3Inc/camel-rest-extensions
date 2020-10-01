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


import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.ms3_inc.camel.extensions.rest.exception.BadRequestException;
import org.apache.camel.AsyncCallback;
import org.apache.camel.Exchange;
import org.apache.camel.support.AsyncProcessorSupport;
import org.apache.camel.support.MessageHelper;
import org.openapi4j.core.exception.ResolutionException;
import org.openapi4j.core.validation.ValidationException;
import org.openapi4j.core.validation.ValidationResults;
import org.openapi4j.operation.validator.model.Request;
import org.openapi4j.operation.validator.model.impl.Body;
import org.openapi4j.operation.validator.model.impl.DefaultRequest;
import org.openapi4j.operation.validator.validation.RequestValidator;
import org.openapi4j.parser.OpenApi3Parser;
import org.openapi4j.parser.model.v3.OpenApi3;
import org.openapi4j.parser.model.v3.Server;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class OpenApi4jValidator extends AsyncProcessorSupport {
    private static final Logger LOGGER = LoggerFactory.getLogger(OpenApi4jValidator.class);
    private static final XmlMapper XML_MAPPER = new XmlMapper();
    private final RequestValidator openapi4jValidator;

    public OpenApi4jValidator(String specPath) {
        this(specPath, null);
    }

    public OpenApi4jValidator(String specPath, String basePath) {
        OpenApi3 api = null;
        try {
            api = new OpenApi3Parser()
                        .parse(new ClassPathResource(specPath).getFile(), false);
        } catch (ResolutionException|ValidationException|IOException caughtExc) {
            throw new IllegalArgumentException(caughtExc);
        }

        if (basePath != null) {
            api.setServers(new ArrayList<Server>(){
                {
                    add(0, new Server().setUrl(basePath));
                }
            });
        }

        openapi4jValidator = new RequestValidator(api);
    }

    @Override
    public boolean process(Exchange exchange, AsyncCallback callback) {
        LOGGER.debug("Trying validation");

        try {
            openapi4jValidator.validate(fromExchange(exchange));
        } catch (ValidationException e) {
            ValidationResults results = e.results();
            LOGGER.debug(e.results().toString());
            exchange.setException(new BadRequestException(fromReport(results)));
        } finally {
            LOGGER.debug("Validating complete");
            callback.done(true);
        }

        return true;
    }

    private static OperationResult fromReport(ValidationResults results) {
        List<OperationResult.Message> answer = new ArrayList<>(1);
        StringBuilder resultItems = new StringBuilder();

        // separates the results into a "Validation failed." line for details and the rest for diagnostics
        for (ValidationResults.ValidationItem result : results.items()) {
            resultItems.append(result.message() + "\n");
        }
        answer.add(new OperationResult.Message(OperationResult.Level.ERROR, null, "Validation failed.", resultItems.toString()));

        return new OperationResult(answer);
    }

    private static Request fromExchange(Exchange exchange) {
        final Request.Method method = Request.Method.valueOf(exchange.getMessage().getHeader(Exchange.HTTP_METHOD, String.class));
        final String path = exchange.getMessage().getHeader(Exchange.HTTP_URI, String.class);
        final String query = exchange.getMessage().getHeader(Exchange.HTTP_QUERY, String.class);
        final String body = MessageHelper.extractBodyAsString(exchange.getMessage());
        final String contentType = exchange.getMessage().getHeader(Exchange.CONTENT_TYPE, String.class);
        final DefaultRequest.Builder requestBuilder = new DefaultRequest.Builder(path, method);

        if (!body.isEmpty()) {
            if (contentType.equals("application/xml")) {
                JsonNode node = XML_MAPPER.createObjectNode();

                try {
                    node = XML_MAPPER.readTree(body.getBytes());
                } catch (IOException ioe) {
                    LOGGER.error(ioe.getMessage());
                }
                requestBuilder.body(Body.from(node));
            } else if (contentType.equals("application/json")) {
                requestBuilder.body(Body.from(body));
            }
        }

        for (Map.Entry<String, Object> header : exchange.getMessage().getHeaders().entrySet()) {
            if (!header.getKey().startsWith("Camel") && header.getValue() instanceof String) {
                requestBuilder.header(header.getKey(), (String) header.getValue());
            }
        }

        requestBuilder.query(query);

        return requestBuilder.build();
    }
}