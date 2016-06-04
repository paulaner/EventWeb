package com.gzhou.eventing.util;


import com.gzhou.eventing.publisher.ErrorQueuePublisher;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

@Component
public class RestHelper {

    @Autowired
    private ActiveMQConnectionFactory connectionFactory;

    public ResponseEntity<String> sendRestCall(JSONObject json) {

        RestHelperWithRetry restHelperWithRetry = new RestHelperWithRetry();

        boolean isThisSent = restHelperWithRetry.sendRestCall(json);

        if (isThisSent) {
            return new ResponseEntity<String>(HttpStatus.OK);
        }

        ErrorQueuePublisher fs = new ErrorQueuePublisher(json.toString(), connectionFactory);
        Thread t = new Thread(fs);
        t.start();

        return new ResponseEntity<String>(HttpStatus.GATEWAY_TIMEOUT);
    }
}
