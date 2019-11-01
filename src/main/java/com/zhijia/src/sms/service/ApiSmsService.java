package com.zhijia.src.sms.service;

import java.util.List;

import com.zhijia.src.sms.entity.ApiSms;

public interface ApiSmsService {
	// 插入api 发送的消息（message）
	boolean insertApiSms(ApiSms apiSms);
	// 查询所有的历史消息记录
	List<ApiSms> selectAllSms();
	// 根据条件查询接口访问历史记录
	List<ApiSms> selectSmsAccessRecordByTerm(String sendTo, String statusCode, String sendTimeFrom, String sendTimeTo);
}
