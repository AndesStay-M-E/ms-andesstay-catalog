package cl.duoc.andesstay.catalog.messaging.consumer;

import cl.duoc.andesstay.catalog.exception.NonRecoverableMessageException;
import cl.duoc.andesstay.catalog.exception.RecoverableMessageException;
import cl.duoc.andesstay.catalog.messaging.model.MessageEnvelope;
import cl.duoc.andesstay.catalog.messaging.model.HousekeepingCommandPayload;
import cl.duoc.andesstay.catalog.messaging.support.MessageAcknowledger;
import cl.duoc.andesstay.catalog.housekeeping.HousekeepingService;
import com.rabbitmq.client.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

/**
 * Consumidor de la cola catalog.housekeeping.q (routing key housekeeping.ticket).
 *
 * Solo traduce el mensaje y decide ACK / reintento / DLQ.
 * La lógica de negocio está en HousekeepingService.
 */
@Component
public class HousekeepingCommandListener {

    private static final Logger log =
            LoggerFactory.getLogger(HousekeepingCommandListener.class);

    private final HousekeepingService housekeepingService;
    private final MessageAcknowledger acknowledger;

    public HousekeepingCommandListener(
            HousekeepingService housekeepingService,
            MessageAcknowledger acknowledger
    ) {
        this.housekeepingService = housekeepingService;
        this.acknowledger = acknowledger;
    }

    @RabbitListener(
            id = "catalog-housekeeping-listener",
            queues = "${andesstay.messaging.route.queue}"
    )
    public void onHousekeepingMessage(
            @Payload MessageEnvelope<HousekeepingCommandPayload> envelope,
            Message message,
            Channel channel
    ) {
        String reference = envelope.reference();

        log.info(
                "Mensaje recibido type={} eventId={} correlationId={} intento={}",
                envelope.type(),
                reference,
                envelope.correlationId(),
                acknowledger.currentAttempt(message) + 1
        );

        try {
            housekeepingService.process(envelope);
            acknowledger.ack(channel, message, reference);

        } catch (NonRecoverableMessageException exception) {
            acknowledger.deadLetter(channel, message, reference, exception.getMessage());

        } catch (RecoverableMessageException exception) {
            acknowledger.retryOrDeadLetter(channel, message, reference, exception.getMessage());

        } catch (RuntimeException exception) {
            // Error no previsto: se trata como temporal y se reintenta.
            acknowledger.retryOrDeadLetter(
                    channel,
                    message,
                    reference,
                    "Error inesperado: " + exception.getMessage()
            );
        }
    }
}
