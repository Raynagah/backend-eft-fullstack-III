package com.backend.gestionMascotas.config;


import org.springframework.amqp.core.*;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    public static final String EXCHANGE_NAME = "mascotas.exchange";
    public static final String QUEUE_NOTIFICACIONES = "notificaciones.queue";
    public static final String ROUTING_KEY = "mascota.reportada";

    // 1. Definimos la cola
    @Bean
    public Queue queue() {
        return new Queue(QUEUE_NOTIFICACIONES, true);
    }

    // 2. Definimos el Exchange
    @Bean
    public TopicExchange exchange() {
        return new TopicExchange(EXCHANGE_NAME);
    }

    // 3. Unimos la cola al Exchange mediante una ruta (Routing Key)
    @Bean
    public Binding binding(Queue queue, TopicExchange exchange) {
        return BindingBuilder.bind(queue).to(exchange).with(ROUTING_KEY);
    }

    // 4. Convertidor para enviar objetos Java como JSON
    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}