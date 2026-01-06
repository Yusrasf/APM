package com.example.vaxregistry.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

@Configuration
@EnableConfigurationProperties(FhirProperties.class)
public class FhirClientConfig {

    @Bean
    public RestClient fhirRestClient(RestClient.Builder builder, FhirProperties props) {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(props.getConnectTimeoutMs());
        factory.setReadTimeout(props.getReadTimeoutMs());

        return builder
                .baseUrl(props.getBaseUrl())
                .requestFactory(factory)
                .build();
    }
}
