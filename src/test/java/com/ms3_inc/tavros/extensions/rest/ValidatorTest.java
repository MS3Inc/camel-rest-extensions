package com.ms3_inc.tavros.extensions.rest;

/*-
 * Copyright 2020-2026 the original author or authors.
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
import org.apache.camel.builder.AdviceWithRouteBuilder;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.builder.AdviceWith;
import org.apache.camel.test.junit5.CamelTestSupport;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

class ValidatorTest extends CamelTestSupport {
	private static final String HELLO_ROUTE = "hello-route";
	private static final String GREETING_ROUTE = "greeting-route";

	/**
	 * Camel 4 requires the context to be stopped when adviceWith is applied. With
	 * the default (false) CamelTestSupport starts the context in setUp, the advice
	 * silently fails to take effect, and any restConfiguration change made in a
	 * test comes too late to affect the already-created rest routes. Each test now
	 * advises, then starts the context itself.
	 */
	@Override
	public boolean isUseAdviceWith() {
		return true;
	}

	@ParameterizedTest(name = "#{index} - Test with: {0}")
	@MethodSource("validatorProvider")
	public void testValidHello(String input) throws Exception {
		AdviceWith.adviceWith(HELLO_ROUTE, context, new AdviceWithRouteBuilder() {
			@Override
			public void configure() throws Exception {
				if (input.equals("openapi4j")) {
					interceptFrom()
							.process(new OpenApi4jValidator("api.yaml"))
					;
				} else {
					interceptFrom()
							.process(new SwaggerRequestValidator("api.yaml"))
					;
				}

			}
		});

		context.start();

		MockEndpoint mock = getMockEndpoint("mock:result");

		CloseableHttpClient httpClient = HttpClientBuilder.create().build();
		HttpUriRequest req = new HttpGet("http://localhost:9000/hello?bar-query=some");
		req.addHeader("foo-header", "some");

		httpClient.execute(req);

		mock.setResultWaitTime(5000);
		mock.expectedMessageCount(1);
		mock.assertIsSatisfied();
	}

	@ParameterizedTest(name = "#{index} - Test with: {0}")
	@MethodSource("validatorProvider")
	public void testInvalidHelloHeader(String input) throws Exception {
		AdviceWith.adviceWith(HELLO_ROUTE, context, new AdviceWithRouteBuilder() {
			@Override
			public void configure() throws Exception {
				if (input.equals("openapi4j")) {
					interceptFrom()
							.process(new OpenApi4jValidator("api.yaml"))
					;
				} else {
					interceptFrom()
							.process(new SwaggerRequestValidator("api.yaml"))
					;
				}
			}
		});

		context.start();

		MockEndpoint mock = getMockEndpoint("mock:error");

		CloseableHttpClient httpClient = HttpClientBuilder.create().build();
		HttpUriRequest req = new HttpGet("http://localhost:9000/hello?bar-query=some");

		httpClient.execute(req);

		mock.setResultWaitTime(5000);
		mock.expectedMessageCount(1);
		mock.assertIsSatisfied();

		String exceptionCaught = mock.getExchanges().get(0).getProperty("CamelExceptionCaught").toString();
		assertThat(exceptionCaught).contains("BadRequestException");
	}

	@ParameterizedTest(name = "#{index} - Test with: {0}")
	@MethodSource("validatorProvider")
	public void testInvalidHelloQuery(String input) throws Exception {
		AdviceWith.adviceWith(HELLO_ROUTE, context, new AdviceWithRouteBuilder() {
			@Override
			public void configure() throws Exception {
				if (input.equals("openapi4j")) {
					interceptFrom()
							.process(new OpenApi4jValidator("api.yaml"))
					;
				} else {
					interceptFrom()
							.process(new SwaggerRequestValidator("api.yaml"))
					;
				}
			}
		});

		context.start();

		MockEndpoint mock = getMockEndpoint("mock:error");

		CloseableHttpClient httpClient = HttpClientBuilder.create().build();
		HttpUriRequest req = new HttpGet("http://localhost:9000/hello");
		req.addHeader("foo-header", "some");

		httpClient.execute(req);

		mock.setResultWaitTime(5000);
		mock.expectedMessageCount(1);
		mock.assertIsSatisfied();

		String exceptionCaught = mock.getExchanges().get(0).getProperty("CamelExceptionCaught").toString();
		assertThat(exceptionCaught).contains("BadRequestException");
	}


	@ParameterizedTest(name = "#{index} - Test with: {0}")
	@MethodSource("validatorProvider")
	public void testInvalidHelloHeaderWithBasePath(String input) throws Exception {
		context.getRestConfiguration().setContextPath("/api");

		AdviceWith.adviceWith(HELLO_ROUTE, context, new AdviceWithRouteBuilder() {
			@Override
			public void configure() throws Exception {
				if (input.equals("openapi4j")) {
					interceptFrom()
							.process(new OpenApi4jValidator("api.yaml", "/api"))
					;
				} else {
					interceptFrom()
							.process(new SwaggerRequestValidator("api.yaml", "/api"))
					;
				}
			}
		});

		context.start();

		MockEndpoint mock = getMockEndpoint("mock:error");

		CloseableHttpClient httpClient = HttpClientBuilder.create().build();
		HttpUriRequest req = new HttpGet("http://localhost:9000/api/hello?bar-query=some");

		httpClient.execute(req);

		mock.setResultWaitTime(5000);
		mock.expectedMessageCount(1);
		mock.assertIsSatisfied();

		String exceptionCaught = mock.getExchanges().get(0).getProperty("CamelExceptionCaught").toString();
		assertThat(exceptionCaught).contains("BadRequestException");
	}

	@ParameterizedTest(name = "#{index} - Test with: {0}")
	@MethodSource("validatorProvider")
	public void testInvalidHelloWithBasePath(String input) throws Exception {
		context.getRestConfiguration().setContextPath("/api");

		AdviceWith.adviceWith(HELLO_ROUTE, context, new AdviceWithRouteBuilder() {
			@Override
			public void configure() throws Exception {
				if (input.equals("openapi4j")) {
					interceptFrom()
							.process(new OpenApi4jValidator("api.yaml", "/api"))
					;
				} else {
					interceptFrom()
							.process(new SwaggerRequestValidator("api.yaml", "/api"))
					;
				}
			}
		});

		context.start();

		MockEndpoint mock = getMockEndpoint("mock:error");

		CloseableHttpClient httpClient = HttpClientBuilder.create().build();
		HttpUriRequest req = new HttpGet("http://localhost:9000/api/hello");
		req.addHeader("foo-header", "some");

		httpClient.execute(req);

		mock.setResultWaitTime(5000);
		mock.expectedMessageCount(1);
		mock.assertIsSatisfied();
	}

	@ParameterizedTest(name = "#{index} - Test with: {0}")
	@MethodSource("validatorProvider")
	public void testInvalidGreetingJSON(String input) throws Exception {
		AdviceWith.adviceWith(GREETING_ROUTE, context, new AdviceWithRouteBuilder() {
			@Override
			public void configure() throws Exception {
				if (input.equals("openapi4j")) {
					interceptFrom()
							.process(new OpenApi4jValidator("api.yaml"))
					;
				} else {
					interceptFrom()
							.process(new SwaggerRequestValidator("api.yaml"))
					;
				}
			}
		});

		context.start();

		MockEndpoint mock = getMockEndpoint("mock:error");

		CloseableHttpClient httpClient = HttpClientBuilder.create().build();
		HttpPost req = new HttpPost("http://localhost:9000/greeting");
		req.setHeader("content-type", "application/json");
		req.setEntity(new StringEntity("{\"not-caller\":\"someone\"}"));

		httpClient.execute(req);

		mock.setResultWaitTime(5000);
		mock.expectedMessageCount(1);
		mock.assertIsSatisfied();

		String exceptionCaught = mock.getExchanges().get(0).getProperty("CamelExceptionCaught").toString();
		assertThat(exceptionCaught).contains("BadRequestException");
	}

	@Test
	public void testInvalidGreetingXML() throws Exception {
		AdviceWith.adviceWith(GREETING_ROUTE, context, new AdviceWithRouteBuilder() {
			@Override
			public void configure() throws Exception {
				interceptFrom()
						.process(new OpenApi4jValidator("api.yaml"))
				;
			}
		});

		context.start();

		MockEndpoint mock = getMockEndpoint("mock:error");

		CloseableHttpClient httpClient = HttpClientBuilder.create().build();
		HttpPost req = new HttpPost("http://localhost:9000/greeting");
		req.setHeader("content-type", "application/xml");
		req.setEntity(new StringEntity("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
				"<greeting>\n" +
				"\t<not-caller>someone</not-caller>\n" +
				"</greeting>"));

		httpClient.execute(req);

		mock.setResultWaitTime(5000);
		mock.expectedMessageCount(1);
		mock.assertIsSatisfied();

		String exceptionCaught = mock.getExchanges().get(0).getProperty("CamelExceptionCaught").toString();
		assertThat(exceptionCaught).contains("BadRequestException");
	}

	@Test
	public void testValidGreetingXML() throws Exception {
		AdviceWith.adviceWith(GREETING_ROUTE, context, new AdviceWithRouteBuilder() {
			@Override
			public void configure() throws Exception {
				interceptFrom()
						.process(new OpenApi4jValidator("api.yaml"))
				;
			}
		});

		context.start();

		MockEndpoint mock = getMockEndpoint("mock:result");

		CloseableHttpClient httpClient = HttpClientBuilder.create().build();
		HttpPost req = new HttpPost("http://localhost:9000/greeting");
		req.setHeader("content-type", "application/xml");
		req.setEntity(new StringEntity("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
				"<greeting>\n" +
				"\t<caller>someone</caller>\n" +
				"</greeting>"));

		httpClient.execute(req);

		mock.setResultWaitTime(5000);
		mock.expectedMessageCount(1);
		mock.assertIsSatisfied();
	}

	static Stream<String> validatorProvider() {
		return Stream.of("openapi4j", "swagger");
	}

	@Override
	protected RouteBuilder createRouteBuilder() throws Exception {
		return new RouteBuilder() {
			public void configure() throws Exception {
				onException(Exception.class)
					.log("${exchangeProperty.CamelExceptionCaught}")
					.to("mock:error");

				restConfiguration()
					.component("netty-http")
					.host("0.0.0.0")
					.port(9000);

				// Camel 4 inlines a rest verb and the direct route it targets into a
				// single route definition (restConfiguration inlineRoutes, default
				// true). Two verbs cannot share one direct consumer under inlining, and
				// the merged definition takes the id of the consumer route - which is
				// what the tests advise by.
				rest()
					.get("/hello")
						.to("direct:hello");

				rest()
					.post("/greeting")
						.to("direct:greeting");

				from("direct:hello")
					.routeId(HELLO_ROUTE)
					.log("${body}")
					.to("mock:result");

				from("direct:greeting")
					.routeId(GREETING_ROUTE)
					.log("${body}")
					.to("mock:result");
			}
		};
	}
}