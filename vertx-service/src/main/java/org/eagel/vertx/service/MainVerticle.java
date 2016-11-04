package org.eagel.vertx.service;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.http.HttpServer;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.servicediscovery.Record;
import io.vertx.servicediscovery.ServiceDiscovery;
import io.vertx.servicediscovery.types.HttpEndpoint;

public class MainVerticle extends AbstractVerticle {
	private HttpServer httpServer;
	private Router router;
	private ServiceDiscovery serviceDiscovery;

	@Override
	public void start(Future<Void> startFuture) throws Exception {
		router = Router.router(vertx);

		router.route("/hello").handler(BodyHandler.create());
		router.route("/hello").handler(this::hello);

		httpServer = vertx.createHttpServer();
		httpServer.requestHandler(router::accept).listen(0, "localhost", (listen) -> {
			if (listen.failed()) {
				startFuture.fail(listen.cause());
			} else {
				String host = "localhost";
				// TODO get the host name
				int port = listen.result().actualPort();

				// FIXME
				System.out.println(host + ":" + port);

				serviceDiscovery = ServiceDiscovery.create(vertx);
				Record record = HttpEndpoint.createRecord("service", false, host, port, "/hello", new JsonObject());

				serviceDiscovery.publish(record, (publish) -> {
					if (publish.failed()) {
						startFuture.fail(publish.cause());
					} else {
						startFuture.complete();
					}
				});
			}
		});
	}

	public void hello(RoutingContext context) {
		JsonObject request = new JsonObject(context.getBodyAsString("UTF-8"));

		// FIXME
		System.out.println(request);

		JsonObject response = new JsonObject();
		response.put("statusCode", 200);
		response.put("statusMsg", "OK");

		context.response().putHeader("Content-Type", "applicatoin/json");
		context.response().end(Json.encodePrettily(response), "UTF-8");
	}
}
