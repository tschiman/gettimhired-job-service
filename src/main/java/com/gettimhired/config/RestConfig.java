package com.gettimhired.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
public class RestConfig {

    @Value("${resumejobservice.mainapp.host}")
    private String host;

    @Bean
    RestClient resumeSiteRestClient() {
        return RestClient.builder().baseUrl(host).build();
    }
}
