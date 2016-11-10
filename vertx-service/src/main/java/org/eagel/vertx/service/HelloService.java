package org.eagel.vertx.service;

import org.eagel.vertx.api.proto.HelloGrpc.HelloImplBase;
import org.eagel.vertx.api.proto.HelloOuterClass.Message;

import io.grpc.stub.StreamObserver;

public class HelloService extends HelloImplBase {

	@Override
	public void hello(Message request, StreamObserver<Message> responseObserver) {
		responseObserver.onNext(Message.newBuilder().setMessage("hello: " + request.getMessage()).build());
		responseObserver.onCompleted();
	}
}
