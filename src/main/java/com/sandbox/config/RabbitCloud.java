package com.sandbox.config;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

import javax.annotation.PostConstruct;

import org.springframework.cloud.config.java.AbstractCloudConfig;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;

@Configuration
@Profile("cloud")
public class RabbitCloud extends AbstractCloudConfig {

	@PostConstruct
	public void init() {
		System.out.println("RabbitCloud configuration created");
	}
	// This will bind to the service that is bound to this app
	@Bean(name = "rabbitConnection")
	public Connection rabbitFactory() {
		Channel tempChannel = connectionFactory().rabbitConnectionFactory().createConnection().createChannel(false);
		Connection conn = tempChannel.getConnection();
		try {
			tempChannel.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (TimeoutException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return conn;
	}

}

	
