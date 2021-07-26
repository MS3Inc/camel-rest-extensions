package com.ms3_inc.tavros.extensions.rest;

/*-
 * Copyright 2020-2021 the original author or authors.
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
import com.ms3_inc.tavros.extensions.rest.exception.BadRequestException;
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
import org.openapi4j.operation.validator.validation.RequestValidatorExtension;
import org.openapi4j.parser.OpenApi3Parser;
import org.openapi4j.parser.model.v3.OpenApi3;
import org.openapi4j.parser.model.v3.Server;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;
import java.util.Collections;
import java.util.Map;

/**
 * This {@code OpenApi4jValidator} class uses the openapi4j library for validation.
 * This library supports both JSON and XML validation.
 * <p>
 * This class provides two constructors for creating the validator,
 * one that sets the base path used in the route,
 * and one without a base path.
 */
public class OpenApi4jValidator extends AsyncProcessorSupport {
    private static final Logger LOGGER = LoggerFactory.getLogger(OpenApi4jValidator.class);
    private static final XmlMapper XML_MAPPER = new XmlMapper();
    private final RequestValidator openapi4jValidator;
    private final static boolean mediaTypeParamSupport = Boolean.parseBoolean(
            System.getProperty("camelx.rest.ff.mediaparams",
            System.getenv().getOrDefault("CAMELX_REST_FF_MEDIAPARAMS", "false")));

    /**
     * Constructs the validator using an {@link OpenApi3} instance. The {@link OpenApi3Parser} parses the provided specification,
     * and creates a {@link RequestValidator} instance based on the {@code OpenApi3} instance.
     *
     * @param specPath location of specification in resources
     * @throws IllegalArgumentException if the url or file can’t be read or if there is an error with the spec
     */
    public OpenApi4jValidator(String specPath) {
        this(specPath, null);
    }

    /**
     * Constructs the validator with a base path using an {@link OpenApi3} instance. The {@link OpenApi3Parser} parses the provided specification.
     * The base path is added to the object and a {@link RequestValidator} instance is created based on the {@code OpenApi3} instance.
     *
     * @param specPath location of specification in resources
     * @param basePath context path of api
     * @throws IllegalArgumentException if the url or file can’t be read or if there is an error with the spec
     */
    public OpenApi4jValidator(String specPath, String basePath) {
        OpenApi3 api;
        try {
            api = new OpenApi3Parser().parse(new ClassPathResource(specPath).getFile(), false);
        } catch (ResolutionException | ValidationException | IOException caughtExc) {
            throw new IllegalArgumentException(caughtExc);
        }

        if (basePath != null) {
            api.setServers(Collections.singletonList(new Server().setUrl(basePath)));
        }

        if (mediaTypeParamSupport)
            openapi4jValidator = new RequestValidatorExtension(api);
        else
            openapi4jValidator = new RequestValidator(api);
    }

    @Override
    public boolean process(Exchange exchange, AsyncCallback callback) {
        LOGGER.debug("Trying validation");

        try {
            openapi4jValidator.validate(requestFrom(exchange));
        } catch (ValidationException e) {
            exchange.setException(new BadRequestException(messageFrom(e)));
        } finally {
            LOGGER.debug("Validating complete");
            callback.done(true);
        }

        return true;
    }

    /**
     * Parses the {@code ValidationResults} report and returns the details and diagnostics
     * of the bad request.
     *
     * @param exception    the ValidationException thrown by the validator
     * @return the {@code Message} with all of the information about the error
     */
    private static OperationResult.Message messageFrom(ValidationException exception) {
        ValidationResults results = exception.results();

        if (results == null) {
            return OperationResult.MessageBuilder.error("RequestValidationError", exception.getMessage())
                    .build();
        }

        StringBuilder diagnostics = new StringBuilder();
        LOGGER.debug(exception.results().toString());
        for (ValidationResults.ValidationItem result : results.items()) {
            diagnostics.append(result.toString()).append(System.lineSeparator());
        }

        return OperationResult.MessageBuilder.error("RequestValidationError", "HTTP request failed API specification validation.")
            .withDiagnostics(diagnostics.toString())
            .build();
    }

    /**
     * Builds the request from the {@code Exchange} using the builder pattern.
     * First initializes the needed headers and values from the exchange.
     * If the body is not empty, it gets added to the request. If the content type is XML,
     * it's converted to a {@link JsonNode}.
     * Adds the query params and the necessary headers to the request.
     *
     * @param exchange the entire {@code Exchange} object of the request
     * @return the {@code Request} built from the necessary exchange values
     */
    private static Request requestFrom(Exchange exchange) {
        final Request.Method method = Request.Method.valueOf(exchange.getMessage().getHeader(Exchange.HTTP_METHOD, String.class));
        final String path = exchange.getMessage().getHeader(Exchange.HTTP_URI, String.class);
        final String query = exchange.getMessage().getHeader(Exchange.HTTP_QUERY, String.class);
        final String body = MessageHelper.extractBodyAsString(exchange.getMessage());
        final String contentType = exchange.getMessage().getHeader(Exchange.CONTENT_TYPE, String.class);
        final DefaultRequest.Builder requestBuilder = new DefaultRequest.Builder(path, method);

        if (body != null && !body.isEmpty()) {
            if (contentType != null && contentType.endsWith("xml")) {
                try {
                    JsonNode node = XML_MAPPER.readTree(body);
                    requestBuilder.body(Body.from(node));
                } catch (IOException ioe) {
                    throw new RuntimeException(ioe);
                }
            } else {
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
