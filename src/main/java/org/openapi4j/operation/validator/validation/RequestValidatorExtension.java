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

import org.openapi4j.core.model.v3.OAI3;
import org.openapi4j.parser.model.v3.OpenApi3;
import org.openapi4j.parser.model.v3.Operation;
import org.openapi4j.parser.model.v3.Path;
import org.openapi4j.schema.validator.ValidationContext;

/**
 * Custom RequestValidator that uses OperationValidatorExtension
 */
public class RequestValidatorExtension extends RequestValidator {
    private final ValidationContext<OAI3> context;

    public RequestValidatorExtension(OpenApi3 openApi) {
        this(new ValidationContext<>(openApi.getContext()), openApi);
    }

    public RequestValidatorExtension(ValidationContext<OAI3> context, OpenApi3 openApi) {
        super(context, openApi);
        this.context = context;
    }

    @Override
    public OperationValidator getValidator(Path path, Operation operation) {
        return new OperationValidatorExtension(context, super.getValidator(path, operation));
    }
}
