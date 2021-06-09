package com.danran.miaosha.MQ;

import com.alibaba.fastjson.JSON;
import com.danran.miaosha.pojo.Order;
import com.danran.miaosha.pojo.OrderMessage;
import com.danran.miaosha.service.BookService;
import com.danran.miaosha.service.OrderService;
import com.danran.miaosha.utils.RedisUtil;
import org.apache.rocketmq.client.consumer.DefaultMQPushConsumer;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyContext;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyStatus;
import org.apache.rocketmq.client.consumer.listener.MessageListenerConcurrently;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.common.message.Message;
import org.apache.rocketmq.common.message.MessageExt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.Map;

/**
 * @Classname MQConsumer
 * @Description TODO
 * @Date 2021/6/7 15:19
 * @Created by LengDanran
 */
@Component
public class MQConsumer {

    private DefaultMQPushConsumer consumer;

    @Value("${mq.nameserver.addr}")
    private String namesrvAddr;

    @Value("${mq.topicname}")
    private String topicName;

    @Autowired
    private OrderService orderService;

    @Autowired
    private BookService bookService;

    @Autowired
    private RedisUtil redisUtil;

    @PostConstruct
    public void init() throws MQClientException {
        consumer = new DefaultMQPushConsumer("stock_consumer_group");
        consumer.setNamesrvAddr(namesrvAddr);
        consumer.subscribe(topicName, "*");

        consumer.registerMessageListener(new MessageListenerConcurrently() {
            @Override
            public ConsumeConcurrentlyStatus consumeMessage(List<MessageExt> msgs, ConsumeConcurrentlyContext context) {
                // 实现真正的数据库扣减库存的逻辑
                System.out.println("<<<<<============消息msgs数量==========>>>>>>>>>===" + msgs.size());
                MessageExt msg = msgs.get(0);
                if (msg.getReconsumeTimes() == 3) {
                    System.out.println("<<<<<============放弃消费==========>>>>>>>>>===");
                    return ConsumeConcurrentlyStatus.CONSUME_SUCCESS;
                }
                String jsonString = new String(msg.getBody());
                Map map = JSON.parseObject(jsonString, Map.class);

                System.out.println("<<<<======消费消息map =========>>>>>" + map);

                String order_id = (String) map.get("order_id");
                Integer user_id = (Integer) map.get("user_id");
                String book_id = (String) map.get("book_id");
                Integer amount = (Integer) map.get("amount");
                Integer version = (Integer) map.get("version");



                System.out.println("<<<<======扣减库存=========>>>>>");
                synchronized (this) {
                    boolean flag = bookService.reduceBook(book_id, version);
                    if (flag) {
                        System.out.println("<<<<======创建订单=========>>>>>");
                        Order order = orderService.addOrder(order_id, user_id, book_id, amount);
                        System.out.println("<<<<======扣减库存后，刷新Redis缓存=========>>>>>");
                        redisUtil.set(book_id, bookService.getBookById(book_id), 2);
                        System.out.println("<<<<<<===回填订单信息["+order+"]=====>>>>>>");
                        OrderMessage.addMessage(order_id, order);
                    } else {
                        System.out.println("<<<<======扣减库存失败，回滚=========>>>>>");
//                    orderService.rollbackOrder(order_id);

                    }
                }

                return ConsumeConcurrentlyStatus.CONSUME_SUCCESS;

            }
        });

        consumer.start();
    }

}
