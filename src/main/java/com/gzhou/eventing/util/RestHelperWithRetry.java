package com.gzhou.eventing.util;


import com.github.rholder.retry.RetryException;
import com.github.rholder.retry.Retryer;
import com.github.rholder.retry.RetryerBuilder;
import com.github.rholder.retry.StopStrategies;
import org.json.JSONObject;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

public class RestHelperWithRetry {

    public boolean sendRestCall(JSONObject json) {

        Retryer<Boolean> retry = RetryerBuilder.<Boolean>newBuilder()
                .retryIfExceptionOfType(TimeoutException.class)
                .retryIfRuntimeException()
                .withStopStrategy(StopStrategies.stopAfterAttempt(3))
                .build();

        boolean ret = false;

        try {
            ret = retry.call(new Retry(json));
        } catch (RetryException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }

        return ret;
    }
}
