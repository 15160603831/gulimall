package com.hwj.mall.seckill.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.hwj.common.to.mq.SeckillOrderTo;
import com.hwj.common.utils.R;
import com.hwj.common.vo.MemberEntity;
import com.hwj.mall.seckill.feign.CouponFeignService;
import com.hwj.mall.seckill.feign.ProductFeignService;
import com.hwj.mall.seckill.interceptor.LoginUserInterceptor;
import com.hwj.mall.seckill.service.SecKillService;
import com.hwj.mall.seckill.to.SeckillSkuRedisTo;
import com.hwj.mall.seckill.vo.SeckillSessionWithSkusVo;
import com.hwj.mall.seckill.vo.SkuInfoVo;
import org.redisson.api.RSemaphore;
import org.redisson.api.RedissonClient;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * @author hwj
 */
@Service
public class SeckillServiceImpl implements SecKillService {


    @Autowired
    private CouponFeignService couponFeignService;
    @Autowired
    private StringRedisTemplate redisTemplate;
    @Autowired
    private ProductFeignService productFeignService;
    @Autowired
    private RedissonClient redissonClient;
    @Autowired
    private RabbitTemplate rabbitTemplate;


    private final String SESSION_CACHE_PREFIX = "{seckill:session:}";
    private final String SKUKILL_CACHE_PREFIX = "{seckill:skus:}";
    //K: SKU_STOCK_SEMAPHORE+商品随机码
    //V: 秒杀的库存件数
    private final String SKU_STOCK_SEMAPHORE = "{seckill:stock:}";    //+商品随机码

    /**
     * 定时任务
     * 每天三点上架最近三天的秒杀商品
     */
    @Override
    public void uploadSeckillSkuLatest3Days() {

        R r = couponFeignService.getLates3DaysSession();
        if (r.getCode() == 0) {
            List<SeckillSessionWithSkusVo> sessionEntities = JSON.parseObject(JSON.toJSONString(r.get("sessionEntities")),
                    new TypeReference<List<SeckillSessionWithSkusVo>>() {
                    });
            //将活动缓存进redis中
            this.saveSessionInfos(sessionEntities);
            //活动商品信息
            this.saveSessionSkuInfos(sessionEntities);

        }

    }

    /**
     * 返回当前时间能秒杀的商品
     *
     * @return
     */
    @Override
    public List<SeckillSkuRedisTo> getCurrenrSeckillSkus() {
        //获取当前时间属于那个场次
        long time = new Date().getTime();
        Set<String> keys = redisTemplate.keys(SESSION_CACHE_PREFIX + "*");
        for (String key : keys) {
            String replace = key.replace(SESSION_CACHE_PREFIX, "");
            String[] s = replace.split("_");
            long start = Long.parseLong(s[0]);
            long end = Long.parseLong(s[1]);
            if (time > start && time < end) {
                //当前场次
                List<String> range = redisTemplate.opsForList().range(key, -100, 100);
                BoundHashOperations<String, String, String> hashOps = redisTemplate.boundHashOps(SKUKILL_CACHE_PREFIX);
                List<String> list = hashOps.multiGet(range);
                if (!CollectionUtils.isEmpty(list)) {
                    List<SeckillSkuRedisTo> collect = list.stream().map(item -> {
                        SeckillSkuRedisTo redisTo = JSON.parseObject(item, SeckillSkuRedisTo.class);
                        return redisTo;
                    }).collect(Collectors.toList());
                    return collect;
                }

                break;
            }
        }

        return null;
    }

    /**
     * 查询sku优惠信息
     *
     * @param skuId
     * @return
     */
    @Override
    public SeckillSkuRedisTo getSeckillSkuInfo(Long skuId) {
        BoundHashOperations<String, String, String> hashOps = redisTemplate.boundHashOps(SKUKILL_CACHE_PREFIX);
        Set<String> keys = hashOps.keys();
        if (!CollectionUtils.isEmpty(keys)) {
            for (String key : keys) {
                if (Pattern.matches("\\d_" + skuId, key)) {
                    String json = hashOps.get(key);
                    SeckillSkuRedisTo redisTo = JSON.parseObject(json, SeckillSkuRedisTo.class);
                    //随机码
                    if (redisTo != null) {
                        long current = System.currentTimeMillis();
                        //当前活动在有效期，暴露商品随机码返回
                        if (redisTo.getStartTime() < current && redisTo.getEndTime() > current) {
                            return redisTo;
                        }
                        redisTo.setRandomCode(null);
                        return redisTo;
                    }
                }
            }

        }
        return null;
    }

