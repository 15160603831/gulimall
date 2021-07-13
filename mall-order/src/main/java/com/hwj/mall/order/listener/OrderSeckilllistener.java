package com.hwj.mall.order.listener;

import com.hwj.common.to.mq.SeckillOrderTo;
import com.hwj.mall.order.service.OrderService;
import com.rabbitmq.client.Channel;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;

/**
 * 监听秒杀方法
 *
 * @author hwj
 */
@RabbitListener(queues = "order.seckill.order.queue")
public class OrderSeckilllistener {

    @Autowired
    private OrderService orderService;

    @RabbitHandler
    public void createOrder(SeckillOrderTo orderTo, Message message, Channel channel) throws IOException {
        System.out.println("***********接收到秒杀消息");
        long deliveryTag = message.getMessageProperties().getDeliveryTag();
        try {
            orderService.createSeckillOrder(orderTo);
            channel.basicAck(deliveryTag, false);
        } catch (Exception e) {
            channel.basicReject(deliveryTag,true);
        }
    }
}
