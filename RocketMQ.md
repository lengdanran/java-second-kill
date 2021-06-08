# CentOS 8 RocketMQ Install

## Install JDK 1.8 OR new version

```shell
$ wget https://repo.huaweicloud.com/java/jdk/8u152-b16/jdk-8u152-linux-x64.rpm
```

使用rpm包安装会很快的。

``` shell
$ rpm -i jdk-8u152-linux-x64.rpm
Unpacking JAR files...
	tools.jar...
	plugin.jar...
	javaws.jar...
	deploy.jar...
	rt.jar...
	jsse.jar...
	charsets.jar...
	localedata.jar...
```

## 下载并启动 RocketMQ4.4.0 version

http://rocketmq.apache.org/release_notes/

选择二进制文件，如果是源码，需要自己编译（麻烦）

``` shell
$ wget https://archive.apache.org/dist/rocketmq/4.4.0/rocketmq-all-4.4.0-bin-release.zip
--2021-06-07 20:25:15--  https://archive.apache.org/dist/rocketmq/4.4.0/rocketmq-all-4.4.0-bin-release.zip
Resolving archive.apache.org (archive.apache.org)... 138.201.131.134, 2a01:4f8:172:2ec5::2
Connecting to archive.apache.org (archive.apache.org)|138.201.131.134|:443... connected.
HTTP request sent, awaiting response... 200 OK
Length: 12306820 (12M) [application/zip]
Saving to: ‘rocketmq-all-4.4.0-bin-release.zip’

100%[===========================================================>] 12,306,820   106KB/s   in 3m 25s 

2021-06-07 20:28:41 (58.5 KB/s) - ‘rocketmq-all-4.4.0-bin-release.zip’ saved [12306820/12306820]


================解压缩==============
mkdir /usr/local/rocketmq
unzip rocketmq-all-4.4.0-bin-release.zip -d /usr/local/rocketmq
cd /usr/local/rocketmq/rocketmq-all-4.4.0-bin-release
nohup ./bin/mqnamesrv -n 你的公网IP:9876 >> namesrv.log
# 此时会出现这样的结果：
=================================分割线=========================================
Java HotSpot(TM) 64-Bit Server VM warning: Using the DefNew young collector with the CMS collector is deprecated and will likely be removed in a future release
Java HotSpot(TM) 64-Bit Server VM warning: UseCMSCompactAtFullCollection is deprecated and will likely be removed in a future release.
Java HotSpot(TM) 64-Bit Server VM warning: INFO: os::commit_memory(0x00000006c0000000, 2147483648, 0) failed; error='Cannot allocate memory' (errno=12)
=====================================分割线=======================================
说明是内存太小，启动不起来，需要修改内存配置文件：
启动内存配置
由于name server和broker默认配置为4g到8g的内存，如果服务器内存不足需要修改默认内存；
解决办法，找到runserver.sh和runbroker.sh，
编辑 JAVA_OPT=”${JAVA_OPT} -server -Xms128m -Xmx256m -Xmn256m -XX:MetaspaceSize=128m -XX:MaxMetaspaceSize=320m”


vim ./bin/runserver.sh

vim ./bin/runbroker.sh

启动namesrv:

nohup ./bin/mqnamesrv -n 你的公网IP:9876 >> namesrv.log

==========namesrv.log===============
Java HotSpot(TM) 64-Bit Server VM warning: Using the DefNew young collector with the CMS collector is deprecated and will likely be removed in a future release
Java HotSpot(TM) 64-Bit Server VM warning: UseCMSCompactAtFullCollection is deprecated and will likely be removed in a future release.
The Name Server boot success. serializeType=JSON


修改broker的配置文件：
vim ./conf/broker.conf

===================./conf/broker.conf========================
brokerClusterName = DefaultCluster
brokerName = broker-a
brokerId = 0
deleteWhen = 04
fileReservedTime = 48
brokerRole = ASYNC_MASTER
flushDiskType = ASYNC_FLUSH
brokerIP1 = 121.37.174.92     # 添加这一行，为你的公网ip
==================./conf/broker.conf==========================

nohup sh ./bin/mqbroker -n 你的公网ip:9876 -c ./conf/broker.conf autoCreateTopicEnable=true >> broker.log

如果broker.log出现：
Error occurred during initialization of VM
Initial heap size set to a larger value than the maximum heap size
就改小./bin/runbroker.sh的内存大小
本人服务器 1G:
#===========================================================================================
# JVM Configuration
#===========================================================================================
JAVA_OPT="${JAVA_OPT} -server -Xms64m -Xmx128m -Xmn128m "

===========broker.log===================
The broker[broker-a, 121.37.174.92:10911] boot success. serializeType=JSON and name server is 121.37.174.92:9876



```



## 注意：服务器端口问题会导致timeout异常

使用Spring来测试：

``` java
org.apache.rocketmq.remoting.exception.RemotingTooMuchRequestException: sendDefaultImpl call timeout
at org.apache.rocketmq.client.impl.producer.DefaultMQProducerImpl.sendDefaultImpl(DefaultMQProducerImpl.java:612) ~[rocketmq-client-4.3.1.jar:4.3.1]
at org.apache.rocketmq.client.impl.producer.DefaultMQProducerImpl.send(DefaultMQProducerImpl.java:1253) ~[rocketmq-client-4.3.1.jar:4.3.1]
at org.apache.rocketmq.client.impl.producer.DefaultMQProducerImpl.send(DefaultMQProducerImpl.java:1203) ~[rocketmq-client-4.3.1.jar:4.3.1]
at org.apache.rocketmq.client.producer.DefaultMQProducer.send(DefaultMQProducer.java:214) ~[rocketmq-client-4.3.1.jar:4.3.1]

在出这个异常之前有[NettyClientSelector_1] INFO RocketmqRemoting.info (Slf4jLoggerFactory.java:95)
closeChannel: close the connection to remote address[] result: true

通过telnet验证过9876端口是通的
rocketMQ后台管理界面有应用机器ip，master的broker.log中查看也有对应ip。但是就是发消息失败
```

可以参考：

https://github.com/apache/rocketmq/issues/568

maybe you should close the vip channel,detail can see [Apache RocketMQ单机部署](https://liangyuanpeng.github.io/post/2018-11-27-rocketmq环境搭建/)

rocket除了9876其实还有两个端口
 10911
 10909

10909是VIP通道对应的端口，在JAVA中的消费者对象或者是生产者对象中关闭VIP通道即可无需开放10909端口

使用如下方法
 `setVipChannelEnabled(false);`
 详细的可以看看[Apache RocketMQ单机部署](https://liangyuanpeng.github.io/post/2018-11-27-rocketmq环境搭建/)

所以应当放开 9876,10911， 10909这三个端口



-----------