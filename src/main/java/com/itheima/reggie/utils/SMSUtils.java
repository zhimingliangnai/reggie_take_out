package com.itheima.reggie.utils;

import com.aliyuncs.DefaultAcsClient;
import com.aliyuncs.IAcsClient;
import com.aliyuncs.dysmsapi.model.v20170525.SendSmsRequest;
import com.aliyuncs.dysmsapi.model.v20170525.SendSmsResponse;
import com.aliyuncs.exceptions.ClientException;
import com.aliyuncs.profile.DefaultProfile;

/**
 * 短信发送工具类
 */
public class SMSUtils {

	/**
	 * 发送短信
	 * @param signName 签名
	 * @param templateCode 模板
	 * @param phoneNumbers 手机号
	 * @param param 参数
	 */

	/*用户登录名称 regiee@1816668062344101.onaliyun.com
	AccessKey ID LTAI5t97kbeaMSZ7xorf3X1h
	AccessKey Secret 1hMGGXpLnkms2Q5Bq1OhBu9cwQ1Gk4*/

	public static void sendMessage(String signName, String templateCode,String phoneNumbers,String param){
		DefaultProfile profile = DefaultProfile.getProfile("cn-hangzhou", "LTAI5t97kbeaMSZ7xorf3X1h",
				"1hMGGXpLnkms2Q5Bq1OhBu9cwQ1Gk4");
		IAcsClient client = new DefaultAcsClient(profile);

			SendSmsRequest request = new SendSmsRequest();
			request.setSysRegionId("cn-hangzhou");
			request.setPhoneNumbers(phoneNumbers);
			request.setSignName(signName);
			request.setTemplateCode(templateCode);
			request.setTemplateParam("{\"code\":\""+param+"\"}");
			try {
			SendSmsResponse response = client.getAcsResponse(request);
			System.out.println("短信发送成功");
		}catch (ClientException e) {
			e.printStackTrace();
		}
	}

}
