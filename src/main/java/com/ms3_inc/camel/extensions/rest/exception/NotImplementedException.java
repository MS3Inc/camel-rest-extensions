package com.ms3_inc.camel.extensions.rest.exception;

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

import com.ms3_inc.camel.extensions.rest.OperationResult;

public class NotImplementedException extends RestException {
	public NotImplementedException(OperationResult result) {
		super(result);
	}

	public NotImplementedException(String message, OperationResult result) {
		super(message, result);
	}

	public NotImplementedException(String message, Throwable cause, OperationResult result) {
		super(message, cause, result);
	}

	public NotImplementedException(Throwable cause, OperationResult result) {
		super(cause, result);
	}
}
