package com.example.boludeo.dao;

import com.example.boludeo.helper.JdbcHelper;
import com.example.boludeo.model.SimpleObject;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;

@Repository
@Slf4j
public class NewRequest implements Request {

    private final Scheduler scheduler;

    public NewRequest(Scheduler scheduler) {
        this.scheduler = scheduler;
    }

    public Mono<SimpleObject> request() {
        return Mono.fromCallable(() -> {
            log.info("simpleRequest is blocking thread {}", Thread.currentThread().getName());
            Thread.sleep(100L);
            return new SimpleObject();
        })
                .transform(JdbcHelper.monoPublishOn(scheduler))
                .doOnSubscribe(s -> log.info("simpleRequest is subscribing on {}", Thread.currentThread().getName()))
                .doOnSuccess(s -> log.info("simpleRequest is doing success on {}", Thread.currentThread().getName()));
    }
}
