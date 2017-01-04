package com.sandbox.services;

import java.io.IOException;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;

public class PubSubConsumer extends DefaultConsumer{
	
	private String myId;

	public PubSubConsumer(Channel channel, String id) {
		super(channel);
		myId = id;
		
	}
	
	@Override
	public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties,
			byte[] body) throws IOException {
		String message = new String(body, "UTF-8");
		System.out.println(" Consumer " + myId + " Received '" + message + "'");
	}

}
