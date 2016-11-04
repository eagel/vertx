package org.eagel.vertx.web;

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
				startFuture.complete();
			}
			;
		});
	}

	public void hello(RoutingContext context) {
		HttpEndpoint.getClient(serviceDiscovery, new JsonObject().put("name", "service"), (getClient) -> {
			if (getClient.failed()) {
				context.response().setStatusCode(500);
				context.response().setStatusMessage("Error");
				context.response().end("Error Happend");
			} else {
				HttpClient httpClient = getClient.result();

				JsonObject request = new JsonObject();
				request.put("message", "hello");

				httpClient.post("", (response) -> {
					response.handler((handler) -> {
						System.out.println(handler.toString("UTF-8"));

						context.response().end("success");

						ServiceDiscovery.releaseServiceObject(serviceDiscovery, httpClient);
					});

				}).end(Json.encodePrettily(request));
			}
		});
	}
}
