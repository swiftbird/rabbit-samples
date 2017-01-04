package com.sandbox.config;

import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
@Profile("default")
public class RabbitLocalConfig {
	
	
	@Bean
	public ConnectionFactory rabbitFactory() {
		CachingConnectionFactory factory = new CachingConnectionFactory();

		factory.setUri("amqp://shaun:password@localhost");

		return factory;
	}
}
