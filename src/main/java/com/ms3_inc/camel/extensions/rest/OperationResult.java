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

/***
 * The {@code OperationResult} class contains a {@link Message} class and a {@link MessageBuilder}
 * class. The {@code MessageBuilder} class uses the builder pattern to build the error information.
 * <p>
 * The {@code Message} instance is used by the REST exceptions.
 */
public class OperationResult {
	public static final String EXCHANGE_OPERATION_RESULT = "CamelxRestOperationResult";

	private final List<Message> messages = new ArrayList<>(4);

	/***
	 * Constructs an operation result with no {@link Message}s.
	 */
	public OperationResult() {
	}

	/***
	 * Constructs an operation result with {@code Message}s wrapped in a {@code List}.
	 *
	 * @param messages the {@code Message}s wrapped in a {@code List}
	 */
	public OperationResult(List<Message> messages) {
		this.messages.addAll(messages);
	}

	/***
	 * Constructs an operation result with any number of {@code Message}s.
	 *
	 * @param messages the {@code Message}s as varargs
	 */
	public OperationResult(Message... messages) {
		this.messages.addAll(Arrays.asList(messages));
	}

	/***
	 * Getter that returns the {@code Message}s.
	 *
	 * @return {@code Message}s wrapped in a {@code List}
	 */
	public List<Message> getMessages() {
		return Collections.unmodifiableList(messages);
	}

	/***
	 * Adds a {@code Message} to the {@code OperationResult}.
	 *
	 * @param messages {@code Message}s as varargs
	 * @return the {@code OperationResult} instance
	 */
	public OperationResult addMessage(Message... messages) {
		return new OperationResult(Arrays.asList(messages));
	}

	/***
	 * Merge {@code Message}s of {@code OperationResult} into an {@code OperationResult} instance.
	 *
	 * @param result the {@code OperationResult} to merge with
	 * @return the {@code OperationResult} instance
	 */
	public OperationResult mergedWith(OperationResult result) {
		return new OperationResult(result.messages);
	}

	/***
	 * Levels that an error can have.
	 */
	public enum Level {
		/***
		 * Info level
		 */
		INFO,
		/***
		 * Warn level
		 */
		WARN,
		/***
		 * Error level
		 */
		ERROR
	}

	/***
	 * This class specifies the values of a {@code Message}, the level, the type,
	 * the code, the details, and the diagnostics.
	 */
	public static class Message {
		public final Level level;
		public final String type;
		public final String code;
		public final String details;
		public final String diagnostics;

		/***
		 * Constructs the error {@code Message} with the specified parameters.
		 *
		 * @param level 		{@link Level}
		 * @param type 			the type
		 * @param code 			the code
		 * @param details		details
		 * @param diagnostics	diagnostics
		 */
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

	/***
	 * This class uses the builder pattern to build the message.
	 */
	public static class MessageBuilder {
		private final Level level;
		private final String type;
		private final String details;
		private String code = null;
		private String diagnostics = null;

		/***
		 * Constructs the message builder with the specified level, type of message,
		 * and details of the the error.
		 *
		 * @param level 	{@link Level}
		 * @param type		type of message
		 * @param details	details of the error
		 */
		private MessageBuilder(Level level, String type, String details) {
			this.level = level;
			this.type = type;
			this.details = details;
		}

		/***
		 * Uses the {@code MessageBuilder} constructor to construct a {@code MessageBuilder} with INFO level.
		 *
		 * @param type		type of message
		 * @param details	details of error
		 * @return  the created instance {@code MessageBuilder}
		 */
		public static MessageBuilder info(String type, String details) {
			return new MessageBuilder(Level.INFO, type, details);
		}

		/***
		 * Uses the {@code MessageBuilder} constructor to construct a {@code MessageBuilder} with a WARN level.
		 *
		 * @param type		type of message
		 * @param details	details of error
		 * @return  		the created instance {@code MessageBuilder}
		 */
		public static MessageBuilder warn(String type, String details) {
			return new MessageBuilder(Level.WARN, type, details);
		}

		/***
		 * Uses the {@code MessageBuilder} constructor to construct a {@code MessageBuilder} with an ERROR level.
		 *
		 * @param type		type of message
		 * @param details	details of error
		 * @return  		the created {@code MessageBuilder} instance
		 */
		public static MessageBuilder error(String type, String details) {
			return new MessageBuilder(Level.ERROR, type, details);
		}

		/***
		 * Adds the code to the instance.
		 *
		 * @param code the error code
		 * @return this {@code MessageBuilder} instance
		 */
		public MessageBuilder withCode(String code) {
			this.code = code;
			return this;
		}

		/***
		 * Adds the information used for diagnosing the error to the instance.
		 *
		 * @param diagnostics the details used for diagnosing of the error
		 * @return this {@code MessageBuilder} instance
		 */
		public MessageBuilder withDiagnostics(String diagnostics) {
			this.diagnostics = diagnostics;
			return this;
		}

		/***
		 * Build the {@code Message} instance with the collected values.
		 *
		 * @return the created {@code Message} instance
		 */
		public Message build() {
			return new Message(level, type, code, details, diagnostics);
		}
	}
}
