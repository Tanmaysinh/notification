package com.vasyerp.Config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

@Configuration
public class RabbitMQConfig {

    @Value("${app.rabbitmq.notification-exchange}")
    private String notificationExchange;

    @Value("${app.rabbitmq.notification-queue}")
    private String notificationQueue;

    @Value("${app.rabbitmq.notification-routing-key}")
    private String notificationRoutingKey;

    @Value("${app.rabbitmq.status-exchange}")
    private String statusExchange;

    @Value("${app.rabbitmq.status-queue}")
    private String statusQueue;

    @Value("${app.rabbitmq.status-routing-key}")
    private String statusRoutingKey;

    // ---------- Notification (delayed) exchange ----------

    @Bean
    public Queue notificationQueue() {
        return new Queue(notificationQueue, true);
    }

    @Bean
    public Binding notificationBinding() {
        return BindingBuilder.bind(notificationQueue())
                .to(notificationExchange())
                .with(notificationRoutingKey);
    }

    // ---------- Status update exchange (per-contact status pushes) ----------

    @Bean
    public DirectExchange notificationExchange() {
        return new DirectExchange(notificationExchange, true, false);
    }

    @Bean
    public Queue statusQueue() {
        return new Queue(statusQueue, true);
    }


    // ---------- JSON conversion ----------

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter(new ObjectMapper().findAndRegisterModules());
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(jsonMessageConverter());
        return template;
    }

    // ---------- Listener container thread pool (controls consumer-side concurrency) ----------

    @Bean
    public ThreadPoolTaskExecutor rabbitExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(5);
        executor.setMaxPoolSize(20);
        executor.setQueueCapacity(100);
        executor.setThreadNamePrefix("rabbit-listener-");
        executor.initialize();
        return executor;
    }

    @Bean
    public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(
            ConnectionFactory connectionFactory,
            ThreadPoolTaskExecutor rabbitExecutor
    ) {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setMessageConverter(jsonMessageConverter());
        factory.setTaskExecutor(rabbitExecutor);
        return factory;
    }

    // ---------- Separate thread pool for per-contact processing inside the listener ----------

    @Bean
    public ThreadPoolTaskExecutor contactProcessorExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(10);
        executor.setMaxPoolSize(50);
        executor.setQueueCapacity(500);
        executor.setThreadNamePrefix("contact-processor-");
        executor.initialize();
        return executor;
    }

    @Bean
    public ThreadPoolTaskExecutor statusPublisherExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(10);
        executor.setMaxPoolSize(50);
        executor.setQueueCapacity(500);
        executor.setThreadNamePrefix("status-publisher-");
        executor.initialize();
        return executor;
    }

    @Bean
    DirectExchange statusExchange() {
        return new DirectExchange(statusExchange);
    }


    @Bean
    Binding statusBinding() {
        return BindingBuilder.bind(statusQueue())
                .to(statusExchange())
                .with(statusRoutingKey);
    }
}