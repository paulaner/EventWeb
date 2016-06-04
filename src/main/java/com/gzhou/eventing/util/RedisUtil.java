package com.gzhou.eventing.util;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

import javax.annotation.Resource;

@Resource
public class RedisUtil {

    private static JedisPool jedisPool;

    private static JedisPoolConfig jedisPoolConfig;

    private static synchronized void poolInit() {
        if (jedisPool == null)
            createJedisPool();
    }

    public static Jedis getJedis() {

        if (jedisPool == null) {
            poolInit();
        }

        Jedis jedis = null;
        try {
            jedis = jedisPool.getResource();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return jedis;
    }

    public static void returnJedis(Jedis jedis) {
        if (null != jedis) {
            try {
                jedisPool.returnResource(jedis);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private static void createJedisPool() {

        jedisPool = new JedisPool(jedisPoolConfig, Constants.redisUrl, Constants.redisPort);

    }

    public JedisPool getJedisPool() {
        return jedisPool;
    }

    public void setJedisPool(JedisPool JedisPool) {
        this.jedisPool = JedisPool;
    }

    public JedisPoolConfig getJedisPoolConfig() {
        return jedisPoolConfig;
    }

    public void setJedisPoolConfig(JedisPoolConfig jedisPoolConfig) {
        this.jedisPoolConfig = jedisPoolConfig;
    }
}
