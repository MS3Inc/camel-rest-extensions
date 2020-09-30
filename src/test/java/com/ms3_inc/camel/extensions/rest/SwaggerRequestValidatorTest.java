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

import org.apache.camel.builder.AdviceWithRouteBuilder;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.reifier.RouteReifier;
import org.apache.camel.test.junit5.CamelTestSupport;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class SwaggerRequestValidatorTest extends CamelTestSupport {
	private final String badRequestException = "com.ms3_inc.camel.extensions.rest.exception.BadRequestException";
	@Test
	public void testValidHello() throws Exception {
		RouteReifier.adviceWith(context.getRouteDefinitions().get(0), context, new AdviceWithRouteBuilder() {
			@Override
			public void configure() throws Exception {
				interceptFrom()
						.process(new SwaggerRequestValidator("api.yaml"))
				;
			}
		});

		MockEndpoint mock = getMockEndpoint("mock:result");

		CloseableHttpClient httpClient = HttpClientBuilder.create().build();
		HttpUriRequest req = new HttpGet("http://localhost:9000/hello?bar-query=some");
		req.addHeader("foo-header", "some");

		httpClient.execute(req);

		mock.expectedMessageCount(1);
		mock.assertIsSatisfied();
	}

	@Test
	public void testInvalidHelloHeader() throws Exception {
		RouteReifier.adviceWith(context.getRouteDefinitions().get(0), context, new AdviceWithRouteBuilder() {
			@Override
			public void configure() throws Exception {
				interceptFrom()
						.process(new SwaggerRequestValidator("api.yaml"))
				;
			}
		});

		MockEndpoint mock = getMockEndpoint("mock:error");

		CloseableHttpClient httpClient = HttpClientBuilder.create().build();
		HttpUriRequest req = new HttpGet("http://localhost:9000/hello?bar-query=some");

		httpClient.execute(req);

		mock.expectedMessageCount(1);
		mock.assertIsSatisfied();

		String exceptionCaught = mock.getExchanges().get(0).getProperty("CamelExceptionCaught").toString();
		assertThat(exceptionCaught).isEqualTo(badRequestException);
	}

	@Test
	public void testInvalidHelloQuery() throws Exception {
		RouteReifier.adviceWith(context.getRouteDefinitions().get(0), context, new AdviceWithRouteBuilder() {
			@Override
			public void configure() throws Exception {
				interceptFrom()
						.process(new SwaggerRequestValidator("api.yaml"))
				;
			}
		});

		MockEndpoint mock = getMockEndpoint("mock:error");

		CloseableHttpClient httpClient = HttpClientBuilder.create().build();
		HttpUriRequest req = new HttpGet("http://localhost:9000/hello");
		req.addHeader("foo-header", "some");

		httpClient.execute(req);

		mock.expectedMessageCount(1);
		mock.assertIsSatisfied();

		String exceptionCaught = mock.getExchanges().get(0).getProperty("CamelExceptionCaught").toString();
		assertThat(exceptionCaught).isEqualTo(badRequestException);
	}

	@Test
	public void testValidHelloWithBasePath() throws Exception {
		context.getRestConfiguration().setContextPath("/api");

		RouteReifier.adviceWith(context.getRouteDefinitions().get(1), context, new AdviceWithRouteBuilder() {
			@Override
			public void configure() throws Exception {
				interceptFrom()
						.process(new SwaggerRequestValidator("api.yaml", "/api"))
				;
			}
		});

		MockEndpoint mock = getMockEndpoint("mock:result");

		CloseableHttpClient httpClient = HttpClientBuilder.create().build();
		HttpUriRequest req = new HttpGet("http://localhost:9000/api/hello?bar-query=some");
		req.addHeader("foo-header", "some");

		httpClient.execute(req);

		mock.expectedMessageCount(1);
		mock.assertIsSatisfied();
	}

	@Test
	public void testInvalidHelloWithBasePath() throws Exception {
		context.getRestConfiguration().setContextPath("/api");

		RouteReifier.adviceWith(context.getRouteDefinitions().get(1), context, new AdviceWithRouteBuilder() {
			@Override
			public void configure() throws Exception {
				interceptFrom()
						.process(new SwaggerRequestValidator("api.yaml", "/api"))
				;
			}
		});

		MockEndpoint mock = getMockEndpoint("mock:result");

		CloseableHttpClient httpClient = HttpClientBuilder.create().build();
		HttpUriRequest req = new HttpGet("http://localhost:9000/api/hello?bar-query=some");
		req.addHeader("foo-header", "some");

		httpClient.execute(req);

		mock.expectedMessageCount(1);
		mock.assertIsSatisfied();
	}

	@Test
	public void testInvalidGreetingJSON() throws Exception {
		RouteReifier.adviceWith(context.getRouteDefinitions().get(0), context, new AdviceWithRouteBuilder() {
			@Override
			public void configure() throws Exception {
				interceptFrom()
						.process(new SwaggerRequestValidator("api.yaml"))
				;
			}
		});

		MockEndpoint mock = getMockEndpoint("mock:error");

		CloseableHttpClient httpClient = HttpClientBuilder.create().build();
		HttpPost req = new HttpPost("http://localhost:9000/greeting");
		req.setHeader("content-type", "application/json");
		req.setEntity(new StringEntity("{\"not-caller\":\"someone\"}"));

		httpClient.execute(req);

		mock.expectedMessageCount(1);
		mock.assertIsSatisfied();

		String exceptionCaught = mock.getExchanges().get(0).getProperty("CamelExceptionCaught").toString();
		assertThat(exceptionCaught).isEqualTo(badRequestException);
	}

	@Test
	public void testInvalidGreetingXML() throws Exception {
		RouteReifier.adviceWith(context.getRouteDefinitions().get(2), context, new AdviceWithRouteBuilder() {
			@Override
			public void configure() throws Exception {
				interceptFrom()
						.process(new SwaggerRequestValidator("api.yaml"))
				;
			}
		});

		MockEndpoint mock = getMockEndpoint("mock:error");

		CloseableHttpClient httpClient = HttpClientBuilder.create().build();
		HttpPost req = new HttpPost("http://localhost:9000/greeting");
		req.setHeader("content-type", "application/xml");
		req.setEntity(new StringEntity("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
				"<greeting>\n" +
				"\t<not-caller>someone</not-caller>\n" +
				"</greeting>"));

		httpClient.execute(req);

		mock.expectedMessageCount(1);
		mock.assertIsSatisfied();

		String exceptionCaught = mock.getExchanges().get(0).getProperty("CamelExceptionCaught").toString();
		assertThat(exceptionCaught).isEqualTo(badRequestException);
	}

	@Test
	public void testValidGreetingXML() throws Exception {
		RouteReifier.adviceWith(context.getRouteDefinitions().get(2), context, new AdviceWithRouteBuilder() {
			@Override
			public void configure() throws Exception {
				interceptFrom()
						.process(new SwaggerRequestValidator("api.yaml"))
				;
			}
		});

		MockEndpoint mock = getMockEndpoint("mock:result");

		CloseableHttpClient httpClient = HttpClientBuilder.create().build();
		HttpPost req = new HttpPost("http://localhost:9000/greeting");
		req.setHeader("content-type", "application/xml");
		req.setEntity(new StringEntity("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
				"<greeting>\n" +
				"\t<caller>someone</caller>\n" +
				"</greeting>"));

		httpClient.execute(req);

		mock.expectedMessageCount(1);
		mock.assertIsSatisfied();
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

				rest()
					.get("/hello")
						.to("direct:test")
					.post("/greeting")
						.to("direct:test");

				from("direct:test")
					.log("${body}")
					.to("mock:result");
			}
		};
	}
}