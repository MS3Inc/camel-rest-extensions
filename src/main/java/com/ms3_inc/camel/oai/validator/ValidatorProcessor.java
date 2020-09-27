package com.ms3_inc.camel.oai.validator;

import com.atlassian.oai.validator.OpenApiInteractionValidator;
import com.atlassian.oai.validator.model.Request;
import com.atlassian.oai.validator.model.SimpleRequest;
import com.atlassian.oai.validator.report.SimpleValidationReportFormat;
import com.atlassian.oai.validator.report.ValidationReport;
import org.apache.camel.AsyncCallback;
import org.apache.camel.Exchange;
import org.apache.camel.support.AsyncProcessorSupport;
import org.apache.camel.support.MessageHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.MultiValueMap;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.List;
import java.util.Map;

public class ValidatorProcessor extends AsyncProcessorSupport {
    private final Logger LOGGER = LoggerFactory.getLogger(getClass());
    private final OpenApiInteractionValidator validator;

    public ValidatorProcessor(String specPath) {
        validator = OpenApiInteractionValidator
                .createFor(specPath)
                .build();
    }

    @Override
    public boolean process(Exchange exchange, AsyncCallback callback) {
        LOGGER.debug("Trying validation");

        try {
            ValidationReport report = validator.validateRequest(fromExchange(exchange));
            if (report.hasErrors()) {
                LOGGER.debug(report.toString());
                exchange.setException(new IllegalArgumentException(SimpleValidationReportFormat.getInstance().apply(report)));
            }

            LOGGER.debug("Validating complete");
        } catch (Exception ex) {
            exchange.setException(ex);
        } finally {
            callback.done(true);
        }

        return true;
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