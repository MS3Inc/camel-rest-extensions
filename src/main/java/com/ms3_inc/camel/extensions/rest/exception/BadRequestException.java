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

import java.util.Optional;

/***
 * The class is an exception with a 400 status code,
 * thrown when there is an error on the client side.
 */
public class BadRequestException extends RestException {
	/***
	 * Constructs a new bad request exception with the specified message.
	 *
	 * @param message the error message
	 */
	public BadRequestException(OperationResult.Message message) {
		super(message);
	}

	/***
	 * Constructs a new bad request exception with the specified message and the cause.
	 *
	 * @param message 	the error message
	 * @param cause 	the exception that caused the exception
	 */
	public BadRequestException(Throwable cause, OperationResult.Message message) {
		super(message, cause);
	}

	/***
	 * @return 400 wrapped in an {@link Optional}
	 */
	@Override
	public Optional<Integer> httpStatusCode() {
		return Optional.of(400);
	}
}
