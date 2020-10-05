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
import org.apache.camel.CamelException;

import java.util.Optional;

/***
 * Base class for all checked REST exceptions
 */
public abstract class RestException extends CamelException {
	private final OperationResult.Message message;

	/***
	 * Constructs a new REST exception with the specified message.
	 *
	 * @param message the error message
	 */
	public RestException(OperationResult.Message message) {
		super(message.toString());
		this.message = message;
	}

	/***
	 * Constructs a new REST exception with the specified message and the cause.
	 *
	 * @param message 	the error message
	 * @param cause 	the exception that caused the exception
	 */
	public RestException(OperationResult.Message message, Throwable cause) {
		super(message.toString(), cause);
		this.message = message;
	}

	/***
	 * Getter for the message.
	 *
	 * @return the {@link OperationResult.Message}
	 */
	public OperationResult.Message getOperationResultMessage() {
		return message;
	}

	/***
	 * The status code of the exception.
	 *
	 * @return the status code wrapped in an {@link Optional}
	 */
	public Optional<Integer> httpStatusCode() {
		return Optional.empty();
	}
}
