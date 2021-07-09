package com.hwj.mall.order.config;

import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.DefaultAlipayClient;
import com.alipay.api.request.AlipayTradePagePayRequest;

import com.hwj.mall.order.vo.PayVo;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@ConfigurationProperties(prefix = "alipay")
@Component
@Data
public class AlipayTemplate {

    //在支付宝创建的应用的id
    private String app_id = "2016102900775323";

    // 商户私钥，您的PKCS8格式RSA2私钥
    private String merchant_private_key = "MIIEvQIBADANBgkqhkiG9w0BAQEFAASCBKcwggSjAgEAAoIBAQDUHhPkcouF4GaPSHgSMTp/anR9ofw6c/ivz+UNPUQpq8HimSpXdxW38yThsrAeWwm7GE1XbXgQt14FHxcCcTViAReW90gHXhhlhhIbbxANcZRuNI/8lVrIaWP8EvwBbIsRHoqaUzaeEvcX2S7nE5UknOhIB391bstb2rxJv2QlFDK/udyF/M1XrirqhNjC6Jrd4PymQTI0aHr9xL3WqJu0isq0brNiPuEycIVqi7XHp4XRn20OTDbMJoWJeuZuyktb0pAqWY8ZowuSo3CXoT3o6U9ajcF/haTn0E1MKnOVkdS3DUZhPqNUuw/IU6WAMAqTt6PzfmjdgdF53hxvyHO3AgMBAAECggEBAMDNKgWevAUO6AnatBDNID/KUmITR4EWAnhDfg3fn0JFNTEzSRJBUsNlxSNE2OJum1StDTenb9e55ELBdJeqsPwIxBlbYOQZehFloLXA6JVfpKvbKPlMBD1f1hq8YsUklO8VlKQbeOyLjn7l0MUGvqyN1nAKKKdj8oHmOvjJd0xe/GOGeKKy3xGf0WB2mY99rDrJeLqyKzEvyYzr2PDy+k9fu6EQl6ojpVkBP3VrEauYYzBEOATVwArhCk9PIYboRHtoZPaSX3GrIMlaYHuaYh2kDSK3LyTtUOG4ZFMitXJ2aVNl5stIbfP6A9qFKNgesJaSXgHTXgq/kWPLNtWQRskCgYEA9Xv/rEHzWSpmFTHniVVELg+q3IG44+FBIw7TvdjvG0OiQnfekDCEqbEtgXelVFVU3xsV6S8h3DoCLEPhmc1uV2uFcSNSTCyQxyr0gGve6Ov9SsBt/g3RJvZj14ZnsErX4NFh1tq/XguH0KC0hPTVn3ARvNSNiubUFVVrHa2MtfsCgYEA3TQs1DY32dzyvXPsOdDKLho7WnsmVeH2ABNlTO5IroE9IRIdhN6tGUBzzans686moV008VI5coBLQOYnJhtlsZg9rs9wMtlKqFRL5L5uKnoEq2upeogbK2eHBMVSgjje52NJJDxsgJX+NiYtdKiqFmdROL4/vYVfF/zf+CrRWHUCgYB5CEHudDW+6KCcIpa9p0SOUTs0rOCzugObfVv4ZlS9ZINDskYRYVqHL8v34T2qbsgBmCEdbxeKtS8NFArPAfI9N837EicpV2+qQwt5pbWpsXCYwnd4oudUBncDdEj7Kmt1dMaAYCmp/JVhz1oxk2Ufzulv6HxA174JJqxBXafasQKBgD0C0wqFGcqBYoJWouUfbWRh39UI+n3doRhMQTiC0JExHEcu0+197Yh1entTTwGLMfJ33s5gu2+VaywvTsT38YiWUQ0SMgQUYr8i4PvqekBE/1y4iTo+qB/Eojzyn9tDDFgNznKsJ9EfjcoppYfzrVYYkRJxhusjCiN3n963EJ6xAoGANBe3yd35uJthu8d/6NArYEbMaHBR6NwtmASw48NSqVyb2Wvi8fFjl9B3N4k4jeO5AlI+gI/4ft7ib4dUOwflX7f/AEZNBvr8RXaJ9CaBlk/+s1haPagoxjO/tIgUayeAdsNOfrYq/FHHro8EMrSNViOq8tspxWOBPbC0Xp1AGJ4=";
    // 支付宝公钥,查看地址：https://openhome.alipay.com/platform/keyManage.htm 对应APPID下的支付宝公钥。
    private String alipay_public_key = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAiojpehzKRiXWqSI//le3NEgs/ZmVHpfNoQBzW2EUgT8MaqQhQOp7Va6QEQb7Zx9T2bglXy3vIRmlcFvDP09nSjAVi8JJjT9dj6iwV9UMGUHp5hSuBFOwtirT/anTRugiv6/aE6CtVm6pYf79ZoS5GbYRoVJPlBO4MIqgvSzYGHU5eSutOhkhEVpPQ9j7a6iUqk/8/pTwAEvM0JXIhkUXLBshEqnk7OUna+ZJI4du2Gn/i+TYd+6xutrZyWR3VeT1DoaM/ibhxedChrX2Pbpxs2qgbJThOg/WydRDzJW7drKC0GF843yVDULoV93EgfmWsMflVzfb++1FmBmrBexDSwIDAQAB";
    // 服务器[异步通知]页面路径  需http://格式的完整路径，不能加?id=123这类自定义参数，必须外网可以正常访问
    // 支付宝会悄悄的给我们发送一个请求，告诉我们支付成功的信息
    private String notify_url = "http://nxz9r8.natappfree.cc/payed/notify";

