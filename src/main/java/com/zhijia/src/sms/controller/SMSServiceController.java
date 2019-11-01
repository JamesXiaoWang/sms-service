package com.zhijia.src.sms.controller;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.zhijia.src.sms.common.ConstantUtil;
import com.zhijia.src.sms.common.Constprops;
import com.zhijia.src.sms.common.JSONResult;
import com.zhijia.src.sms.common.RestSDK;
import com.zhijia.src.sms.entity.ApiSms;
import com.zhijia.src.sms.service.ApiSmsService;

@Controller
@RequestMapping(value = "/sms")
public class SMSServiceController  {
	private static final Logger logger = LoggerFactory.getLogger(SMSServiceController.class);
	@Autowired
	private Constprops constProps;
	
	@Autowired
	private ApiSmsService apiSmsService;
	
	@SuppressWarnings("unused")
	@ResponseBody
	@RequestMapping(value="/sendVerifyCode", produces = {"application/json;charset=UTF-8"})
	public JSONResult sendVerifyCode(HttpServletRequest request, HttpServletResponse response){
		try {
			// 接收方的手机号码
			String sendTo = request.getParameter("sendTo");		
			
			// smsType： 注册（REGISTER）、登录（LOGIN）、其它（OTHER），用于区分发送短信的模板
			String smsType = request.getParameter("smsType");
			/**
			 * 当smsType为OTHER时，verifyCode、expireTime可以为空
			 * 当smsType不为OTHER时，verifyCode、expireTime不可以为空
			 */
			// 验证码
			String verifyCode = request.getParameter("verifyCode");
			// 有效时间（单位：s）
			String expireTime = request.getParameter("expireTime");
			
			if(sendTo == null || "".equals(sendTo)){
				return new JSONResult(ConstantUtil.ERROR, "请求失败, {#错误信息: sendTo不能为空#}", null);				
			}
			if(smsType == null || "".equals(smsType)){
				return new JSONResult(ConstantUtil.ERROR, "请求失败, {#错误信息: smsType值为空#}", null);				
			}else{
				if(verifyCode == null || "".equals(verifyCode)){
					return new JSONResult(ConstantUtil.ERROR, "请求失败, {#错误信息: verifyCode值为空#}", null);				
				}
				HashMap<String, Object> result = null;
				RestSDK restAPI = new RestSDK();
				/*
				 * 测试模板1，只能使用未上线状态的应用进行测试；测试没问题后，你就可以申请自己的短信模板并正式使用了。
				 */
				if("OTHER".equals(smsType)){
					result = restAPI.sendTemplateSMS(constProps, sendTo, "#", request.getMethod(), new String[]{});
				}else{
					result = restAPI.sendTemplateSMS(constProps, sendTo, "398502", request.getMethod(), new String[]{verifyCode, expireTime});
				}
				
				ApiSms apisms = new ApiSms();
				apisms.setSendFrom(constProps.getSendFromNumber());
				apisms.setSendTo(sendTo);
				apisms.setSendTime(new SimpleDateFormat("yyyy-MM-dd").format(new Date()));
				apisms.setStatusCode(result.get("statusCode")+"");
				
				Map<String, String> mapObj = new HashMap<String, String>();
				if("000000".equals(result.get("statusCode"))){
					//正常返回输出data包体信息（map）
					HashMap<String,Object> data = (HashMap<String, Object>) result.get("data");
					
					mapObj.put("statusCode", "000000");
					mapObj.put("msg", "发送成功");
					
					apisms.setContent("成功！");
					apisms.setResult(new JSONResult(ConstantUtil.SUCCESS, "请求成功", mapObj).toString());
					apiSmsService.insertApiSms(apisms);
					
					return new JSONResult(ConstantUtil.SUCCESS, "请求成功", mapObj);
				}else{
					
					mapObj.put("statusCode", result.get("statusCode")+"");
					mapObj.put("msg", result.get("statusMsg")+"");
					
					//异常返回输出错误码和错误信息
					apisms.setContent("");
					apisms.setResult(new JSONResult(ConstantUtil.ERROR, "请求失败, {#错误码:" + result.get("statusCode") +" 错误信息: "+result.get("statusMsg") + "#}", mapObj).toString());
					apiSmsService.insertApiSms(apisms);
					
					return new JSONResult(ConstantUtil.ERROR, "请求失败, {#错误码:" + result.get("statusCode") +" 错误信息: "+result.get("statusMsg") + "#}", mapObj);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			return new JSONResult(ConstantUtil.ERROR, "请求失败, {#错误信息:" + e.getMessage() + "#}", "");
		}
	}
	
	@ResponseBody
	@RequestMapping(value="/searchRecord", produces = {"application/json;charset=UTF-8"})
	public JSONResult searchRecord(HttpServletRequest request, HttpServletResponse response){
		logger.info("访问接口历史访问记录。。。");
		// 接收方的手机号码
		String sendTo = request.getParameter("sendTo");
		// 短信发送的状态码
		String statusCode = request.getParameter("statusCode");
		// 发送开始时间
		String sendTimeFrom = request.getParameter("sendTimeFrom");
		// 发送结束时间
		String sendTimeTo = request.getParameter("sendTimeTo");
		logger.info("接收方的手机号码: " + sendTo + ", 短信发送的状态码: " + statusCode + ", 发送时间: " + sendTimeFrom + "-" + sendTimeTo);
		try {
			// List<ApiSms> apismsList = apiSmsService.selectAllSms();
			List<ApiSms> apismsList = apiSmsService.selectSmsAccessRecordByTerm(sendTo, statusCode, sendTimeFrom, sendTimeTo);
			if(apismsList != null && apismsList.size() > 0){
				return new JSONResult(ConstantUtil.SUCCESS, "请求成功", apismsList);
			}else{
				return new JSONResult(ConstantUtil.SUCCESS, "请求成功", "返回数据为空！");
			}
		} catch (Exception e) {
			e.printStackTrace();
			return new JSONResult(ConstantUtil.ERROR, "请求失败, {#错误信息:" + e.getMessage() + "#}", "");
		}
	}
}
