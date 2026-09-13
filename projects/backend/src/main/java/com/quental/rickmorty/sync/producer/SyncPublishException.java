package com.quental.rickmorty.sync.producer;

public class SyncPublishException extends RuntimeException {

    public SyncPublishException(String message, Throwable cause) {
        super(message, cause);
    }
}
