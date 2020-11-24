package com.savio.monteiro.flowcontrol;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
@Component("flowControlledBridge")
public class ConsumerBridge extends ThreadPoolExecutor implements ConsumerService {

    final static int MAX_BACKPRESSURE_CAPACITY = 5;

    /*@Autowired
    private MeterRegistry meterRegistry;*/


    private ConsumerService forwardingConsumerService;
    private BlockingQueue<Runnable> consumerBridgeQueue;
    private MessageDropPolicy messageDropPolicy = MessageDropPolicy.DROP_AT_EGRESS;


    private AtomicInteger lastBridgeMessageGauge;
    private LinkedBlockingQueue<Runnable> queue;

    public enum MessageDropPolicy {
        DROP_AT_INGRESS,
        DROP_AT_EGRESS
    }

    @Autowired
    public ConsumerBridge(ConsumerService forwardingConsumerService) {
        super(1, 1, 0L, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<>(MAX_BACKPRESSURE_CAPACITY), new RejectedExecutionHandler() {
            @Override
            public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
                log.info("Message Rejected");
            }
        });
        init(forwardingConsumerService, MessageDropPolicy.DROP_AT_EGRESS);
    }

    public ConsumerBridge(ConsumerService forwardingConsumerService, MessageDropPolicy messageDropPolicy) {
        super(1, 1, 0L, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<>(MAX_BACKPRESSURE_CAPACITY), new RejectedExecutionHandler() {
            @Override
            public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
                log.info("Message Rejected");
            }
        });
        init(forwardingConsumerService, messageDropPolicy);
    }

    private void init(ConsumerService forwardingConsumerService, MessageDropPolicy messageDropPolicy) {
        this.forwardingConsumerService = forwardingConsumerService;
        this.messageDropPolicy = messageDropPolicy;

        consumerBridgeQueue = getQueue();

        lastBridgeMessageGauge = AMain.meterRegistry.gauge("last_bridged_message", new AtomicInteger(0));
    }

    @Override
    public void send(final String mesg) {

        consumerBridgeQueue.offer(new MessageSender(mesg, forwardingConsumerService));

        // DO STUFF WITH SINK_BRIDGE_QUEUE TO HANDLE BACKPRESSURE
        int remainingCapacity = consumerBridgeQueue.remainingCapacity();
        if(remainingCapacity == 0) {
            log.info("Reached MAX Capacity: {}", MAX_BACKPRESSURE_CAPACITY);
            switch (messageDropPolicy) {
                case DROP_AT_INGRESS:
                    log.info("Dropping message at bridge ingress");
                    break;
                case DROP_AT_EGRESS:
                    log.info("Dropping message at bridge egress");

                    consumerBridgeQueue.poll();
                    break;
            }
        }
        else {
            assert consumerBridgeQueue.peek() != null;
            MessageSender messageSender = (MessageSender) consumerBridgeQueue.poll();
            assert messageSender != null;
            execute(messageSender);

            final int msgId = Integer.parseInt(messageSender.getMesg().split(" ")[1]);
            lastBridgeMessageGauge.set(msgId);
        }
    }

    private class MessageSender implements Runnable {

        private ConsumerService consumerService;

        @Getter
        private String mesg;

        public MessageSender(String mesg, ConsumerService consumerService) {
            this.mesg = mesg;
            this.consumerService = consumerService;
        }

        @Override
        public void run() {
            consumerService.send(mesg);
        }
    }
}
