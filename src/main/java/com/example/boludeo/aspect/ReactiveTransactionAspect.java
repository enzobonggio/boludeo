package com.example.boludeo.aspect;

import lombok.val;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.reactivestreams.Publisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

@Aspect
@Component
public class ReactiveTransactionAspect {

    private final Map<Class<? extends Publisher>, Transformation> transformationStrategy;
    private final PlatformTransactionManager trxManager;

    private final Scheduler transactionScheduler;

    public ReactiveTransactionAspect(PlatformTransactionManager trxManager, @Qualifier("trx-scheduler") Scheduler transactionScheduler) {
        this.trxManager = trxManager;
        this.transactionScheduler = transactionScheduler;
        transformationStrategy = new HashMap<>();
        transformationStrategy.put(Mono.class, this::transformMono);
        transformationStrategy.put(Flux.class, this::transformFlux);
    }

    @SuppressWarnings("unchecked")
    @Around("@annotation(com.example.boludeo.annotation.ReactiveTransactional)")
    public <T> Publisher<T> reactiveTransaccion(ProceedingJoinPoint joinPoint) throws Throwable {
        Logger log = LoggerFactory.getLogger(joinPoint.getSignature().getDeclaringType());
        Signature signature = joinPoint.getSignature();
        MethodSignature methodSignature = (MethodSignature) signature;
        Class<? extends Publisher> returningType = methodSignature.getReturnType();
        val fullMonad = (Publisher<T>) joinPoint.proceed();
        return transformationStrategy.get(returningType).transform(fullMonad, log);
    }

    private <T> Publisher<T> transformMono(Publisher<T> mono, Logger log) {
        return Mono.defer(() -> Mono.just(trxManager.getTransaction(new DefaultTransactionDefinition())))
                .subscribeOn(transactionScheduler)
                .flatMap(transactionStatus -> ((Mono<T>) mono)
                        .subscriberContext(context -> context.put("TRANSACTION", true))
                        .doOnSuccess(handleSuccess(log, transactionStatus))
                        .doOnError(handleException(log, transactionStatus)));
    }

    private <T> Publisher<T> transformFlux(Publisher<T> flux, Logger log) {
        return Mono.defer(() -> Mono.just(trxManager.getTransaction(new DefaultTransactionDefinition())))
                .flatMapMany(transactionStatus -> ((Flux<T>) flux)
                        .subscriberContext(context -> context.put("TRANSACTION", true))
                        .doOnComplete(handleComplete(log, transactionStatus))
                        .doOnError(handleException(log, transactionStatus)));
    }

    private Runnable handleComplete(Logger log, TransactionStatus transactionStatus) {
        log.info("Commit on thread {}", Thread.currentThread().getName());
        return () -> trxManager.commit(transactionStatus);
    }

    public interface Transformation {
        Publisher transform(Publisher publisher, Logger log);
    }

    private <T> Consumer<T> handleSuccess(Logger log, TransactionStatus transactionStatus) {
        return s -> {
            log.info("Commit on thread {}", Thread.currentThread().getName());
            trxManager.commit(transactionStatus);
        };
    }

    private Consumer<Throwable> handleException(Logger log, TransactionStatus transactionStatus) {
        return ex -> {
            log.error("Rollback on thread {}", Thread.currentThread().getName());
            trxManager.rollback(transactionStatus);
        };
    }
}
