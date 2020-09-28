package com.ms3_inc.camel.extensions.rest.exception;

import com.ms3_inc.camel.extensions.rest.OperationResult;

public class BadRequestException extends RestException {
	public BadRequestException(OperationResult result) {
		super(result);
	}

	public BadRequestException(String message, OperationResult result) {
		super(message, result);
	}

	public BadRequestException(String message, Throwable cause, OperationResult result) {
		super(message, cause, result);
	}

	public BadRequestException(Throwable cause, OperationResult result) {
		super(cause, result);
	}
}
