package com.gzhou.eventing.Subscriber;

import com.gzhou.eventing.util.RestHelper;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;

import javax.jms.*;

/**
 * Created by Alan on 6/3/2016.
 */
public final class ErrorQueueSubscriber {

    @Autowired
    private RestHelper restHelper;

    private String json;
    private volatile boolean shutdown;

    private ActiveMQConnectionFactory connectionFactory;

    public ErrorQueueSubscriber(String json, ActiveMQConnectionFactory connectionFactory) {
        this.json = json;
        this.connectionFactory = connectionFactory;
        this.shutdown = false;
    }

    public void run() throws Exception {

        Connection connection = connectionFactory.createConnection();
        connection.setClientID("ErrorQueueListener");

        Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
        Destination destination = session.createQueue("Event.Error");
        MessageConsumer consumer = session.createConsumer(destination);

        connection.start();

        Message message = consumer.receive();

        if (message instanceof TextMessage) {
            TextMessage textMessage = (TextMessage) message;

            restHelper.sendRestCall(new JSONObject(textMessage.getText()));

            System.out.println("Received message" + textMessage.getText() + "'");
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
