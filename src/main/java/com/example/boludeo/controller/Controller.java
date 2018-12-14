package com.example.boludeo.controller;

import com.example.boludeo.service.TodayTrxService;
import com.example.boludeo.service.TomorrowTrxService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@Slf4j
public class Controller {

    private final TodayTrxService todayTrxService;
    private final TomorrowTrxService tomorrowTrxService;

    public Controller(TodayTrxService todayTrxService, TomorrowTrxService tomorrowTrxService) {
        this.todayTrxService = todayTrxService;
        this.tomorrowTrxService = tomorrowTrxService;
    }

    @GetMapping("/today/{idx}")
    public Mono<Void> today(@PathVariable("idx") Integer idx) {
        return todayTrxService.transaction(idx)
                .doOnSubscribe(s -> log.warn("Starting on thread {}", Thread.currentThread()))
                .doOnSuccess(s -> log.warn("Finishing on thread {}", Thread.currentThread()));
    }

    @GetMapping("/tomorrow/{idx}")
    public Mono<Void> tomorrow(@PathVariable("idx") Integer idx) {
        return tomorrowTrxService.transaction(idx)
                .doOnSubscribe(s -> log.warn("Starting on thread {}", Thread.currentThread()))
                .doOnSuccess(s -> log.warn("Finishing on thread {}", Thread.currentThread()));
    }
}
