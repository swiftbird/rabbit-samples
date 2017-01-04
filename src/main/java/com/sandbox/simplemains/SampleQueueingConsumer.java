package com.sandbox.simplemains;

import java.io.IOException;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;

import com.rabbitmq.client.Envelope;
import com.rabbitmq.client.QueueingConsumer;

public class SampleQueueingConsumer extends QueueingConsumer {

    private AutoDeleteTester tester;

    public SampleQueueingConsumer(Channel channel, AutoDeleteTester tester) {
        super(channel);
        this.tester = tester;

    }

    @Override
    public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body)
            throws IOException {

        tester.resetTimer();
        String message = new String(body, "UTF-8");
        System.out.println(this.getConsumerTag() + " Received '" + message + "'");

    }

}
