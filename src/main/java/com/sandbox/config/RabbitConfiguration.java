package com.sandbox.config;

import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.cloud.Cloud;
import org.springframework.cloud.CloudFactory;
import org.springframework.cloud.config.java.AbstractCloudConfig;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

public class RabbitConfiguration  {

	@Configuration
	@Profile("cloud")
	static class RabbitConfig {

		// This will bind to the service called "rabbit"
		@Bean
		public ConnectionFactory rabbitFactory() {
			CloudFactory cloudFactory = new CloudFactory();
			Cloud cloud = cloudFactory.getCloud();
			return cloud.getServiceConnector("rabbit", ConnectionFactory.class, null);
			
//			return connectionFactory().rabbitConnectionFactory("rabbit");
		}
		
		public ConnectionFactory getFactory() {
			CloudFactory cloudFactory = new CloudFactory();
			Cloud cloud = cloudFactory.getCloud();
			return cloud.getServiceConnector("rabbit", ConnectionFactory.class, null);
			
		}

	}
	
	@Configuration
	@Profile("default")
	static class RabbitLocalConfig {
		@Bean
		public ConnectionFactory rabbitFactory()  {
			CachingConnectionFactory factory = new CachingConnectionFactory();

			factory.setUri("amqp://shaun:Gand4alf@localhost");

			return factory;
		}
		
		public ConnectionFactory getFactory() {
			CachingConnectionFactory factory = new CachingConnectionFactory();
			factory.setUri("amqp://shaun:password@localhost");
			return factory;
		}
	}

}
