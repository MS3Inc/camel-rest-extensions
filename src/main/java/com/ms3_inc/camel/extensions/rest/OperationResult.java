package com.ms3_inc.camel.extensions.rest;

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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class OperationResult {
	public static final String EXCHANGE_OPERATION_RESULT = "CamelxRestOperationResult";

	private final List<Message> messages = new ArrayList<>(4);

	public OperationResult() {
	}

	public OperationResult(List<Message> messages) {
		this.messages.addAll(messages);
	}

	public OperationResult(Message... messages) {
		this.messages.addAll(Arrays.asList(messages));
	}

	public List<Message> getMessages() {
		return Collections.unmodifiableList(messages);
	}

	public OperationResult addMessage(Message... messages) {
		return new OperationResult(Arrays.asList(messages));
	}

	public OperationResult mergedWith(OperationResult result) {
		return new OperationResult(result.messages);
	}

	public enum Level {
		INFO,
		WARN,
		ERROR
	}

	public static class Message {
		public final Level level;
		public final String type;
		public final String code;
		public final String details;
		public final String diagnostics;

		public Message(Level level, String type, String code, String details, String diagnostics) {
			this.level = level;
			this.type = type;
			this.code = code;
			this.details = details;
			this.diagnostics = diagnostics;
		}

		@Override
		public String toString() {
			StringBuilder sb = new StringBuilder(type)
					.append(": ").append(details).append("\n");
			if (code != null){
				sb.append(" code:").append(code);
			}
			if (diagnostics != null) {
				sb.append(" diagnostics:").append(diagnostics.replace("\n", "\n \t"));
			}

			return sb.toString();
		}
	}

	public static class MessageBuilder {
		private final Level level;
		private final String type;
		private final String details;
		private String code = null;
		private String diagnostics = null;

		private MessageBuilder(Level level, String type, String details) {
			this.level = level;
			this.type = type;
			this.details = details;
		}

		public static MessageBuilder info(String type, String details) {
			return new MessageBuilder(Level.INFO, type, details);
		}
		public static MessageBuilder warn(String type, String details) {
			return new MessageBuilder(Level.WARN, type, details);
		}
		public static MessageBuilder error(String type, String details) {
			return new MessageBuilder(Level.ERROR, type, details);
		}

		public MessageBuilder withCode(String code) {
			this.code = code;
			return this;
		}

		public MessageBuilder withDiagnostics(String diagnostics) {
			this.diagnostics = diagnostics;
			return this;
		}

		public Message build() {
			return new Message(level, type, code, details, diagnostics);
		}
	}
}
