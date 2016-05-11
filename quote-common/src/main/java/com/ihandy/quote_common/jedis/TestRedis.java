package com.ihandy.quote_common.jedis;

/**
 * Created by fengwen on 2016/5/11.
 */

public class TestRedis {
    public static byte[] redisKey = "key".getBytes();

    static  {
       // init();
    }
    public static void main(String[] args) throws  Exception{
        init();
        pop();
    }

    private static void pop()  throws  Exception{
        byte[] bytes = JedisUtil.rpop(redisKey);
        Message msg = (Message) ObjectUtil.bytesToObject(bytes);
        if(msg != null){
            System.out.println(msg.getId()+"   "+msg.getContent());
        }
    }

    private static void init() throws  Exception{
        Message msg1 = new Message(1, "内容1");
        JedisUtil.lpush(redisKey, ObjectUtil.objectToBytes(msg1));
        Message msg2 = new Message(2, "内容2");
        JedisUtil.lpush(redisKey, ObjectUtil.objectToBytes(msg2));
        Message msg3 = new Message(3, "内容3");
        JedisUtil.lpush(redisKey, ObjectUtil.objectToBytes(msg3));
    }
}
