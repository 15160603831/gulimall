package com.hwj.mall.product.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.hwj.mall.product.vo.Catalog2Vo;
import org.redisson.Redisson;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hwj.common.utils.PageUtils;
import com.hwj.common.utils.Query;

import com.hwj.mall.product.dao.PmsCategoryDao;
import com.hwj.mall.product.entity.PmsCategoryEntity;
import com.hwj.mall.product.service.PmsCategoryService;
import org.springframework.util.StringUtils;


@Service("pmsCategoryService")
public class PmsCategoryServiceImpl extends ServiceImpl<PmsCategoryDao, PmsCategoryEntity> implements PmsCategoryService {


    @Autowired
    StringRedisTemplate redisTemplate;

    @Autowired
    RedissonClient redissonClient;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<PmsCategoryEntity> page = this.page(
                new Query<PmsCategoryEntity>().getPage(params),
                new QueryWrapper<PmsCategoryEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public List<PmsCategoryEntity> listWithTree() {
        //查出所有分类
        List<PmsCategoryEntity> pmsCategoryEntities = baseMapper.selectList(null);
        //组装成树形结构
        //2.1找到所有一级分类
        List<PmsCategoryEntity> levelMenu = pmsCategoryEntities.stream()
                .filter(t -> t.getParentCid() == 0)
                .map((menu) -> {
                    menu.setChildren(getChildren(menu, pmsCategoryEntities));
                    return menu;
                })
                .sorted(Comparator.comparingInt(sort -> (sort.getSort() == null ? 0 : sort.getSort())))
                .collect(Collectors.toList());
        return levelMenu;
    }

    /**
     * 删除商品分类
     *
     * @param catIds
     */
    @Override
    public void removeMenuByIds(List<Long> catIds) {
        baseMapper.deleteBatchIds(catIds);
    }

    /**
     * 递归查询所有菜单下的子菜单
     *
     * @param root 菜单
     * @param all  子菜单
     * @return 所有菜单
     */
    private List<PmsCategoryEntity> getChildren(PmsCategoryEntity root, List<PmsCategoryEntity> all) {
        List<PmsCategoryEntity> pmsCategoryEntities = all.stream()
                .filter(pmsCategoryEntity -> {
                    return pmsCategoryEntity.getParentCid() == root.getCatId();
                })
                .map(pmsCategoryEntity -> {
                    pmsCategoryEntity.setChildren(getChildren(pmsCategoryEntity, all));
                    return pmsCategoryEntity;
                })
                .sorted(Comparator.comparingInt(sort -> (sort.getSort() == null ? 0 : sort.getSort())))
                .collect(Collectors.toList());
        return pmsCategoryEntities;
    }

    //[2,25,225]
    @Override
    public Long[] findCatelogPath(Long catelogId) {
        List<Long> paths = new ArrayList<>();
        List<Long> parentPath = findParentPath(catelogId, paths);

        Collections.reverse(parentPath);


        return parentPath.toArray(new Long[parentPath.size()]);
    }


    //225,25,2
    private List<Long> findParentPath(Long catelogId, List<Long> paths) {
        //1、收集当前节点id
        paths.add(catelogId);
        PmsCategoryEntity byId = this.getById(catelogId);
        if (byId.getParentCid() != 0) {
            findParentPath(byId.getParentCid(), paths);
        }
        return paths;

    }

    /**
     * 查出所有1级分类
     *
     * @return
     */
    //每一个需要缓存的数据需要我们来指定名字【缓存分区（按业务类型划分】
    @Cacheable(value = {"category"},key = "#root.method.name") //代表当前方法的结果需要缓存，如果方法有，就不调用
    @Override
    public List<PmsCategoryEntity> getLevel1Catagories() {
        System.out.println("方法调用");
        List<PmsCategoryEntity> parent_cid = baseMapper.selectList(new QueryWrapper<PmsCategoryEntity>().eq("parent_cid", 0));
        return parent_cid;
    }

    @Override
    public Map<String, List<Catalog2Vo>> getCatalogJson() {
        //加入缓存
        String catalogJson = redisTemplate.opsForValue().get("catalogJson");
        if (StringUtils.isEmpty(catalogJson)) {
            //缓存中没有
            Map<String, List<Catalog2Vo>> catalogJsonFromDb = getCatalogJsonFromDb();
            String jsonString = JSON.toJSONString(catalogJsonFromDb);
            //查到的数据放入缓存
            redisTemplate.opsForValue().set("catalogJson", jsonString);
        }
        Map<String, List<Catalog2Vo>> stringListMap = JSON.parseObject(catalogJson, new TypeReference<Map<String, List<Catalog2Vo>>>() {
        });
        return stringListMap;
    }

    /**
     * 通过redis占坑来试下分布式锁
     *
     * @return
     */
    public Map<String, List<Catalog2Vo>> getCatalogJsonDbWithRedisLock() {

        //分布式加锁主要两点，加锁和删锁保证原子性
        String uuid = UUID.randomUUID().toString();
        ValueOperations<String, String> ops = redisTemplate.opsForValue();
        //占分布式锁，去redis站位
        Boolean lock = ops.setIfAbsent("lock", uuid, 5, TimeUnit.DAYS);


        if (lock) {

            Map<String, List<Catalog2Vo>> catalogJsonFromDb = getCatalogJsonFromDb();

            // 获取值对比 原子操作能删除锁
            String lockValue = ops.get("lock");
//            if (lockValue.equals(uuid)){
//                //自己的锁才能删
//                redisTemplate.delete("lock");
//            }
            //lua脚本进行删除
            String script = "if redis.call(\"get\",KEYS[1]) == ARGV[1] then\n" +
                    "    return redis.call(\"del\",KEYS[1])\n" +
                    "else\n" +
                    "    return 0\n" +
                    "end";
            redisTemplate.execute(new DefaultRedisScript<Long>(script, Long.class), Arrays.asList("lock"), lockValue);

            //枷锁成功。。。执行业务
            return catalogJsonFromDb;
        } else {
            //加锁失败  重试 synchronized

            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return getCatalogJsonDbWithRedisLock();
        }
    }

    /**
     * 通过redisson
     *
     * @return
     */
    public Map<String, List<Catalog2Vo>> getCatalogJsonDbWithRedissonLock() {


        RLock rlock = redissonClient.getLock("catalogJSON_lock");
        rlock.lock();
        Map<String, List<Catalog2Vo>> catalogJsonFromDb;
        try {
            catalogJsonFromDb = getCatalogJsonFromDb();
        } finally {
            rlock.unlock();
        }
        return catalogJsonFromDb;
    }


    /**
     * 查询三级分类
     *
     * @return
     */
    public Map<String, List<Catalog2Vo>> getCatalogJsonFromDb() {
        System.out.println("查询了数据库");
        //优化业务逻辑，仅查询一次数据库
        List<PmsCategoryEntity> entityList = baseMapper.selectList(null);
        List<PmsCategoryEntity> level1Catagories = this.getParent_cid(entityList, 0L);
        Map<String, List<Catalog2Vo>> collect = level1Catagories.stream().collect(Collectors.toMap(k -> k.getCatId().toString(), v -> {
            //// 拿到每一个一级分类 然后查询他们的二级分类
            List<PmsCategoryEntity> entities = getParent_cid(entityList, v.getCatId());
            List<Catalog2Vo> catalog2Vos = null;
            if (entities != null) {
                catalog2Vos = entities.stream().map(l2 -> {
                    Catalog2Vo catalog2Vo = new Catalog2Vo(v.getCatId().toString(), l2.getCatId().toString(), l2.getName(), null);
                    // 找当前二级分类的三级分类
                    List<PmsCategoryEntity> entities1 = getParent_cid(entityList, l2.getCatId());
                    // 三级分类有数据的情况下
                    if (entities1 != null) {
                        List<Catalog2Vo.Catalog3Vo> catalog3Vo = entities1.stream().map(l3 -> new Catalog2Vo.Catalog3Vo(l3.getCatId().toString(), l2.getCatId().toString(), l3.getName())).collect(Collectors.toList());
                        catalog2Vo.setCatalog3List(catalog3Vo);
                    }
                    return catalog2Vo;
                }).collect(Collectors.toList());
            }
            return catalog2Vos;
        }));
        return collect;
    }

    private List<PmsCategoryEntity> getParent_cid(List<PmsCategoryEntity> entityList, Long parentCid) {
        List<PmsCategoryEntity> collect = entityList.stream().filter(item -> parentCid.equals(item.getParentCid())).collect(Collectors.toList());
        return collect;
    }


}