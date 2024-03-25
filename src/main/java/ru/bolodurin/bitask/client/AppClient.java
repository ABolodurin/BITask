package ru.bolodurin.bitask.client;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.stub.StreamObserver;
import lombok.extern.slf4j.Slf4j;
import ru.bolodurin.bitask.AppServiceGrpc;
import ru.bolodurin.bitask.AppServiceGrpc.AppServiceStub;
import ru.bolodurin.bitask.AppServiceOuterClass.AppRequest;
import ru.bolodurin.bitask.AppServiceOuterClass.AppResponse;

@Slf4j
public class AppClient {
    public static void main(String[] args) {
        log.info("Numbers Client is starting...");

        ManagedChannel channel = getChannel();
        AppServiceStub stub = AppServiceGrpc.newStub(channel);
        ServerValueContainer lastValue = new ServerValueContainer();

        AppRequest request = AppRequest.newBuilder()
                .setFirstValue(0)
                .setLastValue(30)
                .build();

        stub.doTask(request, observer(lastValue));

        doMainWork(lastValue);

        channel.shutdownNow();
    }

    private static StreamObserver<AppResponse> observer(ServerValueContainer valueContainer) {
        return new StreamObserver<>() {
            @Override
            public void onNext(AppResponse response) {
                int value = response.getServerValue();
                valueContainer.setLastServerValue(value);
                log.info("new value: " + value);
            }

            @Override
            public void onError(Throwable throwable) {
                log.error("Error occurred: " + throwable.getMessage());
            }

            @Override
            public void onCompleted() {
                log.info("Request completed");
            }
        };
    }

    private static void doMainWork(ServerValueContainer valueContainer) {
        int currentValue = 0;
        int serverValue = 0;

        for (int i = 0; i < 50; i++) {
            currentValue += 1;
            int readLast = valueContainer.getLastServerValue();

            if (serverValue != readLast) {
                currentValue += readLast;
            }

            serverValue = readLast;

            log.info("currentValue: " + currentValue);

            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private static ManagedChannel getChannel() {
        return ManagedChannelBuilder
                .forTarget("localhost:8080")
                .usePlaintext()
                .build();
    }

}
