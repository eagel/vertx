package org.eagel.vertx.service;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import org.eagel.vertx.api.GrpcServiceType;

import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.servicediscovery.Record;
import io.vertx.servicediscovery.ServiceDiscovery;

public class MainVerticle extends AbstractVerticle {
	private ServiceDiscovery serviceDiscovery;
	private Server server;

	@Override
	public void start(Future<Void> startFuture) throws Exception {
		server = ServerBuilder.forPort(0).addService(new HelloService()).build();
		vertx.executeBlocking((future) -> {
			try {
				server.start();
				future.complete();
			} catch (IOException e) {
				future.fail(e);
			}
		}, (rst) -> {
			if (rst.failed()) {
				startFuture.fail(rst.cause());
			} else {
				Record record = GrpcServiceType.createRecord("service", "localhost", server.getPort());
				serviceDiscovery = ServiceDiscovery.create(vertx);
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

	@Override
	public void stop(Future<Void> stopFuture) throws Exception {
		server.shutdown();
		vertx.executeBlocking((futrue) -> {
			try {
				if (server.awaitTermination(10, TimeUnit.SECONDS)) {
					futrue.complete();
				} else {
					futrue.fail("");
				}
			} catch (InterruptedException e) {
				futrue.fail(e);
			}
		}, (rst) -> {
			if (rst.failed()) {
				if (rst.cause() instanceof InterruptedException) {
					stopFuture.fail(rst.cause());
				} else {
					server.shutdownNow();
					vertx.executeBlocking((futrue) -> {
						try {
							if (server.awaitTermination(10, TimeUnit.SECONDS)) {
								futrue.complete();
							} else {
								futrue.fail("");
							}
						} catch (InterruptedException e) {
							futrue.fail(e);
						}
					}, (rsts) -> {
						if (rst.failed()) {
							stopFuture.fail(rst.cause());
						} else {
							stopFuture.complete();
						}
					});
				}
			} else {
				stopFuture.complete();
			}
		});
	}
}
