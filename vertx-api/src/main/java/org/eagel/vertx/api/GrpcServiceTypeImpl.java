package org.eagel.vertx.api;

import java.util.Objects;

import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.servicediscovery.Record;
import io.vertx.servicediscovery.ServiceDiscovery;
import io.vertx.servicediscovery.ServiceReference;

public class GrpcServiceTypeImpl implements GrpcServiceType {

	@Override
	public String name() {
		return TYPE;
	}

	@Override
	public ServiceReference get(Vertx vertx, ServiceDiscovery discovery, Record record, JsonObject configuration) {
		Objects.requireNonNull(vertx);
		Objects.requireNonNull(record);
		Objects.requireNonNull(discovery);
		return new GrpcReference(vertx, discovery, record, configuration);
	}

}
