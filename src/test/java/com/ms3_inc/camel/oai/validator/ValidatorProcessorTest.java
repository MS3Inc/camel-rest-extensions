package com.ms3_inc.camel.oai.validator;

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

class ValidatorProcessorTest extends CamelTestSupport {
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
					.process(new ValidatorProcessor("api.yaml"));

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