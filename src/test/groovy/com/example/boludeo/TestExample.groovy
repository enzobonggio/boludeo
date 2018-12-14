package com.example.boludeo

import com.example.boludeo.service.TodayTrxService
import com.example.boludeo.service.TomorrowTrxService
import com.sun.media.jfxmedia.logging.Logger
import com.zaxxer.hikari.HikariDataSource
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.test.context.ContextConfiguration
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.core.scheduler.Schedulers
import reactor.test.StepVerifier
import spock.lang.Specification

@ContextConfiguration
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class TestExample extends Specification {

    @Autowired
    TestRestTemplate testRestTemplate

    @Autowired
    TodayTrxService todayTrxService

    @Autowired
    TomorrowTrxService tomorrowTrxService

    @Autowired
    HikariDataSource hikariDataSource

    def setup() {
        hikariDataSource.getConnection().close()
    }

    def "test prueba subscriber publisher"() {
        when:
        def res = Mono.defer { Mono.just(1) }
                .doOnNext { println Thread.currentThread().getName() }
                .subscribeOn(Schedulers.newElastic("elastic1"))
                .publishOn(Schedulers.newSingle("single1"))
                .doOnNext { println Thread.currentThread().getName() }
        then:
        StepVerifier.create(res).expectNext(1).verifyComplete()
    }

    def "test trx"() {
        when:
        todayTrxService.transaction(1).block()
        then:
        true
    }
    private final static Integer COUNT = 100

    def "test trx masive"() {
        Logger.setLevel(Logger.WARNING)
        when:
        def res = Flux.range(1, COUNT).parallel().runOn(Schedulers.elastic())
                .map{testRestTemplate.getForEntity("/today/${it}", Void.class)}
        then:
        StepVerifier.create(res).expectNextCount(COUNT).verifyComplete()
    }

    def "test trx tomorrow"() {
        when:
        tomorrowTrxService.transaction(1).block()
        then:
        true
    }

    def "test trx masive tomorrow"() {
        Logger.setLevel(Logger.WARNING)
        when:
        def res = Flux.range(1, COUNT).parallel().runOn(Schedulers.elastic())
                .map{testRestTemplate.getForEntity("/tomorrow/${it}", Void.class)}
        then:
        StepVerifier.create(res).expectNextCount(COUNT).verifyComplete()
    }

}
