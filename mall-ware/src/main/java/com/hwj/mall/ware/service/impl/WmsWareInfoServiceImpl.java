package com.hwj.mall.ware.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.hwj.common.utils.R;
import com.hwj.common.vo.MemberEntity;
import com.hwj.mall.ware.feign.MemberFeignService;
import com.hwj.mall.ware.vo.FareVo;
import com.hwj.mall.ware.vo.MemberAddressVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Map;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hwj.common.utils.PageUtils;
import com.hwj.common.utils.Query;

import com.hwj.mall.ware.dao.WmsWareInfoDao;
import com.hwj.mall.ware.entity.WmsWareInfoEntity;
import com.hwj.mall.ware.service.WmsWareInfoService;
import org.springframework.util.StringUtils;


@Service("wmsWareInfoService")
public class WmsWareInfoServiceImpl extends ServiceImpl<WmsWareInfoDao, WmsWareInfoEntity> implements WmsWareInfoService {

    @Autowired
    private MemberFeignService memberFeignService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        QueryWrapper<WmsWareInfoEntity> wrapper = new QueryWrapper<>();
        String key = (String) params.get("key");
        if (!StringUtils.isEmpty(key)) {
            wrapper.eq("id", key)
                    .or().like("name", key)
                    .or().like("address", key)
                    .or().like("areacode", key);
        }
        IPage<WmsWareInfoEntity> page = this.page(
                new Query<WmsWareInfoEntity>().getPage(params),
                wrapper
        );

        return new PageUtils(page);
    }

    /**
     * 根据用户的收货地址计算运费
     *
     * @param id
     * @return
     */
    @Override
    public FareVo getFare(Long id) {
        FareVo fareVo = new FareVo();
        R r = memberFeignService.info(id);
        MemberAddressVo memberAddressVo = JSON.parseObject(JSON.toJSONString(r.get("umsMemberReceiveAddress")), new TypeReference<MemberAddressVo>() {
        });
        if (memberAddressVo!=null){
            fareVo.setAddress(memberAddressVo);
            String phone = memberAddressVo.getPhone();
            String substring = phone.substring(phone.length() - 2, phone.length());
            fareVo.setFare(new BigDecimal(substring));
        }
        return fareVo;
    }

}