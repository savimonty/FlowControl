package com.savio.monteiro.flowcontrol;

import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
public class Producer {

    /*@Autowired
    MeterRegistry meterRegistry;*/

    AtomicInteger lastProducedMessageGauge;

    ConsumerService consumerService;
    public Producer(ConsumerService consumerService) {
        this.consumerService = consumerService;
        this.lastProducedMessageGauge = AMain.meterRegistry.gauge("last_produced_message", new AtomicInteger(0));
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
