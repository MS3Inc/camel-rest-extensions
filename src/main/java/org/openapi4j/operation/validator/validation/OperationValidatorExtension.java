package org.openapi4j.operation.validator.validation;

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
import com.ms3_inc.tavros.extensions.rest.MediaTypeUtils;
import org.openapi4j.core.model.v3.OAI3;
import org.openapi4j.core.validation.ValidationResult;
import org.openapi4j.operation.validator.model.Request;
import org.openapi4j.operation.validator.model.Response;
import org.openapi4j.operation.validator.model.impl.Body;
import org.openapi4j.parser.model.v3.Info;
import org.openapi4j.parser.model.v3.MediaType;
import org.openapi4j.parser.model.v3.OpenApi3;
import org.openapi4j.parser.model.v3.Operation;
import org.openapi4j.parser.model.v3.Path;
import org.openapi4j.schema.validator.ValidationContext;
import org.openapi4j.schema.validator.ValidationData;
import org.springframework.util.MimeType;
import org.springframework.util.MimeTypeUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

import static org.openapi4j.core.validation.ValidationSeverity.ERROR;

/**
 * <p>
 * This file is a derived work of org.openapi4j.operation.validator.validation.OperationValidator
 * from openapi4j v1.0.4. Modifications made to the original work include:
 * <li>Retain only pieces to be overriden and employ proxy pattern to original implementation</li>
 * <li>Using Spring MimeTypes and MediaTypeUtils for content type validation</li>
 * </p>
 */
public class OperationValidatorExtension extends OperationValidator {
    private final OperationValidator proxy;
    private final Operation operation;
    private final ValidationContext<OAI3> context;

    private static final ValidationResult BODY_REQUIRED_ERR = new ValidationResult(ERROR, 200, "Body is required but none provided.");
    private static final ValidationResult BODY_CONTENT_TYPE_ERR = new ValidationResult(ERROR, 202, "Body content type cannot be determined. No 'Content-Type' header available.");
    private static final ValidationResult BODY_WRONG_CONTENT_TYPE_ERR = new ValidationResult(ERROR, 203, "Content type '%s' is not allowed for body content.");

    // Map<content type, validator>
    private final Map<MimeType, BodyValidator> specRequestBodyValidators;


    private static final OpenApi3 dummyApi;
    private static final Path dummyPath;
    private static final Operation dummyOp;

    static {
        try {
            dummyOp = new Operation().setDescription("Dummy operation").setResponse("200", new org.openapi4j.parser.model.v3.Response().setDescription("dummy response"));
            dummyPath = new Path().setOperation("get", dummyOp);
            dummyApi = new OpenApi3()
                    .setOpenapi("3.0.3")
                    .setInfo(new Info().setDescription("Dummy API").setTitle("Dummy").setVersion("1.0.0"))
                    .setPath("/dummy", dummyPath);
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    public OperationValidatorExtension(ValidationContext<OAI3> context, OperationValidator validator) {
        super(dummyApi, dummyPath, dummyOp);

        this.proxy = validator;
        this.context = context;
        this.operation = proxy.getOperation();
        specRequestBodyValidators = createRequestBodyValidators();
    }

    @Override
    public Operation getOperation() {
        return proxy.getOperation();
    }

    @Override
    public Map<String, JsonNode> validatePath(Request request, ValidationData<?> validation) {
        return proxy.validatePath(request, validation);
    }

    @Override
    Map<String, JsonNode> validatePath(Request request, Pattern pathPattern, ValidationData<?> validation) {
        return proxy.validatePath(request, pathPattern, validation);
    }

    @Override
    public Map<String, JsonNode> validateQuery(Request request, ValidationData<?> validation) {
        return proxy.validateQuery(request, validation);
    }

    @Override
    public Map<String, JsonNode> validateHeaders(Request request, ValidationData<?> validation) {
        return proxy.validateHeaders(request, validation);
    }

    @Override
    public Map<String, JsonNode> validateCookies(Request request, ValidationData<?> validation) {
        return proxy.validateCookies(request, validation);
    }

    @Override
    public void validateHeaders(Response response, ValidationData<?> validation) {
        proxy.validateHeaders(response, validation);
    }

    @Override
    public void validateBody(Request request, ValidationData<?> validation) {
        if (specRequestBodyValidators == null) return;

        if (operation.getRequestBody().isRequired()) {
            if (request.getContentType() == null) {
                validation.add(BODY_CONTENT_TYPE_ERR);
                return;
            } else if (request.getBody() == null) {
                validation.add(BODY_REQUIRED_ERR);
                return;
            }
        }

        validateBodyWithContentType(
                specRequestBodyValidators,
                request.getContentType(),
                request.getBody(),
                validation);
    }


    private void validateBodyWithContentType(final Map<MimeType, BodyValidator> validators,
                                             final String rawContentType,
                                             final Body body,
                                             final ValidationData<?> validation) {

        final MimeType contentType = MimeType.valueOf(rawContentType);

        BodyValidator validator = null;
        for (Map.Entry<MimeType, BodyValidator> mediaType : validators.entrySet()) {
            if (MediaTypeUtils.includes(mediaType.getKey(), contentType)) {
                validator = mediaType.getValue();
                break;
            }
        }

        if (validator == null) {
            validation.add(BODY_WRONG_CONTENT_TYPE_ERR, rawContentType);
            return;
        }

        validator.validate(body,
                rawContentType,
                validation);
    }

    private Map<MimeType, BodyValidator> createRequestBodyValidators() {
        if (operation.getRequestBody() == null) {
            return null;
        }

        return createBodyValidators(operation.getRequestBody().getContentMediaTypes());
    }

    private Map<MimeType, BodyValidator> createBodyValidators(final Map<String, MediaType> mediaTypes) {
        final Map<MimeType, BodyValidator> validators = new HashMap<>();

        if (mediaTypes == null) {
            validators.put(MimeTypeUtils.ALL, new BodyValidator(context, null));
        } else {
            for (Map.Entry<String, MediaType> entry : mediaTypes.entrySet()) {
                validators.put(MimeType.valueOf(entry.getKey()), new BodyValidator(context, entry.getValue()));
            }
        }

        return validators;
    }
}
