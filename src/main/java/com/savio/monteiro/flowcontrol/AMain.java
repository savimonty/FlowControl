package com.savio.monteiro.flowcontrol;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;

import javax.annotation.PostConstruct;

@Slf4j
@SpringBootApplication
@EnableAutoConfiguration
public class AMain {

    public static MeterRegistry meterRegistry = new SimpleMeterRegistry();

    @Autowired
    private Producer producer;

    public static void main(String[] args) {
        SpringApplication.run(AMain.class, args);
    }

    @EventListener(ApplicationReadyEvent.class)
    private void onApplicationReadyEvent() {
        log.info("Spring @onApplicationReadyEvent");
        producer.start();
    }
}
