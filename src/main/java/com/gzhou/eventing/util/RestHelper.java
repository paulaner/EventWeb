package com.gzhou.eventing.util;


import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

import java.util.ArrayList;
import java.util.List;

@Component
public class RestHelper {

    public ResponseEntity<String> sendRestCall(JSONObject json) {

        RestHelperWithRetry restHelperWithRetry = new RestHelperWithRetry();

        // process failed rest calls
        // if found failed rest calls from Redis server, then process those failed calls first
        // jedis.rpush method will keep the order of failed call in a FIFO order
//        JedisPool pool = new JedisPool(new JedisPoolConfig(), Constants.redisUrl, Constants.redisPort);
//        Jedis jedis = pool.getResource();
//        jedis.select(0);

        Jedis jedis = RedisUtil.getJedis();

        List<String> jsonList;

        if (jedis.exists("queue") && jedis.llen("queue") > 0) {

            jsonList = jedis.lrange("queue", 0, -1);

            List<String> failedCalls = new ArrayList<>();
            if (jsonList.size() > 0) {
                for (String str : jsonList) {
                    boolean isSent = restHelperWithRetry.sendRestCall(new JSONObject(str));
                    if (!isSent) {
                        failedCalls.add(str);
                    }
                }
            }

            jedis.del("queue");

            if (failedCalls.size() > 0) {
                for (String str : failedCalls) {
                    jedis.rpush("queue", str);
                }
            }
        }

        boolean isThisSent = restHelperWithRetry.sendRestCall(json);

        if (isThisSent) {
//            jedis.close();
            RedisUtil.returnJedis(jedis);
            return new ResponseEntity<String>(HttpStatus.OK);
        }

        jedis.rpush("queue", json.toString());
//        jedis.close();
        RedisUtil.returnJedis(jedis);

        return new ResponseEntity<String>(HttpStatus.GATEWAY_TIMEOUT);
    }
}
