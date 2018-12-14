package com.example.boludeo.service;

import com.example.boludeo.dao.SimpleRequest;
import com.example.boludeo.dao.SimpleUpdate;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.DefaultTransactionDefinition;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@Service
@Slf4j
public class TodayTrxService extends TransactionService {
    private final PlatformTransactionManager trxManager;

    @Autowired
    public TodayTrxService(SimpleRequest request, SimpleUpdate update, PlatformTransactionManager trxManager) {
        super(request, update);
        this.trxManager = trxManager;
    }

    @Override
    public Mono<Void> transaction(Integer idx) {
        return request.requestWithScheduler()
                .then(Mono.defer(() -> Mono.just(trxManager.getTransaction(new DefaultTransactionDefinition())))
                        .subscribeOn(Schedulers.single())
                        .doOnSuccess(s -> log.info("Creating trx on thread {}", Thread.currentThread().getName()))
                        .flatMap(transactionStatus -> update.update(idx * 2)
                                .then(request.request()
                                        .flatMap(e -> update.updateWithDepedencies(e, (idx * 2) - 1)))
                                .then(Mono.<Void>error(new RuntimeException()))
                                .doOnError(ex -> {
                                    log.error("Rollback on thread {}", Thread.currentThread().getName(), ex);
                                    trxManager.rollback(transactionStatus);
                                })
                                .doOnSuccess(s -> {
                                    log.info("Commit on thread {}", Thread.currentThread().getName());
                                    trxManager.commit(transactionStatus);
                                })));
    }
}
