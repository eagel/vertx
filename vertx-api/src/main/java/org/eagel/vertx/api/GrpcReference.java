package org.eagel.vertx.api;

import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.servicediscovery.Record;
import io.vertx.servicediscovery.ServiceDiscovery;
import io.vertx.servicediscovery.types.AbstractServiceReference;

public class GrpcReference extends AbstractServiceReference<GrpcClient> {
	private final JsonObject location;
	private final JsonObject config;

	public GrpcReference(Vertx vertx, ServiceDiscovery discovery, Record record, JsonObject config) {
		super(vertx, discovery, record);
		this.config = config;
		this.location = record.getLocation();
	}

	@Override
	protected GrpcClient retrieve() {
		return new GrpcClient(location, config);
	}

	protected void close() {
		service.close();
	}

}
