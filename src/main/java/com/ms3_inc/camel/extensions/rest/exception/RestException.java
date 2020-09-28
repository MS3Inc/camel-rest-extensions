package com.ms3_inc.camel.extensions.rest.exception;

import com.ms3_inc.camel.extensions.rest.OperationResult;
import org.apache.camel.CamelException;

public class RestException extends CamelException {
	private final OperationResult result;

	public RestException(OperationResult result) {
		this.result = result;
	}

	public RestException(String message, OperationResult result) {
		super(message);
		this.result = result;
	}

	public RestException(String message, Throwable cause, OperationResult result) {
		super(message, cause);
		this.result = result;
	}

	public RestException(Throwable cause, OperationResult result) {
		super(cause);
		this.result = result;
	}

	public OperationResult getOperationResult() {
		return result;
	}
}
