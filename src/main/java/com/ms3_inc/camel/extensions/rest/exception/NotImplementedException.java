package com.ms3_inc.camel.extensions.rest.exception;

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