    // 页面跳转同步通知页面路径 需http://格式的完整路径，不能加?id=123这类自定义参数，必须外网可以正常访问
    //同步通知，支付成功，一般跳转到成功页
    private String return_url = "http://order.mall.com/memberOrder.html";

    // 签名方式
    private String sign_type = "RSA2";

    // 字符编码格式
    private String charset = "utf-8";

    // 支付宝网关； https://openapi.alipaydev.com/gateway.do
    private String gatewayUrl = "https://openapi.alipaydev.com/gateway.do";

    public String pay(PayVo vo) throws AlipayApiException {

        //AlipayClient alipayClient = new DefaultAlipayClient(AlipayTemplate.gatewayUrl, AlipayTemplate.app_id, AlipayTemplate.merchant_private_key, "json", AlipayTemplate.charset, AlipayTemplate.alipay_public_key, AlipayTemplate.sign_type);
        //1、根据支付宝的配置生成一个支付客户端
        AlipayClient alipayClient = new DefaultAlipayClient(gatewayUrl,
                app_id, merchant_private_key, "json",
                charset, alipay_public_key, sign_type);

        //2、创建一个支付请求 //设置请求参数
        AlipayTradePagePayRequest alipayRequest = new AlipayTradePagePayRequest();
        alipayRequest.setReturnUrl(return_url);
        alipayRequest.setNotifyUrl(notify_url);

        //商户订单号，商户网站订单系统中唯一订单号，必填
        String out_trade_no = vo.getOut_trade_no();
        //付款金额，必填
        String total_amount = vo.getTotal_amount();
        //订单名称，必填
        String subject = vo.getSubject();
        //商品描述，可空
        String body = vo.getBody();

        alipayRequest.setBizContent("{\"out_trade_no\":\"" + out_trade_no + "\","
                + "\"total_amount\":\"" + total_amount + "\","
                + "\"subject\":\"" + subject + "\","
                + "\"body\":\"" + body + "\","
                + "\"product_code\":\"FAST_INSTANT_TRADE_PAY\"}");

        String result = alipayClient.pageExecute(alipayRequest).getBody();

        //会收到支付宝的响应，响应的是一个页面，只要浏览器显示这个页面，就会自动来到支付宝的收银台页面
        System.out.println("支付宝的响应：" + result);

        return result;

    }
}
