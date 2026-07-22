package com.cardex.api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import com.cardex.api.config.properties.JwtProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties(JwtProperties.class)
public class CardexApiApplication {

	public static void main(String[] args) {
		SpringApplication.run(CardexApiApplication.class, args);
	}

}
