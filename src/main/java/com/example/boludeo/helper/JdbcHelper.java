package com.example.boludeo.helper;

import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;

import java.util.function.Function;

@UtilityClass
@Slf4j
public class JdbcHelper {

    public <T> Function<Flux<T>, Flux<T>> fluxPublishOn(Scheduler scheduler) {
        return flux -> Mono.subscriberContext()
                .map(context -> context.getOrDefault("TRANSACTION", false))
                .flatMapMany(transaction -> transaction ?
                        flux :
                        flux.subscribeOn(scheduler).publishOn(Schedulers.single()));
    }

    public <T> Function<Mono<T>, Mono<T>> monoPublishOn(Scheduler scheduler) {
        return mono -> Mono.subscriberContext()
                .map(context -> context.getOrDefault("TRANSACTION", false))
                .flatMap(activeTrx -> activeTrx ?
                        mono :
                        mono.subscribeOn(scheduler).publishOn(Schedulers.single()));
    }
}
