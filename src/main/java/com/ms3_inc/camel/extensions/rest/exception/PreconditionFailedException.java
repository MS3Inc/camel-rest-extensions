package com.ms3_inc.camel.extensions.rest.exception;

import com.ms3_inc.camel.extensions.rest.OperationResult;

public class PreconditionFailedException extends RestException {
	public PreconditionFailedException(OperationResult result) {
		super(result);
	}

	public PreconditionFailedException(String message, OperationResult result) {
		super(message, result);
	}

	public PreconditionFailedException(String message, Throwable cause, OperationResult result) {
		super(message, cause, result);
	}

	public PreconditionFailedException(Throwable cause, OperationResult result) {
		super(cause, result);
	}
}
