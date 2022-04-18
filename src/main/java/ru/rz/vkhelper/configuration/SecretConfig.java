package ru.rz.vkhelper.configuration;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.annotation.PropertySources;

@Configuration
@PropertySource("classpath:secret.properties")
@Getter
public class SecretConfig {

    @Value("${vk.client_secret}")
    String clientSecret;

}
