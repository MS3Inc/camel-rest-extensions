package com.ms3_inc.tavros.extensions.rest.exception;

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
import com.ms3_inc.tavros.extensions.rest.OperationResult;

import java.util.Optional;

/***
 * This class is an exception thrown when the gateway or proxy server
 * didn't receive a valid response from an inbound server.
 */
public class BadGatewayException extends RestException {
	public BadGatewayException(OperationResult.Message message) {
		super(message);
	}

	public BadGatewayException(Throwable cause, OperationResult.Message message) {
		super(message, cause);
	}

	/***
	 * @return 502 wrapped in an {@link Optional}
	 */
	@Override
	public Optional<Integer> httpStatusCode() {
		return Optional.of(502);
	}
}