    /**
     * 立即购买
     *
     * @param killId
     * @param key
     * @param num
     * @return
     */
    @Override
    public String kill(String killId, String key, Integer num) {
        MemberEntity memberEntity = LoginUserInterceptor.loginUser.get();
        //获取当前活动
        BoundHashOperations<String, String, String> hashOps = redisTemplate.boundHashOps(SKUKILL_CACHE_PREFIX);
        String s = hashOps.get(killId);
        if (StringUtils.isEmpty(s)) {
            return null;
        } else {
            SeckillSkuRedisTo redisTo = JSON.parseObject(s, SeckillSkuRedisTo.class);
            //1.时间校验合法性
            long newTime = new Date().getTime();
            Long startTime = redisTo.getStartTime();
            Long endTime = redisTo.getEndTime();
            Long ttl = endTime - startTime;
            if (newTime >= startTime && newTime <= endTime) {
                //2.检验随机码和id
                String randomCode = redisTo.getRandomCode();
                String skuId = redisTo.getPromotionSessionId().toString() + "_" + redisTo.getSkuId().toString();
                if (randomCode.equals(key) && killId.equals(skuId)) {
                    //3：购买数量
                    if (redisTo.getSeckillLimit() >= num) {
                        //检验是否购买过 使用userId+sessionId+skuId 判断
                        String redisKey = memberEntity.getId().toString() + "_" + redisTo.getPromotionSessionId().toString() + "_" + redisTo.getSkuId();
                        //自动过期
                        Boolean aBoolean = redisTemplate.opsForValue().setIfAbsent(redisKey, num.toString(), ttl, TimeUnit.MILLISECONDS);
                        if (aBoolean) {
                            //占位成功没有买过
                            //获取信号量
                            RSemaphore semaphore = redissonClient.getSemaphore(SKU_STOCK_SEMAPHORE + redisTo.getRandomCode());
                            boolean b = semaphore.tryAcquire(num);
                            //秒杀成功
                            if (b) {
                                //5. 发送消息创建订单
                                //5.1 创建订单号
                                String timeId = IdWorker.getTimeId();
                                SeckillOrderTo orderTo = new SeckillOrderTo();
                                orderTo.setMemberId(memberEntity.getId());
                                orderTo.setNum(num);
                                orderTo.setOrderSn(timeId);
                                orderTo.setPromotionSessionId(redisTo.getPromotionSessionId());
                                orderTo.setSeckillPrice(redisTo.getSeckillPrice());
                                orderTo.setSkuId(redisTo.getSkuId());
                                //5.3 发送创建订单的消息
                                rabbitTemplate.convertAndSend("order-event-exchange", "order.seckill.order", orderTo);
                                return timeId;
                            }

                        } else {
                            //占位失败
                            return null;
                        }
                    } else {
                        return null;
                    }
                } else {
                    return null;
                }
            } else {
                return null;
            }
        }
        return null;
    }

    /**
     * 活动信息
     *
     * @param sessionEntities
     */
    private void saveSessionInfos(List<SeckillSessionWithSkusVo> sessionEntities) {
        sessionEntities.stream().forEach(session -> {
            long startTime = session.getStartTime().getTime();
            long endTime = session.getEndTime().getTime();
            String key = SESSION_CACHE_PREFIX + startTime + "_" + endTime;
            if (!redisTemplate.hasKey(key)) {
                List<String> collect = session.getRelationSkus().stream().map(sku -> sku.getPromotionSessionId().toString() + "_" + sku.getSkuId().toString()).collect(Collectors.toList());
                redisTemplate.opsForList().leftPushAll(key, collect);
            }
        });

    }

    /**
     * @param sessionEntities
     */
    private void saveSessionSkuInfos(List<SeckillSessionWithSkusVo> sessionEntities) {
        sessionEntities.stream().forEach(session -> {
            //绑定hash操作
            BoundHashOperations<String, Object, Object> hashOps = redisTemplate.boundHashOps(SKUKILL_CACHE_PREFIX);
            session.getRelationSkus().stream().forEach(skuItem -> {
                String token = UUID.randomUUID().toString().replace("-", "");

                if (!hashOps.hasKey(skuItem.getPromotionSessionId().toString() + "_" + skuItem.getSkuId().toString())) {

                    //缓存商品
                    SeckillSkuRedisTo redisTo = new SeckillSkuRedisTo();

                    //1.sku的基本信息
                    R r = productFeignService.info(skuItem.getSkuId());
                    if (r.getCode() == 0) {
                        SkuInfoVo pmsSkuInfo = JSON.parseObject(JSON.toJSONString(r.get("pmsSkuInfo")),
                                new TypeReference<SkuInfoVo>() {
                                });
                        redisTo.setSkuInfoVo(pmsSkuInfo);
                    }
                    //2.sku的秒杀信息
                    BeanUtils.copyProperties(skuItem, redisTo);
                    //3、当前商品的秒杀信息
                    redisTo.setStartTime(session.getStartTime().getTime());
                    redisTo.setEndTime(session.getEndTime().getTime());

                    //秒杀商品随机码
                    redisTo.setRandomCode(token);

                    String s = JSON.toJSONString(redisTo);
                    hashOps.put(skuItem.getPromotionSessionId().toString() + "_" + skuItem.getSkuId().toString(), s);
                    //5.使用库存作为分布式信号量  限流
                    RSemaphore semaphore = redissonClient.getSemaphore(SKU_STOCK_SEMAPHORE + token);
                    semaphore.trySetPermits(skuItem.getSeckillCount());

                }

            });
        });
    }
}
