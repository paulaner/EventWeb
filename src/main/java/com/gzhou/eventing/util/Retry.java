package com.gzhou.eventing.util;


import org.json.JSONObject;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;

import java.util.concurrent.Callable;
import java.util.concurrent.TimeoutException;

public class Retry implements Callable<Boolean> {

    private JSONObject json;

    public Retry(JSONObject json) {
        this.json = json;
    }

    @Override
    public Boolean call() throws Exception {

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> entity = new HttpEntity<String>(json.toString(), headers);

        RestTemplate restTemplate = new RestTemplate();

        ResponseEntity<String> response =
                restTemplate.exchange(Constants.incomingWebHook, HttpMethod.POST, entity, String.class);

        if (!response.getStatusCode().equals(HttpStatus.OK)) {
            throw new TimeoutException("Connection timed out");
        }

        return true;
    }
}
