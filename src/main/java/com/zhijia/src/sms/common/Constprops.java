package com.zhijia.src.sms.common;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix="constprops")
public class Constprops {
	// 发送方
	private String sendFromNumber;
	// 主账号SID
	private String accountSid;
	// 主账号TOKEN
	private String authToken;
	private String accountToken;
	// 短信应用服务ID
	private String appId;
	// 短信应用服务TOKEN
	private String appToken;

	public String getSendFromNumber() {
		return sendFromNumber;
	}

	public void setSendFromNumber(String sendFromNumber) {
		this.sendFromNumber = sendFromNumber;
	}

	public String getAccountSid() {
		return accountSid;
	}

	public void setAccountSid(String accountSid) {
		this.accountSid = accountSid;
	}

	public String getAuthToken() {
		return authToken;
	}

	public void setAuthToken(String authToken) {
		this.authToken = authToken;
	}

	public String getAccountToken() {
		return accountToken;
	}

	public void setAccountToken(String accountToken) {
		this.accountToken = accountToken;
	}

	public String getAppId() {
		return appId;
	}

	public void setAppId(String appId) {
		this.appId = appId;
	}

	public String getAppToken() {
		return appToken;
	}

	public void setAppToken(String appToken) {
		this.appToken = appToken;
	}
}
