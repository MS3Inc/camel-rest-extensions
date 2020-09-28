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

import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.test.junit5.CamelTestSupport;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.junit.jupiter.api.Test;

class SwaggerRequestValidatorTest extends CamelTestSupport {
	@Test
	public void testValid() throws Exception {
		MockEndpoint mock = getMockEndpoint("mock:result");

		CloseableHttpClient httpClient = HttpClientBuilder.create().build();
		HttpUriRequest req = new HttpGet("http://localhost:9000/hello?bar-query=some");
		req.addHeader("foo-header", "some");

		httpClient.execute(req);

		mock.expectedMessageCount(1);
		mock.assertIsSatisfied();
	}

	@Test
	public void testInvalid() throws Exception {
		MockEndpoint mock = getMockEndpoint("mock:error");

		CloseableHttpClient httpClient = HttpClientBuilder.create().build();
		HttpPost req = new HttpPost("http://localhost:9000/greeting");
		req.setHeader("content-type", "application/json");
		req.setEntity(new StringEntity("{\"not-caller\":\"someone\"}"));

		httpClient.execute(req);

		mock.expectedMessageCount(1);
		mock.assertIsSatisfied();
	}

	@Override
	protected RouteBuilder createRouteBuilder() throws Exception {
		return new RouteBuilder() {
			public void configure() throws Exception {
				interceptFrom()
					.process(new SwaggerRequestValidator("api.yaml"));

				onException(Exception.class)
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