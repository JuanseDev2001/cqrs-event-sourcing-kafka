package com.techbank.account.query.infrastructure.consumers;

import com.techbank.account.query.infrastructure.handlers.EventHandler;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import java.util.Map;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

@Service
public class AccountEventConsumer implements EventConsumer {
    @Autowired
    private EventHandler eventHandler;

    @Autowired
    private ObjectMapper objectMapper;

    private final Logger logger = Logger.getLogger(AccountEventConsumer.class.getName());

    private final Map<String, Consumer<Object>> eventHandlers = Map.of(
        "AccountOpenedEvent", e -> eventHandler.on(objectMapper.convertValue(e, com.techbank.account.common.events.AccountOpenedEvent.class)),
        "FundsDepositedEvent", e -> eventHandler.on(objectMapper.convertValue(e, com.techbank.account.common.events.FundsDepositedEvent.class)),
        "FundsWithdrawnEvent", e -> eventHandler.on(objectMapper.convertValue(e, com.techbank.account.common.events.FundsWithdrawnEvent.class)),
        "AccountClosedEvent", e -> eventHandler.on(objectMapper.convertValue(e, com.techbank.account.common.events.AccountClosedEvent.class))
    );

    @KafkaListener(topics = "${kafka.topic}", groupId = "${spring.kafka.consumer.group-id}")
    @Override
    public void consume(@Payload Object payload, Acknowledgment ack, @Header(value = "eventType", required = false) String eventType) {
        try {
            Object message = (payload instanceof ConsumerRecord) ? ((ConsumerRecord<?, ?>) payload).value() : payload;
            if (eventType == null || !eventHandlers.containsKey(eventType)) {
                logger.log(Level.WARNING, "Unknown or missing eventType: " + eventType);
            } else {
                eventHandlers.get(eventType).accept(message);
            }
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error processing event", e);
        } finally {
            ack.acknowledge();
        }
    }
}