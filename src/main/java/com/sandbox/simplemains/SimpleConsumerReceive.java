package com.sandbox.simplemains;

import com.rabbitmq.client.AMQP.Queue.DeclareOk;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

public class SimpleConsumerReceive {

	private static final String QUEUE_NAME = "ServiceTestQueue";

	public static void main(String[] argv) throws Exception {

		// for (int x = 0; x < 1000; x++) {
		ConnectionFactory factory = new ConnectionFactory();
		factory.setHost("localhost");
		Connection connection = factory.newConnection();
		Channel channel = connection.createChannel();

		// Uncomment this block for a singleton subscriber
		// DeclareOk ok = channel.queueDeclare(QUEUE_NAME, false, false, false,
		// null);
		// if (ok.getConsumerCount() == 0) {
		//
		// System.out.println(" [*] Waiting for messages. To exit press
		// CTRL+C");
		//
		// SimpleConsumer consumer = new SimpleConsumer(channel);
		// channel.basicConsume(QUEUE_NAME, true, consumer);
		//
		// System.out.println("Now I am listening!");
		// } else {
		// System.out.println("Someone beat me to it ");
		// connection.close();
		// }

		DeclareOk ok = channel.queueDeclare(QUEUE_NAME, false, false, false, null);

		System.out.println(" [*] Waiting for messages. To exit press CTRL+C");

		SimpleConsumer consumer = new SimpleConsumer(channel);
		channel.basicConsume(QUEUE_NAME, true, consumer);

		System.out.println("Now I am listening!");

		// }
		// connection.close();
		System.out.println("Done");

	}

}
