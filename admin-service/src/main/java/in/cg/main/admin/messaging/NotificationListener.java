package in.cg.main.admin.messaging;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class NotificationListener {
    private static final Logger log = LoggerFactory.getLogger(NotificationListener.class);

    @RabbitListener(queues = "policy.purchase.queue")
    public void receivePolicyEvent(String message) {
        log.info("Received Policy Event: {}", message);
        // Can be forwarded to email/SMS in a real app
    }

    @RabbitListener(queues = "claim.submitted.queue")
    public void receiveClaimEvent(String message) {
        log.info("Received Claim Event: {}", message);
    }
}
