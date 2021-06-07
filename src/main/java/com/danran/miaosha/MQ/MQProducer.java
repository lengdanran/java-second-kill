package com.danran.miaosha.MQ;

import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.client.producer.DefaultMQProducer;
import org.apache.rocketmq.client.producer.LocalTransactionState;
import org.apache.rocketmq.client.producer.TransactionListener;
import org.apache.rocketmq.client.producer.TransactionMQProducer;
import org.apache.rocketmq.common.message.Message;
import org.apache.rocketmq.common.message.MessageExt;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

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
                    // 开始执行操作
                    // 事务提交成功后返回提交状态通知
                    System.out.println("executeLocalTransaction: msg == " + msg);
                    return LocalTransactionState.COMMIT_MESSAGE;
                } catch (Exception e) {
                    // 事务提交失败后返回回滚状态通知
                    return LocalTransactionState.ROLLBACK_MESSAGE;
                }
            }

            @Override
            public LocalTransactionState checkLocalTransaction(MessageExt msg) {
                boolean flag = true; // 查看数据库事务提交结果, 提交成功返回true, 否则返回false
                if(flag){
                    System.out.println("checkLocalTransaction: msg == " + msg);
                    return LocalTransactionState.COMMIT_MESSAGE;
                }else {
                    return LocalTransactionState.ROLLBACK_MESSAGE;
                }
            }
        });
    }




}
