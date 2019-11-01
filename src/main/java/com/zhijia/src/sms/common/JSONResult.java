package com.zhijia.src.sms.common;

import java.io.Serializable;

public class JSONResult implements Serializable {
	
	private static final long serialVersionUID = 6907496066227611719L;
	private static final String SUCCESS = "SUCCESS";
	private static final String ERROR = "ERROR";
	
	// 返回的状态
	private String status;
	// 返回的错误消息
	private String msg;
	// 返回的具体数据
	private Object data;

	public JSONResult() {
		
    }
    
    public JSONResult(String error){
        status = ERROR;
        this.msg = error;
    }
    
    public JSONResult(Object data){
        status = SUCCESS;
        this.data = data;
    }
    
    public JSONResult(Throwable e) {
        status = ERROR;
        msg = e.getMessage();
    }
    
    public JSONResult(String status, Throwable e) {
        this.status = status;
        this.msg = e.getMessage();
    }
	
	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getMsg() {
		return msg;
	}

	public void setMsg(String msg) {
		this.msg = msg;
	}

	public Object getData() {
		return data;
	}

	public void setData(Object data) {
		this.data = data;
	}

	@Override
	public String toString() {
		return "{status=" + status + ", msg=" + msg + ", data=" + data + "}";
	}

	public JSONResult(String status, String msg, Object data) {
		super();
		this.status = status;
		this.msg = msg;
		this.data = data;
	}

}
