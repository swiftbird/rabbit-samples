package com.sandbox.simplemains;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeoutException;

import com.rabbitmq.client.AMQP.Queue.DeclareOk;

public class AutoDeleteTester {

	private Timer timer;
	private Channel channel;
	private String consumerTag;
	private Connection connection;

	private static final String QUEUE_NAME = "DynamicQueue";

	public static void main(String[] argv) throws Exception {
		AutoDeleteTester adt = new AutoDeleteTester();
		adt.startListening();
	}

	public void startListening() throws IOException, TimeoutException {

		ConnectionFactory factory = new ConnectionFactory();
		factory.setHost("localhost");
		connection = factory.newConnection();
		channel = connection.createChannel();
		// channel.queueDeclare(queue, durable, exclusive, autoDelete,
		// arguments)

		DeclareOk ok = channel.queueDeclare(QUEUE_NAME, false, false, true, null);
		if (ok.getConsumerCount() == 0) {

			System.out.println(" [*] Waiting for messages. To exit press CTRL+C");

			SampleQueueingConsumer consumer = new SampleQueueingConsumer(channel, this);
			consumerTag = channel.basicConsume(QUEUE_NAME, true, consumer);

			resetTimer();

			System.out.println("Now I am listening!");
		} else {
			System.out.println("Someone beat me to it ");
			connection.close();
		}
		// }
		// connection.close();
		System.out.println("Finished Loading consumer");
	}

	public void resetTimer() {

		System.out.println("Resetting the Timer!");
		int seconds = 15;
		if (timer != null) {
			System.out.println("Timer was cancelled");
			timer.cancel();
		}
		timer = new Timer();
		timer.schedule(new DoneWithQueueTask(), seconds * 1000);
	}

	class DoneWithQueueTask extends TimerTask {
		public void run() {
			System.out.println("Timer is up. Going to stop consuming " + consumerTag);
			timer.cancel();
			try {
				channel.basicCancel(consumerTag);
				connection.close();
			} catch (IOException e) {

				e.printStackTrace();
			}

		}
	}

}
