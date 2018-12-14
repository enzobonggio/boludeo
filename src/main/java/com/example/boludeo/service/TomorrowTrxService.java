package com.example.boludeo.service;

import com.example.boludeo.annotation.ReactiveTransactional;
import com.example.boludeo.dao.NewRequest;
import com.example.boludeo.dao.NewUpdate;
import com.example.boludeo.dao.SimpleRequest;
import com.example.boludeo.dao.SimpleUpdate;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.DefaultTransactionDefinition;
import reactor.core.publisher.Mono;

@Service
@Slf4j
public class TomorrowTrxService extends TransactionService {

    @Autowired
    public TomorrowTrxService(NewRequest request, NewUpdate update) {
        super(request, update);
    }

    @Override
    @ReactiveTransactional
    public Mono<Void> transaction(Integer idx) {
        return request.request()
                .then(update.update(idx * 2)
                        .then(request.request()
                                .flatMap(a -> update.updateWithDepedencies(a, (idx * 2) - 1))))
                .then(Mono.error(new RuntimeException()));
    }
}
