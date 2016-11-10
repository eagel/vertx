package org.eagel.vertx.api;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.vertx.core.json.JsonObject;

public class GrpcClient {
	private ManagedChannel channel;

	public GrpcClient(JsonObject location, JsonObject config) {
		this.channel = ManagedChannelBuilder
				.forAddress(location.getString("host", "localhost"), location.getInteger("port", 8080))
				.usePlaintext(true).build();
	}

	public ManagedChannel getChannel() {
		return channel;
	}

	public void close() {
		channel.shutdown();
	}

}
