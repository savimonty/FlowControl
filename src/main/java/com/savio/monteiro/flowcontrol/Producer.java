package com.savio.monteiro.flowcontrol;

import io.prometheus.client.Gauge;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
public class Producer {

    /*@Autowired
    MeterRegistry meterRegistry;*/

    AtomicInteger lastProducedMessageGauge = new AtomicInteger(0);

    ConsumerService consumerService;
    public Producer(ConsumerService consumerService) {
        this.consumerService = consumerService;
        AMain.meterRegistry.gauge("last_produced_message", lastProducedMessageGauge);
    }

    public void start() {
        for(int i = 0; i < 50; i++) {
            log.info("Sending {}", i);
            lastProducedMessageGauge.set(i);
            consumerService.send("Transaction: " + i);
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
