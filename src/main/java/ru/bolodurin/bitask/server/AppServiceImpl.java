package ru.bolodurin.bitask.server;

import io.grpc.stub.StreamObserver;
import ru.bolodurin.bitask.AppServiceGrpc.AppServiceImplBase;
import ru.bolodurin.bitask.AppServiceOuterClass.AppRequest;
import ru.bolodurin.bitask.AppServiceOuterClass.AppResponse;


public class AppServiceImpl extends AppServiceImplBase {
    @Override
    public void doTask(AppRequest request, StreamObserver<AppResponse> responseObserver) {
        if (request.getFirstValue() < 0 || request.getLastValue() < request.getFirstValue())
            throw new RuntimeException("Service error");

        int firstValue = request.getFirstValue();

        for (int i = firstValue; i <= request.getLastValue(); i++) {
            AppResponse response = AppResponse
                    .newBuilder()
                    .setServerValue(firstValue + i)
                    .build();

            responseObserver.onNext(response);

            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                responseObserver.onCompleted();
            }
        }

        responseObserver.onCompleted();
    }
}
