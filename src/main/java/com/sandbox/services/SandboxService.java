package com.sandbox.services;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.GetResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbitmq.client.AMQP.BasicProperties;
import com.sandbox.config.RabbitConfig;
//import com.sandbox.config.RabbitConfiguration;
import com.sandbox.config.RabbitConfiguration;
import com.sandbox.model.GenericResponse;
import com.sandbox.model.UsageRequest;

@Service
public class SandboxService {

	private static final String WORK_QUEUE_NAME = "UsageWorkQueue";
	private static final String PUBSUB_EXCHANGE_NAME = "PubSub";

	@Autowired
	private RabbitConfig rabbitConfig;

	public SandboxService() {
		
	}
	
	@PostConstruct
	public void init() {
		try {
			this.startConsumer();
			this.startPubSubConsumer();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public GenericResponse startConsumer() throws Exception {
		GenericResponse response = new GenericResponse();
		response.setResultCode("200");
		response.setResultMessage("Consumer Started");

		System.out.println("@@@@@ RabbitConfig @@@@@@");
		// System.out.println(rabbitConfig.getUris());

		// ConnectionFactory factory = new ConnectionFactory();
		// factory.setHost("localhost");
		// Connection connection = factory.newConnection();
		// Channel channel = connection.createChannel();

		Channel channel = getRabbitMqChannel(WORK_QUEUE_NAME);
		
		
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

		// DeclareOk ok = channel.queueDeclare(WORK_QUEUE_NAME, false, false,
		// false, null);

		System.out.println(" [*] Waiting for messages. ");

		UsageRequestConsumer consumer = new UsageRequestConsumer(channel, this);
		// channel.basicConsume(WORK_QUEUE_NAME, true, consumer);
		channel.basicConsume(WORK_QUEUE_NAME, true, WORK_QUEUE_NAME, consumer);

		System.out.println("Now I am listening!");

		// }
		// connection.close();
		System.out.println("Done");
		return response;
	}

	public GenericResponse stopConsumer() throws Exception {
		GenericResponse response = new GenericResponse();
		response.setResultCode("200");
		response.setResultMessage("Consumer Stopped");

		Channel channel = getRabbitMqChannel(WORK_QUEUE_NAME);

		channel.basicCancel(WORK_QUEUE_NAME);

		System.out.println("Cancelled consumer");

		System.out.println("Done");
		return response;
	}

	public GenericResponse startPubSubConsumer() throws Exception {

		GenericResponse response = new GenericResponse();
		response.setResultCode("200");

//		ConnectionFactory factory = new ConnectionFactory();
//		factory.setUri(getConnectionURI());
//
//		Connection connection = factory.newConnection();
//		Channel channel = connection.createChannel();
		
		Channel channel = rabbitConfig.connectionFactory().rabbitConnectionFactory().createConnection()
				.createChannel(false);

		channel.exchangeDeclare(PUBSUB_EXCHANGE_NAME, "fanout");
		// Get a default queue (random name just for me)
		String queueName = channel.queueDeclare().getQueue();
		// Bind my queue to the exchange (subscribe for messages)
		channel.queueBind(queueName, PUBSUB_EXCHANGE_NAME, "");
		String responseMessage = "Started PubSubConsumer " + queueName;
		response.setResultMessage(responseMessage);
		System.out.println(" [" + responseMessage + "] Waiting for messages. ");

		PubSubConsumer consumer = new PubSubConsumer(channel, queueName);
		channel.basicConsume(queueName, true, consumer);

		return response;

	}

	public GenericResponse getFromDynamicQ(String qId) throws Exception {
		GenericResponse response = new GenericResponse();
		response.setResultCode("200");

		String dynamicQ = "request_" + qId;

		System.out.println("Looking for a message on: " + dynamicQ);

		Channel channel = getDynamicRabbitMqChannel(dynamicQ);

		GetResponse res = channel.basicGet(dynamicQ, true);
		if (res != null) {
			System.out.println("I got a message!");
			response.setResultMessage(new String(res.getBody(), "UTF-8"));

			// Got the message so clean up the queue

			channel.queueDelete(dynamicQ);

		} else {
			System.out.println("No message found");
		}
		channel.getConnection().close();
		return response;
	}

	public UsageRequest enqueueRequest(UsageRequest request) throws Exception {

		String uuid = UUID.randomUUID().toString();

		BasicProperties props = new BasicProperties.Builder().replyTo("response_" + uuid).build();

		// You should really be using routing headers instead of data to
		// determine the response
		// queue. Routing in the message is done for illistration purposes only.
		request.setRequestId(uuid);

//		System.out.println("EnqueueRequest: Going to connect to Rabbit at " + getConnectionURI());

		// Channel channel = getRabbitMqChannel(getConnectionURI(),
		// WORK_QUEUE_NAME);
		// System.out.println("Channel is: " + channel);
		// String message = request.toJSON();
		// channel.basicPublish("", WORK_QUEUE_NAME, null,
		// message.getBytes("UTF-8"));
		// System.out.println(" [x] Sent '" + message + "'");
		// channel.getConnection().close();

		// Return the request with the uuid populated

		Channel channel = rabbitConfig.connectionFactory().rabbitConnectionFactory().createConnection()
				.createChannel(false);
		System.out.println("Channel is: " + channel);
		String message = request.toJSON();
		channel.basicPublish("", WORK_QUEUE_NAME, null, message.getBytes("UTF-8"));
		System.out.println(" [x] Sent '" + message + "'");
		channel.getConnection().close();

		return request;
	}

	public GenericResponse sendPubSub(String message) throws Exception {

		GenericResponse response = new GenericResponse();
		response.setResultCode("200");
		response.setResultMessage("Connection Successful");

//		ConnectionFactory factory = new ConnectionFactory();
//		String uri = getConnectionURI();
//		factory.setUri(uri);
//		Connection connection = factory.newConnection();
//		Channel channel = connection.createChannel();
		
		Channel channel = rabbitConfig.connectionFactory().rabbitConnectionFactory().createConnection()
				.createChannel(false);

		channel.exchangeDeclare(PUBSUB_EXCHANGE_NAME, "fanout");

		channel.basicPublish(PUBSUB_EXCHANGE_NAME, "", null, message.getBytes("UTF-8"));
		System.out.println(" [x] Sent '" + message + "'");

		channel.close();
//		connection.close();

		return response;
	}

	public GenericResponse ping() {

		GenericResponse response = new GenericResponse();
		response.setResultCode("200");
		response.setResultMessage("Connection Successful");

		// ------
		System.out.println("The connection factory is: " + rabbitConfig.connectionFactory());

		// -----
		return response;

	}

//	public String getConnectionURI() {
//		System.out.println("CHECKING CONFIG!!!!");
//
//		String uri = "";
//		Map<String, Object> vcap = mapVCAP(System.getenv().get("VCAP_SERVICES"));
//
//		if (vcap != null) {
//			for (String element : vcap.keySet()) {
//
//				// if there is a redis service bound, get the info
//				if (element.contains("p-rabbitmq")) {
//
//					System.out.println("I have a Rabbit Config! ****");
//
//					ArrayList<Object> a = (ArrayList<Object>) vcap.get(element);
//					// redisConfig = getRedisConfig((LinkedHashMap) a.get(0));
//					Iterator<Object> i = a.iterator();
//					while (i.hasNext()) {
//
//						LinkedHashMap section = (LinkedHashMap) i.next();
//						String credentials = (String) section.get("credentials").toString();
//						int start = credentials.indexOf("uri=amqp") + 4;
//						String temp = credentials.substring(start);
//						int end = temp.indexOf(", uris=");
//						uri = temp.substring(0, end);
//
//						System.out.println(uri);
//
//					}
//
//				}
//			}
//		}
//
//		return uri;
//	}

	public Channel getRabbitMqChannel(String queueName) throws Exception {
//		ConnectionFactory factory = new ConnectionFactory();
//		factory.setUri(uri);
//		Connection connection = factory.newConnection();
//		Channel channel = connection.createChannel();
		Channel channel = rabbitConfig.connectionFactory().rabbitConnectionFactory().createConnection()
				.createChannel(false);
		channel.queueDeclare(queueName, false, false, false, null);
		return channel;	
	}

	public Channel getDynamicRabbitMqChannel(String queueName) throws Exception {
//		ConnectionFactory factory = new ConnectionFactory();
//		factory.setUri(uri);
//		Connection connection = factory.newConnection();
//		Channel channel = connection.createChannel();
		Channel channel = rabbitConfig.connectionFactory().rabbitConnectionFactory().createConnection()
				.createChannel(false);
		Map<String, Object> args = new HashMap<String, Object>();
		// args.put("x-message-ttl", 60000);
		args.put("x-expires", 120000);
		channel.queueDeclare(queueName, false, false, false, args);
		return channel;
	}

//	@SuppressWarnings("unchecked")
//	public static Map<String, Object> mapVCAP(String vcapString) {
//
//		Map<String, Object> vcap = null;
//
//		if (vcapString != null) {
//			ObjectMapper mapper = new ObjectMapper();
//			try {
//				vcap = mapper.readValue(vcapString, Map.class);
//				System.out.println("vcap: " + vcap);
//			} catch (Exception e) {
//				e.printStackTrace();
//			}
//		} else {
//			System.out.println("There was no VCAP environment variable.");
//		}
//
//		return vcap;
//
//	}
}