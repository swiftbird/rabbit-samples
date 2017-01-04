package com.sandbox.services;

import java.io.IOException;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Envelope;
import com.rabbitmq.client.QueueingConsumer;
import com.sandbox.model.UsageRequest;

public class UsageRequestConsumer extends QueueingConsumer {

	SandboxService service;

	public UsageRequestConsumer(Channel ch, SandboxService svc) {
		super(ch);
		this.service = svc;

	}

	@Override
	public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body)
			throws IOException {

		String message = new String(body, "UTF-8");	

		ObjectMapper mapper = new ObjectMapper();
		UsageRequest request = mapper.readValue(message, UsageRequest.class);

		System.out.println(this.getConsumerTag() + " Received '" + message + "'");

		// This way is determining the queue name from the message content
		String dynamicQ = "request_" + request.getRequestId();
		
		// This way is determining the queue name from the message header (prefered)
//		String dynamicQ = properties.getReplyTo();

		System.out.println("Going to create a queue called: " + dynamicQ);
		// This is where the work gets done

		System.out.println("Handle Delivery: Going to connect to Rabbit ");
		try {
			Channel channel = service.getDynamicRabbitMqChannel(dynamicQ);
			System.out.println("Channel is: " + channel);

			// now actually make the call here to get the response

			String response = request.toJSON();
			channel.basicPublish("", dynamicQ, null, message.getBytes("UTF-8"));
			System.out.println(" [x] Sent '" + message + "'");
			channel.getConnection().close();
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

}
