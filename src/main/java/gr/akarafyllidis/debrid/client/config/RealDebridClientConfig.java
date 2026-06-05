package gr.akarafyllidis.debrid.client.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
public class RealDebridClientConfig {

    @Value("${realdebrid.api-token}")
    private String apiToken;

    @Bean
    public RestClient realDebridRestClient(RestClient.Builder builder) {
        return builder
                .baseUrl("https://api.real-debrid.com/rest/1.0")
                .defaultHeader("Authorization", "Bearer " + apiToken)
                .build();
    }
}
