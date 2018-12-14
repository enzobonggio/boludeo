package com.example.boludeo.dao;

import com.example.boludeo.helper.JdbcHelper;
import com.example.boludeo.model.SimpleObject;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;

@Repository
@Slf4j
public class NewUpdate implements Update {

    private final Scheduler scheduler;

    private final JdbcTemplate jdbcTemplate;

    public NewUpdate(Scheduler scheduler, JdbcTemplate jdbcTemplate) {
        this.scheduler = scheduler;
        this.jdbcTemplate = jdbcTemplate;
    }

    public Mono<Void> update(Integer idx) {
        return Mono.fromCallable(() -> {
            log.info("simpleUpdate is blocking thread {}", Thread.currentThread().getName());
            Thread.sleep(100L);
            jdbcTemplate.execute("insert into admmref.mref_carga(entidad, codigo, descripcion, descripcion_corta) values ('010', '" + idx + "', 'descripcion', 'descripcion')");
            return true;
        })
                .transform(JdbcHelper.monoPublishOn(scheduler))
                .then()
                .doOnSubscribe(s -> log.info("simpleUpdate is subscribing on {}", Thread.currentThread().getName()))
                .doOnSuccess(s -> log.info("simpleUpdate is doing success on {}", Thread.currentThread().getName()));
    }

    public Mono<Void> updateWithDepedencies(SimpleObject simpleObject, Integer idx) {
        return Mono.fromCallable(() -> {
            log.info("simpleUpdateWithDepedencies is blocking thread {}", Thread.currentThread().getName());
            Thread.sleep(100L);
            jdbcTemplate.execute("insert into admmref.mref_carga(entidad, codigo, descripcion, descripcion_corta) values ('010', '" + idx + "', 'descripcion', 'descripcion')");
            return simpleObject;
        })
                .transform(JdbcHelper.monoPublishOn(scheduler))
                .then()
                .doOnSubscribe(s -> log.info("simpleUpdateWithDepedencies is subscribing on {}", Thread.currentThread().getName()))
                .doOnSuccess(s -> log.info("simpleUpdateWithDepedencies is doing success on {}", Thread.currentThread().getName()));
    }
}
