package org.eagel.vertx.api;

import java.util.Objects;

import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonObject;
import io.vertx.servicediscovery.Record;
import io.vertx.servicediscovery.ServiceDiscovery;
import io.vertx.servicediscovery.spi.ServiceType;

public interface GrpcServiceType extends ServiceType {
	String TYPE = "grpc";

	static Record createRecord(String name, String host, int port, JsonObject metadata) {
		Objects.requireNonNull(name);
		Objects.requireNonNull(host);

		Record record = new Record().setName(name).setType(TYPE)
				.setLocation(new JsonObject().put("host", host).put("port", port));

		if (metadata != null) {
			record.setMetadata(metadata);
		}

		return record;
	}

	static Record createRecord(String name, String host, int port) {
		return createRecord(name, host, port, null);
	}

	static Record createRecord(String name, String host) {
		return createRecord(name, host, 80, null);
	}

	static void getClient(ServiceDiscovery discovery, JsonObject filter,
			Handler<AsyncResult<GrpcClient>> resultHandler) {
		discovery.getRecord(filter, ar -> {
			if (ar.failed() || ar.result() == null) {
				resultHandler.handle(Future.failedFuture("No matching record"));
			} else {
				resultHandler.handle(Future.succeededFuture(discovery.getReference(ar.result()).get()));
			}
		});
	}

}
