package com.vasyerp.Config;

import com.fasterxml.jackson.databind.ObjectMapper;
//import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
//import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;




@Configuration
public class AppConfig {

//    @Bean
//    public MessageConverter jsonMessageConverter() {
//        return new Jackson2JsonMessageConverter(
//                new ObjectMapper().findAndRegisterModules()
//        );
//    }

//    @Bean
//    public ObjectMapper objectMapper() {
//        return new ObjectMapper()
//                .findAndRegisterModules();
//    }

//    @Bean
//    public ObjectMapper objectMapper() {
//        ObjectMapper mapper = new ObjectMapper();
//        mapper.registerModule(new JavaTimeModule());
//        return mapper;
//    }
//

    @Bean
    public ObjectMapper objectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        return mapper;
    }

}
