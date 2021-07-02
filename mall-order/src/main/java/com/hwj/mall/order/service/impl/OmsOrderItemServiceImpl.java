package com.hwj.mall.order.service.impl;

import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

import java.util.Map;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hwj.common.utils.PageUtils;
import com.hwj.common.utils.Query;

import com.hwj.mall.order.dao.OmsOrderItemDao;
import com.hwj.mall.order.entity.OmsOrderItemEntity;
import com.hwj.mall.order.service.OmsOrderItemService;


@Service("omsOrderItemService")
public class OmsOrderItemServiceImpl extends ServiceImpl<OmsOrderItemDao, OmsOrderItemEntity> implements OmsOrderItemService {

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<OmsOrderItemEntity> page = this.page(
                new Query<OmsOrderItemEntity>().getPage(params),
                new QueryWrapper<OmsOrderItemEntity>()
        );

        return new PageUtils(page);
    }

    /**
     * 监听的队列
     */
    @RabbitListener(queues = {"hello-java-queue"})
    public void recieveMessage(Message message) {
        //{"id":1,"name":"打来及ad","sort":null,"status":2,"createTime":1625207213758}
        System.out.println("接收到内容" + message);
    }

}