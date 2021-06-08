package com.danran.miaosha.MQ;

import com.alibaba.fastjson.JSON;
import com.danran.miaosha.pojo.Order;
import com.danran.miaosha.service.OrderService;
import org.apache.rocketmq.client.exception.MQBrokerException;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.client.producer.*;
import org.apache.rocketmq.common.message.Message;
import org.apache.rocketmq.common.message.MessageExt;
import org.apache.rocketmq.remoting.exception.RemotingException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @Classname MQProducer
 * @Description TODO
 * @Date 2021/6/6 17:42
 * @Created by LengDanran
 */
@Component
public class MQProducer {

    private DefaultMQProducer producer;

    private TransactionMQProducer transactionMQProducer;

    @Value("${mq.nameserver.addr}")
    private String nameAddr;

    @Value("${mq.topicname}")
    private String topicName;

    @Autowired
    private OrderService orderService;

    @PostConstruct
    public void init() throws MQClientException {
        // 做 MQ producer 的初始化
        producer = new DefaultMQProducer("producer_group");
        producer.setNamesrvAddr(nameAddr);
        producer.start();

        transactionMQProducer = new TransactionMQProducer("transaction_producer_group");
        transactionMQProducer.setNamesrvAddr(nameAddr);
        transactionMQProducer.start();

        transactionMQProducer.setTransactionListener(new TransactionListener() {
            @Override
            public LocalTransactionState executeLocalTransaction(Message msg, Object arg) {
                try {
                    // 开始 ========创建订单 order =========
                    // 事务提交成功后返回提交状态通知
                    String order_id = (String) ((Map) arg).get("order_id");
                    Integer user_id = (Integer) ((Map) arg).get("user_id");
                    Integer book_id = (Integer) ((Map) arg).get("book_id");
                    Integer amount = (Integer) ((Map) arg).get("amount");


                    orderService.addOrder(order_id, user_id, book_id, amount);


                    System.out.println("executeLocalTransaction: msg == " + msg);
                    return LocalTransactionState.COMMIT_MESSAGE;
                } catch (Exception e) {
                    // 事务提交失败后返回回滚状态通知
                    e.printStackTrace();
                    return LocalTransactionState.ROLLBACK_MESSAGE;
                }
            }

            /**
             * 当executeLocalTransaction没返回明确的
             * LocalTransactionState时
             * 就轮到checkLocalTransaction方法了
             */
            @Override
            public LocalTransactionState checkLocalTransaction(MessageExt msg) {
                String jsonString = new String(msg.getBody());
                Map map = JSON.parseObject(jsonString, Map.class);
                String orderID = (String) map.get("order_id");
                System.out.println("=============checkLocalTransaction中生成的orderID ===========>>>>" + orderID);
                Order orderById = orderService.getOrderById(orderID);

                boolean flag = orderById != null; // 查看数据库事务提交结果, 提交成功返回true, 否则返回false
                if(flag) {
                    System.out.println("checkLocalTransaction: msg == " + msg);
                    return LocalTransactionState.COMMIT_MESSAGE;
                } else {
                    return LocalTransactionState.ROLLBACK_MESSAGE;
                }
            }
        });
    }


    /**
     * 事务型同步库存扣减消息
     * @param orderId 系统生成的 UUID 订单号
     * @param userId 用户的ID
     * @param bookId 书籍的ID
     * @return 是否成功
     */
    public boolean transactionAsyncReduceStock(String orderId, Integer userId, Integer bookId) {
        Map<String, Object> bodyMap = new ConcurrentHashMap<>();
        bodyMap.put("order_id", orderId);
        bodyMap.put("user_id", userId);
        bodyMap.put("book_id", bookId);
        bodyMap.put("amount", 1);

        Map<String, Object> argsMap = new ConcurrentHashMap<>();
        argsMap.put("order_id", orderId);
        argsMap.put("user_id", userId);
        argsMap.put("book_id", bookId);
        argsMap.put("amount", 1);


        // 创建消息
        Message msg = new Message(topicName, "reduce stock",
                JSON.toJSON(bodyMap).toString().getBytes(StandardCharsets.UTF_8));

        TransactionSendResult sendResult;

        try {
            // 发送事务消息到消息队列中
            sendResult = transactionMQProducer.sendMessageInTransaction(msg, argsMap);
        } catch (MQClientException e) {
            // 出错即为消息发送失败
            e.printStackTrace();
            return false;
        }
        LocalTransactionState state = sendResult.getLocalTransactionState();
        if (state == LocalTransactionState.ROLLBACK_MESSAGE) {
            return false;
        } else {
            return state == LocalTransactionState.COMMIT_MESSAGE;
        }

    }

    public boolean asyncReduceStock(Integer userId, Integer bookId) {
        Map<String, Object> bodyMap = new HashMap<>();
        bodyMap.put("user_id", userId);
        bodyMap.put("book_id", bookId);
        bodyMap.put("amount", 1);

        Message message = new Message(topicName, "increase",
                JSON.toJSON(bodyMap).toString().getBytes(StandardCharsets.UTF_8));
        try {
            producer.send(message);
        } catch (MQClientException | RemotingException | MQBrokerException | InterruptedException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }


}
