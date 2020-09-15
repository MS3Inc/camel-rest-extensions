package com.ms3.camelaccountsapi;

import com.atlassian.oai.validator.OpenApiInteractionValidator;
import com.atlassian.oai.validator.model.Request;
import com.atlassian.oai.validator.model.SimpleRequest;
import com.atlassian.oai.validator.report.ValidationReport;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.Paths;
import io.swagger.v3.parser.OpenAPIV3Parser;
import io.swagger.v3.parser.core.models.ParseOptions;
import io.swagger.v3.parser.core.models.SwaggerParseResult;
import org.apache.camel.AsyncCallback;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.support.AsyncProcessorSupport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.MultiValueMap;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.*;

public class Validator extends AsyncProcessorSupport {
    private final Logger LOGGER = LoggerFactory.getLogger(this.getClass());

    private OpenApiInteractionValidator validator;
    private Paths paths;

    public Validator(String specPath) {
        validator = OpenApiInteractionValidator
                .createFor(specPath)
                .build();

        SwaggerParseResult parser = new OpenAPIV3Parser().readLocation(specPath, null, new ParseOptions());
        paths = parser.getOpenAPI().getPaths();
    }


    @Override
    public boolean process(Exchange exchange, AsyncCallback callback) {
        LOGGER.debug("Trying validation");

        String body = exchange.getIn().getBody(String.class);

        String currentMethod = exchange.getIn().getHeader(Exchange.HTTP_METHOD, String.class).toLowerCase();
        String currentPath = exchange.getIn().getHeader(Exchange.HTTP_PATH, String.class);
        String contextPath = exchange.getIn().getHeader("CamelServletContextPath", String.class);
        String contentType = exchange.getIn().getHeader(Exchange.CONTENT_TYPE, String.class);

        PathItem path = paths.get(contextPath);

        Map<String, Operation> operationCache = new HashMap<>();

        String routeId = exchange.getFromRouteId();

        Operation operation = new Operation();

        if (!operationCache.containsKey(routeId)) {
            switch (currentMethod) {
                case "patch":
                    operation = path.getPatch();
                    break;
                case "post":
                    operation = path.getPost();
                    break;
                case "put":
                    operation = path.getPut();
                    break;
                case "get":
                    operation = path.getGet();
                    break;
                case "options":
                    operation = path.getOptions();
                    break;
                case "trace":
                    operation = path.getTrace();
                    break;
                case "head":
                    operation = path.getHead();
                    break;
                case "delete":
                    operation = path.getDelete();
                    break;
                default:
            }

            operationCache.put(routeId, operation);
        } else {
            operation = operationCache.get(routeId);
        }

        SimpleRequest.Builder requestBuilder = new SimpleRequest.Builder(currentMethod, currentPath);
        Map<String, Object> headers = exchange.getIn().getHeaders();

        String queryParams = exchange.getIn().getHeader(Exchange.HTTP_QUERY, String.class);
        MultiValueMap<String, String> params = UriComponentsBuilder.fromUriString("?" + queryParams).build().getQueryParams();

        headers.forEach((key, val) -> {
            if (!key.startsWith("Camel") && !params.containsKey(key)) {
                requestBuilder.withHeader(key, (String) val);
            }
        });

        if (operation.getParameters() != null) {
            LOGGER.debug(currentMethod + " for " + contextPath + " needs parameter validation");

            if (!params.containsKey("null")) {
                params.forEach((key, valAsList) -> {
                    requestBuilder.withQueryParam(key, valAsList);
                });
            }

        }

        if (operation.getRequestBody() != null && operation.getRequestBody().getContent().size() == 1) {
            LOGGER.debug(currentMethod + " for " + contextPath + " needs body validation");

            requestBuilder
                    .withContentType(contentType)
                    .withBody(body)
            ;

        }

        Request request = requestBuilder
                .withAccept(exchange.getIn().getHeader("accept", String.class))
                .withAuthorization(exchange.getIn().getHeader("authorization", String.class))
                .build();

        ValidationReport report = validator.validateRequest(request);

        if (report.hasErrors()) {
            LOGGER.debug(report.toString());
            exchange.setException(new Exception("Validation failed"));
        } else {
            exchange.getIn().setBody(body);
        }

        LOGGER.debug("Validating complete");

        callback.done(true);
        return true;
    }
}