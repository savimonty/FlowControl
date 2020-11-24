package com.savio.monteiro.flowcontrol.config;

import com.savio.monteiro.flowcontrol.Consumer;
import com.savio.monteiro.flowcontrol.ConsumerBridge;
import com.savio.monteiro.flowcontrol.ConsumerService;
import com.savio.monteiro.flowcontrol.Producer;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

@Slf4j
@Configuration
public class AConfig {

    @Autowired
    @Qualifier("flowControlledBridge")
    ConsumerService flowControlledBridge;


    @Bean("meterRegistry")
    public MeterRegistry initMeterRegistry() {
        log.info("Initialized Meter Registry");
        return new SimpleMeterRegistry();
    }

    @Bean
    @Primary
    @DependsOn({"meterRegistry"})
    public Producer newProducer() {
        return new Producer(flowControlledBridge);
    }
}
