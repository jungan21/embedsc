package org.apache.servicecomb.embedsc.core;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ListenerConfig {

    @Bean
    public EmbedSCApplicationListener applicationStartListener(){
        return new EmbedSCApplicationListener();
    }
}
