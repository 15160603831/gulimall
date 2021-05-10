import http from '@/utils/httpRequest.js'
// 获取签名
export function policy() {
   return  new Promise((resolve,reject)=>{
        http({
            // 先去获取签名
            url: http.adornUrl("/thirdparty/oss/policy"),
            method: "get",
            params: http.adornParams({})
        }).then(({ data }) => {
            resolve(data);
        })
    });
}