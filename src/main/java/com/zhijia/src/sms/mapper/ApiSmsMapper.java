package com.zhijia.src.sms.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.zhijia.src.sms.entity.ApiSms;

@Mapper
public interface ApiSmsMapper {
	// 插入api 发送的消息（message）
	int insertApiSms(ApiSms apiSms);

	// 查询所有的历史消息记录
	List<ApiSms> selectAllSms();

	// 根据条件查询接口访问历史记录
	List<ApiSms> selectSmsAccessRecordByTerm(
			@Param("sendTo") String sendTo, @Param("statusCode") String statusCode,
			@Param("sendTimeFrom") String sendTimeFrom, @Param("sendTimeTo") String sendTimeTo);
}
