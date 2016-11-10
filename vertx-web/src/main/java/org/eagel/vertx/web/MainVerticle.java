package org.eagel.vertx.web;

import org.eagel.vertx.api.GrpcClient;
import org.eagel.vertx.api.GrpcServiceType;
import org.eagel.vertx.api.proto.HelloGrpc;
import org.eagel.vertx.api.proto.HelloGrpc.HelloStub;
import org.eagel.vertx.api.proto.HelloOuterClass.Message;

import io.grpc.stub.StreamObserver;
import io.vertx.circuitbreaker.CircuitBreaker;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.http.HttpServer;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.servicediscovery.ServiceDiscovery;

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
			GrpcServiceType.getClient(serviceDiscovery, new JsonObject().put("name", "service"), (getClient) -> {
				if (getClient.failed()) {
					f.fail("NO SERVICE");
				} else {
					GrpcClient grpcClient = getClient.result();
					HelloStub stub = HelloGrpc.newStub(grpcClient.getChannel());

					stub.hello(Message.newBuilder().setMessage("wo").build(), new StreamObserver<Message>() {

						@Override
						public void onNext(Message value) {
							System.out.println(value.getMessage());

							f.complete("SUCCESS");
						}

						@Override
						public void onError(Throwable t) {
							f.fail(t);
						}

						@Override
						public void onCompleted() {
							ServiceDiscovery.releaseServiceObject(serviceDiscovery, grpcClient);
						}
					});
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
