package ru.bolodurin.bitask.client;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class ServerValueContainer {
    private volatile int lastServerValue;

}
