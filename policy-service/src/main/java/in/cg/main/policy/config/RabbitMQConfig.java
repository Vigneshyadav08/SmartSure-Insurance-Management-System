package in.cg.main.policy.config;

import org.modelmapper.ModelMapper;
import org.springframework.amqp.core.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {
    public static final String EXCHANGE = "smartsure.exchange";
    public static final String POLICY_QUEUE = "policy.purchase.queue";
    public static final String POLICY_ROUTING_KEY = "policy.purchased";

    @Bean
    public Queue policyQueue() { return new Queue(POLICY_QUEUE, true); }

    @Bean
    public DirectExchange policyDirectExchange() { return new DirectExchange(EXCHANGE); }

    @Bean
    public Binding bindingPolicy(Queue policyQueue, DirectExchange policyDirectExchange) {
        return BindingBuilder.bind(policyQueue).to(policyDirectExchange).with(POLICY_ROUTING_KEY);
    }
    @Bean
    public ModelMapper modelMapper()
    {
    		return new ModelMapper();
    }
}
