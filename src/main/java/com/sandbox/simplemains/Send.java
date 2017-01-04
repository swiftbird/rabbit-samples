package com.sandbox.simplemains;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

//import com.tmobile.eus.digitalservices.customeractivity.sandbox.config.Credentials;

public class Send {

    private static final String QUEUE_NAME = "ServiceTestQueue";

    public static void main(String[] argv) throws Exception {
        ConnectionFactory factory = new ConnectionFactory();
        // factory.setHost("localhost");
        // factory.setUsername("shaun");
        // factory.setPassword("Gand4alf");
        // factory.setVirtualHost("/");
        // factory.setPort(5672);
        factory.setUri(getConnectionURI());
        // factory.setUri("amqp://shaun:Gand4alf@localhost");

        Connection connection = factory.newConnection();
        Channel channel = connection.createChannel();

        AMQP.Queue.DeclareOk dok = channel.queueDeclare(QUEUE_NAME, false, false, false, null);

        System.out.println("Consumer Count is: " + dok.getConsumerCount());
        System.out.println("Message Count is: " + dok.getMessageCount());

        String message = "Hello World " + new Date().toString();
        channel.basicPublish("", QUEUE_NAME, null, message.getBytes("UTF-8"));
        System.out.println(" [x] Sent '" + message + "'");

        channel.queueDeclare("AutoDeleteQueue", false, false, true, null);
        channel.basicPublish("", "AutoDeleteQueue", null, message.getBytes("UTF-8"));
        channel.close();
        connection.close();
    }

    public static String getConnectionURI() {
        System.out.println("CHECKING CONFIG!!!!");

        String uri = "";
        Map<String, Object> vcap = mapVCAP(System.getenv().get("VCAP_SERVICES"));
        // System.out.println("got back: " + vcap);

        if (vcap != null) {
            for (String element : vcap.keySet()) {

                // if there is a redis service bound, get the info
                if (element.equals("p-rabbitmq")) {

                    System.out.println("I have a Rabbit Config! ****");

                    ArrayList<Object> a = (ArrayList<Object>) vcap.get(element);
                    // redisConfig = getRedisConfig((LinkedHashMap) a.get(0));
                    Iterator<Object> i = a.iterator();
                    while (i.hasNext()) {

                        LinkedHashMap section = (LinkedHashMap) i.next();
                        String credentials = (String) section.get("credentials").toString();
                        int start = credentials.indexOf("uri=amqp") + 4;
                        String temp = credentials.substring(start);
                        int end = temp.indexOf(", uris=");
                        uri = temp.substring(0, end);

                        System.out.println(uri);

                    }

                }
            }
        }

        return uri;
    }

    // Map<String, Object> vcap = mapVCAP(System.getenv().get("VCAP_SERVICES"));
    // if (vcap != null) {
    // for (String element : vcap.keySet()) {
    //
    // // if there is a redis service bound, get the info
    // if (element.equals("p-redis")) {
    // log.debug("I have a REDIS Config! ****");
    //
    // ArrayList<Object> a = (ArrayList<Object>) vcap.get(element);
    // redisConfig = getRedisConfig((LinkedHashMap) a.get(0));
    //
    // }
    // }
    //
    // Credentials creds = redisConfig.getCredentials();
    //
    // String redisURIString = "redis://" + creds.getPassword() + "@"
    // + creds.getHost() + ":" + creds.getPort();
    // RedisURI redisUri = RedisURI.create(redisURIString);
    // redisClient = RedisClient.create(redisUri);
    // }
    //
    // private RedisConfig getRedisConfig(HashMap jsonString) {
    //
    // System.out.println(" Going to try get the config for " + jsonString);
    // RedisConfig rc = new RedisConfig();
    //
    // rc.setCredentials(new Credentials());
    // rc.setLabel((String) jsonString.get("label"));
    // rc.setName((String) jsonString.get("name"));
    // rc.setPlan((String) jsonString.get("name"));
    // HashMap<String, Object> creds = (HashMap<String, Object>) jsonString
    // .get("credentials");
    // System.out.println("Credentials = "
    // + jsonString.get("credentials").getClass());
    // rc.getCredentials().setHost((String) creds.get("host"));
    // rc.getCredentials().setPassword((String) creds.get("password"));
    // rc.getCredentials().setPort((Integer) creds.get("port"));
    //
    // return rc;
    //
    // }

    private static Map<String, Object> mapVCAP(String vcapString) {

        Map<String, Object> vcap = null;

        if (vcapString != null) {
            ObjectMapper mapper = new ObjectMapper();
            try {
                vcap = mapper.readValue(vcapString, Map.class);
                System.out.println("vcap: " + vcap);
            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        } else {
            System.out.println("There was no VCAP environment variable.");
        }

        return vcap;

    }
}
