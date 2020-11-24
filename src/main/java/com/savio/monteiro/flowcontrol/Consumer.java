package com.savio.monteiro.flowcontrol;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
@Component("forwardingConsumer")
public class Consumer implements ConsumerService {

    /*@Autowired
    MeterRegistry meterRegistry;*/

    AtomicInteger lastMessageConsumed;

    public Consumer() {
        this.lastMessageConsumed = AMain.meterRegistry.gauge("last_message_consumed", new AtomicInteger(0));
    }

    @Override
    public synchronized void send(String mesg) {
        log.info("Sending to Sink: {}", mesg);

        final int msgId = Integer.parseInt(mesg.split(" ")[1]);
        lastMessageConsumed.set(msgId);

        // SIMULATE THROTTLING
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
