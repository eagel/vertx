package org.eagel.vertx.web;

import io.vertx.circuitbreaker.CircuitBreaker;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.http.HttpClient;
import io.vertx.core.http.HttpServer;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.servicediscovery.ServiceDiscovery;
import io.vertx.servicediscovery.types.HttpEndpoint;

public class MainVerticle extends AbstractVerticle {
	private HttpServer httpServer;
	private Router router;
	private ServiceDiscovery serviceDiscovery;
	private CircuitBreaker circuitBreaker;

	@Override
	public void start(Future<Void> startFuture) throws Exception {
		router = Router.router(vertx);

		router.route("/hello").handler(this::hello);

		httpServer = vertx.createHttpServer();
		httpServer.requestHandler(router::accept).listen(8080, "localhost", (listen) -> {
			if (listen.failed()) {
				startFuture.fail(listen.cause());
			} else {
				serviceDiscovery = ServiceDiscovery.create(vertx);
				circuitBreaker = CircuitBreaker.create("service", vertx);
				startFuture.complete();
			}
			;
		});
	}

	public void hello(RoutingContext context) {
		long begin = System.nanoTime();

		circuitBreaker.executeWithFallback((f) -> {
			HttpEndpoint.getClient(serviceDiscovery, new JsonObject().put("name", "service"), (getClient) -> {
				if (getClient.failed()) {
					f.fail("NO SERVICE");
				} else {
					HttpClient httpClient = getClient.result();

					JsonObject request = new JsonObject();
					request.put("message", "hello");
					httpClient.post("/hello", (response) -> {
						response.handler((handler) -> {
							System.out.println(handler.toString("UTF-8"));

							ServiceDiscovery.releaseServiceObject(serviceDiscovery, httpClient);

							f.complete("SUCCESS");
						});

					}).end(Json.encodePrettily(request));
				}
			});
		}, (v) -> {
			return "TRY LATER";
		}).setHandler((v) -> {
			context.response().end(v.succeeded() + ": " + (v.succeeded() ? v.result() : v.cause().getMessage()));
			System.out.println("spend: " + (System.nanoTime() - begin));
		});

	}
}
