package com.sandbox.config;

import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.cloud.config.java.AbstractCloudConfig;

@Configuration
@Profile("cloud")
public class RabbitConfig extends AbstractCloudConfig {

	// This will bind to the service called "rabbit"
	@Bean
	public ConnectionFactory rabbitFactory() {
		return connectionFactory().rabbitConnectionFactory("rabbit");
	}

}
//
//public class RabbitConfig extends AbstractCloudConfig {
//
//	@Configuration
//	@Profile("cloud")
//	static class RabbitCloudConfig extends AbstractCloudConfig {
//
//		// This will bind to the service called "rabbit"
//		@Bean
//		public ConnectionFactory rabbitFactory() {
//			return connectionFactory().rabbitConnectionFactory("rabbit");
//		}
//
//	}
//
//	@Configuration
//	@Profile("default")
//	static class RabbitLocalConfig {
//		@Bean
//		public ConnectionFactory rabbitFactory() {
//			CachingConnectionFactory factory = new CachingConnectionFactory();
//
//			factory.setUri("amqp://shaun:password@localhost");
//
//			return factory;
//		}
//	}
//}
