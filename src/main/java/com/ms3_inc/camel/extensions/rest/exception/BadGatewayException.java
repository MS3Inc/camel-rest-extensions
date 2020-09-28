package com.ms3_inc.camel.extensions.rest.exception;

import com.ms3_inc.camel.extensions.rest.OperationResult;

public class BadGatewayException extends RestException {
	public BadGatewayException(OperationResult result) {
		super(result);
	}

	public BadGatewayException(String message, OperationResult result) {
		super(message, result);
	}

	public BadGatewayException(String message, Throwable cause, OperationResult result) {
		super(message, cause, result);
	}

	public BadGatewayException(Throwable cause, OperationResult result) {
		super(cause, result);
	}
}
