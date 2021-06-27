package com.hwj.mall.thirdparty.controller;

import com.aliyun.dysmsapi20170525.Client;
import com.aliyun.dysmsapi20170525.models.SendSmsRequest;
import com.aliyun.dysmsapi20170525.models.SendSmsResponse;
import com.aliyun.teaopenapi.models.Config;
import com.hwj.common.utils.R;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/sms")
public class SmsController {

    @Value("${spring.cloud.alicloud.sms.sms-access-key}")
    String accessId;
    @Value("${spring.cloud.alicloud.sms.sms-secret-key}")
    String accessKey;
    @Value("${spring.cloud.alicloud.sms.endpoint}")
    String endpoint;
    @Value("${spring.cloud.alicloud.sms.sign-name}")
    String signName;
    @Value("${spring.cloud.alicloud.sms.template-code}")
    String templateCode;




    @GetMapping("/sendSms/{phone}/{code}")
    @ApiOperation("短信服务")
    public R sendSms(@PathVariable String phone, @PathVariable String code) throws Exception {
        com.aliyun.dysmsapi20170525.Client client = this.createClient();
        SendSmsRequest sendSmsRequest = new SendSmsRequest()
                .setSignName(signName)
                .setTemplateCode(templateCode)
                .setPhoneNumbers(phone)
                .setTemplateParam("{\"code\":" + code + "}");
        // 复制代码运行请自行打印 API 的返回值
        SendSmsResponse sendSmsResponse = client.sendSms(sendSmsRequest);
        return R.ok().put("code",sendSmsResponse.getBody().code);
    }


    public Client createClient() throws Exception {
        Config config = new Config()
                // 您的AccessKey ID
                .setAccessKeyId(accessId)
                // 您的AccessKey Secret
                .setAccessKeySecret(accessKey);
        // 访问的域名
        config.endpoint = endpoint;
        return new Client(config);
    }


}
