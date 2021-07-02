package com.hwj.mall.order;


import com.hwj.mall.order.entity.OmsOrderReturnReasonEntity;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.amqp.core.AmqpAdmin;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Date;

@RunWith(SpringRunner.class)
@SpringBootTest
@Slf4j
@ToString
public class MallOrderApplicationTests {

    @Autowired
    private AmqpAdmin amqpAdmin;
    @Autowired
    private RabbitTemplate rabbitTemplate;



    //--------------------------------------------------------------创建 exchanges交换机、 queue队列、binding绑定

    /**
     * Rabbit
     * 1、如何创建 exchanges交换机、 queue队列、binding绑定
     * 1）使用AmqpAdmin创建
     * <p>
     * 2、如何收发消息
     */
    @Test
    public void createExchange() {
        DirectExchange directExchange = new DirectExchange("hello-java-exchange", true, false);
        amqpAdmin.declareExchange(directExchange);
        log.info("exchange[{}]创建成功", "hello-java-exchange");
    }
    // 声明（创建）队列
    /**
     * 参数1：队列名称
     * 参数2：是否定义持久化队列
     * 参数3：是否独占本次连接
     * 参数4：是否在不使用的时候自动删除队列
     * 参数5：队列其它参数
     */
    @Test
    public void createQueue() {
        Queue queue = new Queue("hello-java-queue", true, false, false);
        amqpAdmin.declareQueue(queue);
        log.info("Queue[{}]创建成功", "hello-java-queue");
    }

    /**
     * 参数1：交换机名称，如果没有指定则使用默认Default Exchage
     * 参数2：路由key,简单模式可以传递队列名称
     * 参数3：消息其它属性
     * 参数4：消息内容
     */
    @Test
    public void binding() {
        Binding binding = new Binding("hello-java-queue", Binding.DestinationType.QUEUE, "hello-java-exchange",
                "hello.java", null);
        amqpAdmin.declareBinding(binding);
        log.info("binding[{}]创建成功", "hello-java-binding");
    }

    //***************************************************************收发消息
    @Test
    public void sendMessage() {
        OmsOrderReturnReasonEntity reasonEntity = new OmsOrderReturnReasonEntity();
        reasonEntity.setId(1L);
        reasonEntity.setName("打来及ad");
        reasonEntity.setStatus(2);
        reasonEntity.setCreateTime(new Date());
        String msg = "小垃圾";
        //发送消息内容是一个对象必须进行序列化或者json
        rabbitTemplate.convertAndSend("hello-java-exchange", "hello.java", reasonEntity);
        log.info("exchange[{}]消息发送成功", reasonEntity);
    }

    @Test
    public void Message() {
        OmsOrderReturnReasonEntity reasonEntity = new OmsOrderReturnReasonEntity();
        reasonEntity.setId(1L);
        reasonEntity.setName("打来及ad");
        reasonEntity.setStatus(2);
        reasonEntity.setCreateTime(new Date());
        String msg = "小垃圾";
        //发送消息内容是一个对象必须进行序列化或者json
        rabbitTemplate.convertAndSend("hello-java-exchange", "hello.java", reasonEntity);
        log.info("exchange[{}]消息发送成功", reasonEntity);
    }

}
