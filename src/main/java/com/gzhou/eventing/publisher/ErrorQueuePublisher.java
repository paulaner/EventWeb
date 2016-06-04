package com.gzhou.eventing.publisher;

import org.apache.activemq.ActiveMQConnectionFactory;

import javax.jms.*;

/**
 * Created by Alan on 6/3/2016.
 */
public class ErrorQueuePublisher implements Runnable {

    private String json;
    private volatile boolean shutdown;

    private ActiveMQConnectionFactory connectionFactory;

    public ErrorQueuePublisher(String json,
                               ActiveMQConnectionFactory connectionFactory) {
        this.json = json;
        this.connectionFactory = connectionFactory;
        this.shutdown = false;
    }

    @Override
    public void run() {
        Session session = null;
        MessageProducer sendPublisher;
        Connection connection = null;
        while (!shutdown) {
            try {
                connection = connectionFactory.createConnection();
                connection.start();
                session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
                Destination destination = session.createQueue("Event.Error");

                sendPublisher = session.createProducer(destination);
                sendPublisher.setDeliveryMode(DeliveryMode.NON_PERSISTENT);
                sendPublisher.send(session.createTextMessage(json));

                //wait 1s make sure not to return before message is not sent yet
                //due to connection latency
                Thread.sleep(1000);
                this.shutdown = true;

                System.out.println("Sent !!");

            } catch (JMSException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public String getJson() {
        return json;
    }

    public void setJson(String json) {
        this.json = json;
    }

    public boolean isShutdown() {
        return shutdown;
    }

    public void setShutdown(boolean shutdown) {
        this.shutdown = shutdown;
    }
}