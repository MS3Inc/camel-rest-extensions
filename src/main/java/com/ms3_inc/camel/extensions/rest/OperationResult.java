package com.ms3_inc.camel.extensions.rest;

import java.util.Arrays;
import java.util.List;

public class OperationResult {
	private final List<Message> messages;

	public OperationResult(List<Message> messages) {
		this.messages = messages;
	}

	public OperationResult(Message... messages) {
		this.messages = Arrays.asList(messages);
	}

	public List<Message> getMessages() {
		return messages;
	}

	public void addMessages(Message... messages) {
		this.messages.addAll(Arrays.asList(messages));
	}

	public enum Level {
		FATAL,
		ERROR,
		WARN,
		INFO
	}

	public static class Message {
		public final Level level;
		public final String code;
		public final String details;
		public final String diagnostics;

		public Message(Level level, String code, String details, String diagnostics) {
			this.details = details;
			this.diagnostics = diagnostics;
			this.code = code;
			this.level = level;
		}
	}
}
