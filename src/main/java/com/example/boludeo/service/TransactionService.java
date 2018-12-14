package com.example.boludeo.service;

import com.example.boludeo.dao.Request;
import com.example.boludeo.dao.Update;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

@Slf4j
public abstract class TransactionService {


    final Request request;
    final Update update;


    public TransactionService(Request request, Update update) {
        this.request = request;
        this.update = update;
    }

    public abstract Mono<Void> transaction(Integer idx);


}
