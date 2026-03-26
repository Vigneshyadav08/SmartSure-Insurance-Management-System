package in.cg.main.admin.config;

import org.springframework.amqp.core.Queue;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    @Bean
    public Queue policyPurchaseQueue() {
        return new Queue("policy.purchase.queue", true);
    }

    @Bean
    public Queue claimSubmittedQueue() {
        return new Queue("claim.submitted.queue", true);
    }
}
