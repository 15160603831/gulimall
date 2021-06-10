package com.hwj.mall.product.web.controller;

/**
 * @author hwj
 */


import com.hwj.mall.product.entity.PmsCategoryEntity;
import com.hwj.mall.product.service.PmsCategoryService;

import com.hwj.mall.product.vo.Catalog2Vo;
import org.redisson.Redisson;
import org.redisson.api.RLock;
import org.redisson.api.RReadWriteLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Controller
public class IndexController {

    @Autowired
    PmsCategoryService categoryService;

    @Autowired
    RedissonClient redissonClient;

    @Autowired
    StringRedisTemplate redisTemplate;
    @Autowired
    Redisson redisson;


    @GetMapping({"/", "index.html"})
    public String indexPage(Model model) {

        List<PmsCategoryEntity> categorys = categoryService.getLevel1Catagories();
        model.addAttribute("categorys", categorys);
        return "index";
    }

    @ResponseBody
    @GetMapping("/index/json/catalog.json")
    private Map<String, List<Catalog2Vo>> getCategorylogJson() {
        Map<String, List<Catalog2Vo>> map = categoryService.getCatalogJson();
        return map;
    }

    @ResponseBody
    @GetMapping("/hello")
    private String hello() {
        //获取一把锁
        RLock lock = redissonClient.getLock("my-lock");
        //加锁
        //阻塞式等待   默认30秒自动解锁
//        lock.lock();
        lock.lock(10, TimeUnit.SECONDS);//10秒自动解锁
        try {
            System.out.println("加锁成功，执行业务" + Thread.currentThread().getId());
            Thread.sleep(30000);
        } catch (Exception e) {

        } finally {
            //解锁
            System.out.println("解锁---" + Thread.currentThread().getId());
            lock.unlock();
        }
        return "hello";
    }

    @ResponseBody
    @GetMapping("/write")
    private String writeValue() {


        RReadWriteLock lock = redisson.getReadWriteLock("rw_lock");
        RLock rLock = lock.writeLock();
        String s = "";
        try {
            //改数据加写锁，读数据加读锁
            rLock.lock();
            s = UUID.randomUUID().toString();
            Thread.sleep(30000);
            redisTemplate.opsForValue().set("writeValue", s);
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            rLock.unlock();
        }
        return s;
    }

    @ResponseBody
    @GetMapping("/read")
    private String readVale() {
        RReadWriteLock lock = redisson.getReadWriteLock("rw_lock");
        //加读锁
        RLock rLock = lock.readLock();
        rLock.lock();
        String s = "";
        try {
            s = redisTemplate.opsForValue().get("writeValue");
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            rLock.unlock();
        }
        return s;
    }


}
