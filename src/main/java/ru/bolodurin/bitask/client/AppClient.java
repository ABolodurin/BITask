package ru.bolodurin.bitask.client;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import lombok.extern.slf4j.Slf4j;
import ru.bolodurin.bitask.AppServiceGrpc;
import ru.bolodurin.bitask.AppServiceGrpc.AppServiceBlockingStub;
import ru.bolodurin.bitask.AppServiceOuterClass.AppRequest;
import ru.bolodurin.bitask.AppServiceOuterClass.AppResponse;

import java.util.Iterator;

@Slf4j
public class AppClient {
    private static volatile int lastValue;

    public static void main(String[] args) {
        log.info("Numbers Client is starting...");

        ManagedChannel channel = getChannel();
        AppServiceBlockingStub stub = AppServiceGrpc.newBlockingStub(channel);

        AppRequest request = AppRequest
                .newBuilder()
                .setFirstValue(0)
                .setLastValue(30)
                .build();

        worker(stub.doTask(request)).start();

        doMainWork();

        channel.shutdownNow();
        log.info("Request completed");
    }

    private static void doMainWork() {
        int currentValue = 0;
        int serverValue = 0;

        for (int i = 0; i < 50; i++) {
            currentValue += 1;
            if (serverValue != lastValue) currentValue += lastValue;

            serverValue = lastValue;

            log.info("currentValue: " + currentValue);

            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private static Thread worker(Iterator<AppResponse> response) {
        Thread thread = new Thread(() -> {
            while (response.hasNext()) {
                lastValue = response.next().getServerValue();
                log.info("new value: " + lastValue);
            }
        });
        thread.setDaemon(true);

        return thread;
    }

    private static ManagedChannel getChannel() {
        return ManagedChannelBuilder
                .forTarget("localhost:8080")
                .usePlaintext()
                .build();
    }

}
