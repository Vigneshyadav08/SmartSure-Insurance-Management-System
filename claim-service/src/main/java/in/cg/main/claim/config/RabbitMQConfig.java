package in.cg.main.claim.config;

import org.modelmapper.ModelMapper;
import org.springframework.amqp.core.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {
    public static final String EXCHANGE = "smartsure.exchange";
    public static final String CLAIM_QUEUE = "claim.submitted.queue";
    public static final String CLAIM_ROUTING_KEY = "claim.submitted";

    @Bean
    public Queue claimQueue() { return new Queue(CLAIM_QUEUE, true); }

    @Bean
    public DirectExchange claimDirectExchange() { return new DirectExchange(EXCHANGE); }

    @Bean
    public Binding bindingClaim(Queue claimQueue, DirectExchange claimDirectExchange) {
        return BindingBuilder.bind(claimQueue).to(claimDirectExchange).with(CLAIM_ROUTING_KEY);
    }
    
    @Bean
    public ModelMapper modelMapper()
    {
    		return new ModelMapper();
    }
}
