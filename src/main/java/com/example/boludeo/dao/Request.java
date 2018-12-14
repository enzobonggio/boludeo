package com.example.boludeo.dao;

import com.example.boludeo.model.SimpleObject;
import reactor.core.publisher.Mono;

public interface Request {

    Mono<SimpleObject> request();

    default Mono<SimpleObject> requestWithScheduler() {
        throw new RuntimeException();
    }

}
