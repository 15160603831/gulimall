//package com.hwj.mall.thirdparty;
//
//
//import com.aliyun.credentials.http.MethodType;
//import com.aliyun.dysmsapi20170525.Client;
//import com.aliyun.dysmsapi20170525.models.SendSmsRequest;
//import com.aliyun.dysmsapi20170525.models.SendSmsResponse;
//import com.aliyun.oss.OSS;
//import com.aliyun.oss.OSSClient;
//import com.aliyun.teaopenapi.models.Config;
//import com.aliyuncs.CommonRequest;
//import com.aliyuncs.CommonResponse;
//import com.aliyuncs.DefaultAcsClient;
//import com.aliyuncs.IAcsClient;
//import com.aliyuncs.exceptions.ClientException;
//import com.aliyuncs.exceptions.ServerException;
//import com.aliyuncs.profile.DefaultProfile;
//
//import io.prometheus.client.Collector;
//import org.junit.Test;
//import org.junit.runner.RunWith;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.context.SpringBootTest;
//import org.springframework.test.context.junit4.SpringRunner;
//
//import java.io.FileInputStream;
//import java.io.FileNotFoundException;
//import java.io.InputStream;
//
//@SpringBootTest
//@RunWith(SpringRunner.class)
//public class MallThirdPartyApplicationTests {
//
//    @Autowired
//    private OSS ossClient;
//
//
//
//    @Test
//    public void testUploads() throws FileNotFoundException {
////        // yourEndpoint填写Bucket所在地域对应的Endpoint。以华东1（杭州）为例，Endpoint填写为https://oss-cn-hangzhou.aliyuncs.com。
////        String endpoint = "oss-cn-hongkong.aliyuncs.com";
////        // 阿里云账号AccessKey拥有所有API的访问权限，风险很高。强烈建议您创建并使用RAM用户进行API访问或日常运维，请登录RAM控制台创建RAM用户。
////        String accessKeyId = "LTAI5tDFqJrcdy1hT4k2v5rg";
////        String accessKeySecret = "9Wr1gtZeLkGvOQgJZTWb5cydwhgedy";
//
//        // 创建OSSClient实例。
////        OSS ossClient = new OSSClientBuilder().build(endpoint, accessKeyId, accessKeySecret);
//
//        // 填写本地文件的完整路径。如果未指定本地路径，则默认从示例程序所属项目对应本地路径中上传文件流。
//        InputStream inputStream = new FileInputStream("C:\\Users\\hwj\\Pictures\\Saved Pictures\\3bffcdaac1bcea1028992e95b7717cd4_t.gif");
//        // 填写Bucket名称和Object完整路径。Object完整路径中不能包含Bucket名称。
//        ossClient.putObject("hwj-mall", "3bffcdaac1.gif", inputStream);
//
//        // 关闭OSSClient。
//        ossClient.shutdown();
//        System.out.println("上传完成");
//    }
//
//
//
//    @Test
//    public void sendSms() throws Exception {
////
////        com.aliyun.dysmsapi20170525.Client client = SmsController.createClient();
////        SendSmsRequest sendSmsRequest = new SendSmsRequest()
////                .setPhoneNumbers("15160603831")
////                .setSignName("传一家政服务")
////                .setTemplateCode("SMS_192985049")
////                .setTemplateParam("{\"code\":123455}");
////        // 复制代码运行请自行打印 API 的返回值
////        SendSmsResponse sendSmsResponse = client.sendSms(sendSmsRequest);
////        System.out.println(sendSmsResponse.getBody().code);
////
//
//    }
//
//
//}
