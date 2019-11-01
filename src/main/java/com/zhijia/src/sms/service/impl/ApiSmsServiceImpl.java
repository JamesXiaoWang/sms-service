package com.zhijia.src.sms.service.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.zhijia.src.sms.entity.ApiSms;
import com.zhijia.src.sms.mapper.ApiSmsMapper;
import com.zhijia.src.sms.service.ApiSmsService;

@Service
@Transactional
public class ApiSmsServiceImpl implements ApiSmsService {

	@Autowired
	private ApiSmsMapper apiSmsMapper;
	
	public List<ApiSms> selectAllSms() {
		return apiSmsMapper.selectAllSms();
	}

	public boolean insertApiSms(ApiSms apiSms) {
		boolean flag = false;
		try {
			int result = apiSmsMapper.insertApiSms(apiSms);
			if(result > 0){
				flag = true;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return flag;
	}

	public List<ApiSms> selectSmsAccessRecordByTerm(String sendTo, String statusCode, String sendTimeFrom, String sendTimeTo) {
		return apiSmsMapper.selectSmsAccessRecordByTerm(sendTo, statusCode, sendTimeFrom, sendTimeTo) ;
	}

}
