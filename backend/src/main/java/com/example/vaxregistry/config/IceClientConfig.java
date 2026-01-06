package com.example.vaxregistry.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

@Configuration
@EnableConfigurationProperties(IceProperties.class)
public class IceClientConfig {

    @Bean
    public RestClient iceRestClient(RestClient.Builder builder, IceProperties props) {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(props.getConnectTimeoutMs());
        factory.setReadTimeout(props.getReadTimeoutMs());

        return builder
                .baseUrl(props.getEndpointUrl())
                .requestFactory(factory)
                .build();
    }
}
