package com.ms3_inc.camel.extensions.rest.exception;

import com.ms3_inc.camel.extensions.rest.OperationResult;

public class GatewayTimeoutException extends RestException {
	public GatewayTimeoutException(OperationResult result) {
		super(result);
	}

	public GatewayTimeoutException(String message, OperationResult result) {
		super(message, result);
	}

	public GatewayTimeoutException(String message, Throwable cause, OperationResult result) {
		super(message, cause, result);
	}

	public GatewayTimeoutException(Throwable cause, OperationResult result) {
		super(cause, result);
	}
}
