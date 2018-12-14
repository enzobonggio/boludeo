package com.example.boludeo.dao;

import com.example.boludeo.model.SimpleObject;
import reactor.core.publisher.Mono;

public interface Update {

    Mono<Void> update(Integer idx);

    Mono<Void> updateWithDepedencies(SimpleObject simpleObject, Integer idx);
}
