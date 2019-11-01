package com.zhijia.src.sms.common;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.BasicHttpEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import org.apache.http.client.methods.HttpGet;

import org.apache.http.client.methods.HttpRequestBase;

public class RestSDK {
	public static final String REST_URL = "https://app.cloopen.com:8883";
	
	int status;
	private static final String Request_Get = "GET";

	private static final String Request_Post = "POST";
	// 短信服务IP
	public static final String SERVER_IP = "app.cloopen.com";
	// 短信服务端口
	public static final String SERVER_PORT = "8883";
	
	private String SUBACCOUNT_SID;
	private String SUBACCOUNT_Token;
	private BodyType BODY_TYPE = BodyType.Type_XML;
	
	private static final String TemplateSMS = "SMS/TemplateSMS";
	private static final String Query_SMSTemplate = "SMS/QuerySMSTemplate";
	
	public enum BodyType {
		Type_XML, Type_JSON;
	}

	public enum AccountType {
		Accounts, SubAccounts;
	}
	

	/**
	 * 初始化子帐号信息
	 * 
	 * @param subAccountSid
	 *            必选参数 子帐号
	 * @param subAccountToken
	 *            必选参数 子帐号TOKEN
	 */
	public void setSubAccount(String subAccountSid, String subAccountToken) {
		if (isEmpty(subAccountSid) || isEmpty(subAccountToken)) {
			System.out.println("初始化异常:subAccountSid或subAccountToken为空");
			throw new IllegalArgumentException("必选参数:"
					+ (isEmpty(subAccountSid) ? " 子帐号" : "")
					+ (isEmpty(subAccountToken) ? " 子帐号TOKEN " : "") + "为空");
		}
		SUBACCOUNT_SID = subAccountSid;
		SUBACCOUNT_Token = subAccountToken;
	}

	/**
	 * 发送短信模板请求
	 * 
	 * @param to
	 *            必选参数 短信接收端手机号码集合，用英文逗号分开，每批发送的手机号数量不得超过100个
	 * @param templateId
	 *            必选参数 模板Id
	 * @param datas
	 *            可选参数 内容数据，用于替换模板中{序号}
	 * @return
	 */
	public HashMap<String, Object> sendTemplateSMS(Constprops constprops, String to, String templateId, String method, String[] datas) {
		HashMap<String, Object> validate = accountValidate(constprops);
		if (validate != null)
			return validate;
		if ((isEmpty(to)) || (isEmpty(constprops.getAppId())) || (isEmpty(templateId)))
			throw new IllegalArgumentException("必选参数:" + (isEmpty(to) ? " 手机号码 " : "")
					+ (isEmpty(templateId) ? " 模板Id " : "") + "为空");
		HttpClientUtil chc = new HttpClientUtil();
		DefaultHttpClient httpclient = null;
		try {
			httpclient = chc.registerSSL(SERVER_IP, "TLS", Integer.parseInt(SERVER_PORT), "https");
		} catch (Exception e1) {
			e1.printStackTrace();
			throw new RuntimeException("初始化httpclient异常" + e1.getMessage());
		}
		String result = "";
		
		try {
			HttpPost httppost = (HttpPost) getHttpRequestBase(constprops, method, TemplateSMS);
			String requsetbody = "";
			if (BODY_TYPE == BodyType.Type_JSON) {
				JsonObject json = new JsonObject();
				json.addProperty("appId", constprops.getAppId());
				json.addProperty("to", to);
				json.addProperty("templateId", templateId);
				if (datas != null) {
					StringBuilder sb = new StringBuilder("[");
					for (String s : datas) {
						sb.append("\"" + s + "\"" + ",");
					}
					sb.replace(sb.length() - 1, sb.length(), "]");
					JsonParser parser = new JsonParser();
					JsonArray Jarray = parser.parse(sb.toString())
							.getAsJsonArray();
					json.add("datas", Jarray);
				}
				requsetbody = json.toString();
			} else {
				StringBuilder sb = new StringBuilder("<?xml version='1.0' encoding='utf-8'?><TemplateSMS>");
				sb.append("<appId>").append(constprops.getAppId()).append("</appId>")
						.append("<to>").append(to).append("</to>")
						.append("<templateId>").append(templateId)
						.append("</templateId>");
				if (datas != null) {
					sb.append("<datas>");
					for (String s : datas) {
						sb.append("<data>").append(s).append("</data>");
					}
					sb.append("</datas>");
				}
				sb.append("</TemplateSMS>").toString();
				requsetbody = sb.toString();
			}
			//打印包体
			System.out.println("请求的包体："+requsetbody);
			System.out.println("sendTemplateSMS Request body =  " + requsetbody);
			BasicHttpEntity requestBody = new BasicHttpEntity();
			requestBody.setContent(new ByteArrayInputStream(requsetbody.getBytes("UTF-8")));
			requestBody.setContentLength(requsetbody.getBytes("UTF-8").length);
			httppost.setEntity(requestBody);
			
			HttpResponse response = httpclient.execute(httppost);			
			
			//获取响应码
			
			status = response.getStatusLine().getStatusCode();
			
			System.out.println("Https请求返回状态码："+status);

			HttpEntity entity = response.getEntity();
			if (entity != null)
				result = EntityUtils.toString(entity, "UTF-8");

			EntityUtils.consume(entity);
		} catch (IOException e) {
			e.printStackTrace();
			System.out.println(e.getMessage());
			return getMyError("172001", "网络错误"+"Https请求返回码："+status);
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println(e.getMessage());
			return getMyError("172002", "无返回");
		} finally {
			if (httpclient != null)
				httpclient.getConnectionManager().shutdown();
		}

		System.out.println("sendTemplateSMS response body = " + result);

		try {
			if (BODY_TYPE == BodyType.Type_JSON) {
				return jsonToMap(result);
			} else {
				return xmlToMap(result);
			}
		} catch (Exception e) {

			return getMyError("172003", "返回包体错误");
		}
	}
	
	private HashMap<String, Object> jsonToMap(String result) {
		HashMap<String, Object> hashMap = new HashMap<String, Object>();
		JsonParser parser = new JsonParser();
		JsonObject asJsonObject = parser.parse(result).getAsJsonObject();
		Set<Entry<String, JsonElement>> entrySet = asJsonObject.entrySet();
		HashMap<String, Object> hashMap2 = new HashMap<String, Object>();

		for (Map.Entry<String, JsonElement> m : entrySet) {
			if ("statusCode".equals(m.getKey())
					|| "statusMsg".equals(m.getKey()))
				hashMap.put(m.getKey(), m.getValue().getAsString());
			else {
				if ("SubAccount".equals(m.getKey())
						|| "totalCount".equals(m.getKey())
						|| "smsTemplateList".equals(m.getKey())
						|| "token".equals(m.getKey())
						|| "callSid".equals(m.getKey())
						|| "state".equals(m.getKey())
						|| "downUrl".equals(m.getKey())) {
					if (!"SubAccount".equals(m.getKey())
							&& !"smsTemplateList".equals(m.getKey()))
						hashMap2.put(m.getKey(), m.getValue().getAsString());
					else {
						try {
							if ((m.getValue().toString().trim().length() <= 2)
									&& !m.getValue().toString().contains("[")) {
								hashMap2.put(m.getKey(), m.getValue()
										.getAsString());
								hashMap.put("data", hashMap2);
								break;
							}
							if (m.getValue().toString().contains("[]")) {
								hashMap2.put(m.getKey(), new JsonArray());
								hashMap.put("data", hashMap2);
								continue;
							}
							JsonArray asJsonArray = parser.parse(
									m.getValue().toString()).getAsJsonArray();
							ArrayList<HashMap<String, Object>> arrayList = new ArrayList<HashMap<String, Object>>();
							for (JsonElement j : asJsonArray) {
								Set<Entry<String, JsonElement>> entrySet2 = j
										.getAsJsonObject().entrySet();
								HashMap<String, Object> hashMap3 = new HashMap<String, Object>();
								for (Map.Entry<String, JsonElement> m2 : entrySet2) {
									hashMap3.put(m2.getKey(), m2.getValue()
											.getAsString());
								}
								arrayList.add(hashMap3);
							}
							hashMap2.put(m.getKey(), arrayList);
						} catch (Exception e) {
							JsonObject asJsonObject2 = parser.parse(
									m.getValue().toString()).getAsJsonObject();
							Set<Entry<String, JsonElement>> entrySet2 = asJsonObject2
									.entrySet();
							HashMap<String, Object> hashMap3 = new HashMap<String, Object>();
							for (Map.Entry<String, JsonElement> m2 : entrySet2) {
								hashMap3.put(m2.getKey(), m2.getValue()
										.getAsString());
							}
							hashMap2.put(m.getKey(), hashMap3);
							hashMap.put("data", hashMap2);
						}

					}
					hashMap.put("data", hashMap2);
				} else {

					JsonObject asJsonObject2 = parser.parse(
							m.getValue().toString()).getAsJsonObject();
					Set<Entry<String, JsonElement>> entrySet2 = asJsonObject2
							.entrySet();
					HashMap<String, Object> hashMap3 = new HashMap<String, Object>();
					for (Map.Entry<String, JsonElement> m2 : entrySet2) {
						hashMap3.put(m2.getKey(), m2.getValue().getAsString());
					}
					if (hashMap3.size() != 0) {
						hashMap2.put(m.getKey(), hashMap3);
					} else {
						hashMap2.put(m.getKey(), m.getValue().getAsString());
					}
					hashMap.put("data", hashMap2);
				}
			}
		}
		return hashMap;
	}
	
	/**
	 * 短信模板查询
	 * 
	 * @param templateId
	 *            可选参数 模板Id，不带此参数查询全部可用模板
	 * @return
	 */
	public HashMap<String, Object> QuerySMSTemplate(Constprops constprops, String templateId) {
		HashMap<String, Object> validate = accountValidate(constprops);
		if (validate != null)
			return validate;

		HttpClientUtil chc = new HttpClientUtil();
		DefaultHttpClient httpclient = null;
		try {
			httpclient = chc.registerSSL(SERVER_IP, "TLS",
					Integer.parseInt(SERVER_PORT), "https");
		} catch (Exception e1) {
			e1.printStackTrace();
			System.out.println(e1.getMessage());
			throw new RuntimeException("初始化httpclient异常" + e1.getMessage());
		}
		String result = "";
		try {
			HttpPost httppost = (HttpPost) getHttpRequestBase(constprops, "POST", Query_SMSTemplate);
			String requsetbody = "";

			if (BODY_TYPE == BodyType.Type_JSON) {
				JsonObject json = new JsonObject();
				json.addProperty("appId", constprops.getAppId());
				json.addProperty("templateId", templateId);
				requsetbody = json.toString();
			} else {
				requsetbody = "<?xml version='1.0' encoding='utf-8'?><Request>"
						+ "<appId>" + constprops.getAppId() + "</appId>" + "<templateId>"
						+ templateId + "</templateId>" + "</Request>";
			}
			System.out.println("QuerySMSTemplate Request body =  " + requsetbody);
			//打印包体
			System.out.println("请求的包体："+requsetbody);
			BasicHttpEntity requestBody = new BasicHttpEntity();
			requestBody.setContent(new ByteArrayInputStream(requsetbody
					.getBytes("UTF-8")));
			requestBody.setContentLength(requsetbody.getBytes("UTF-8").length);
			httppost.setEntity(requestBody);

			HttpResponse response = httpclient.execute(httppost);
		
			//获取响应码
			
			status = response.getStatusLine().getStatusCode();
			
			System.out.println("Https请求返回状态码："+status);

			HttpEntity entity = response.getEntity();

			if (entity != null) {
				result = EntityUtils.toString(entity, "UTF-8");
			}

			EntityUtils.consume(entity);
		} catch (IOException e) {
			e.printStackTrace();
			System.out.println(e.getMessage());
			return getMyError("172001", "网络错误");
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println(e.getMessage());
			return getMyError("172002", "无返回");
		} finally {
			if (httpclient != null)
				httpclient.getConnectionManager().shutdown();
		}
		System.out.println("QuerySMSTemplate response body = " + result);
		try {
			if (BODY_TYPE == BodyType.Type_JSON) {
				return jsonToMap(result);
			} else {
				return xmlToMap(result);
			}
		} catch (Exception e) {

			return getMyError("172003", "返回包体错误");
		}
	}

	/**
	 * @description 将xml字符串转换成map
	 * @param xml
	 * @return Map
	 */
	@SuppressWarnings("rawtypes")
	private HashMap<String, Object> xmlToMap(String xml) {
		HashMap<String, Object> map = new HashMap<String, Object>();
		Document doc = null;
		try {
			doc = DocumentHelper.parseText(xml); // 将字符串转为XML
			Element rootElt = doc.getRootElement(); // 获取根节点
			HashMap<String, Object> hashMap2 = new HashMap<String, Object>();
			ArrayList<HashMap<String, Object>> arrayList = new ArrayList<HashMap<String, Object>>();
			for (Iterator i = rootElt.elementIterator(); i.hasNext();) {
				Element e = (Element) i.next();
				if ("statusCode".equals(e.getName()) || "statusMsg".equals(e.getName()))
					map.put(e.getName(), e.getText());
				else {
					if ("SubAccount".equals(e.getName()) || "TemplateSMS".equals(e.getName()) || "totalCount".equals(e.getName())
							|| "token".equals(e.getName()) || "callSid".equals(e.getName()) || "state".equals(e.getName()) || "downUrl".equals(e.getName())) {
						if (!"SubAccount".equals(e.getName())&&!"TemplateSMS".equals(e.getName())) {
							hashMap2.put(e.getName(), e.getText());
						} else if ("SubAccount".equals(e.getName())){
							HashMap<String, Object> hashMap3 = new HashMap<String, Object>();
							for (Iterator i2 = e.elementIterator(); i2.hasNext();) {
								Element e2 = (Element) i2.next();
								hashMap3.put(e2.getName(), e2.getText());
							}
							arrayList.add(hashMap3);
							hashMap2.put("SubAccount", arrayList);
						}else if ("TemplateSMS".equals(e.getName())){

							HashMap<String, Object> hashMap3 = new HashMap<String, Object>();
							for (Iterator i2 = e.elementIterator(); i2.hasNext();) {
								Element e2 = (Element) i2.next();
								hashMap3.put(e2.getName(), e2.getText());
							}
							arrayList.add(hashMap3);
							hashMap2.put("TemplateSMS", arrayList);
						}
						map.put("data", hashMap2);
					} else {
						HashMap<String, Object> hashMap3 = new HashMap<String, Object>();
						for (Iterator i2 = e.elementIterator(); i2.hasNext();) {
							Element e2 = (Element) i2.next();
							hashMap3.put(e2.getName(), e2.getText());
						}
						if (hashMap3.size() != 0) {
							hashMap2.put(e.getName(), hashMap3);
						} else {
							hashMap2.put(e.getName(), e.getText());
						}
						map.put("data", hashMap2);
					}
				}
			}
		} catch (DocumentException e) {
			e.printStackTrace();
			System.out.println(e.getMessage());
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
		}
		return map;
	}
	
	private boolean isEmpty(String str) {
		return (("".equals(str)) || (str == null));
	}

	private HashMap<String, Object> getMyError(String code, String msg) {
		HashMap<String, Object> hashMap = new HashMap<String, Object>();
		hashMap.put("statusCode", code);
		hashMap.put("statusMsg", msg);
		return hashMap;
	}
	
	private HashMap<String, Object> accountValidate(Constprops constprops) {
		if ((isEmpty(SERVER_IP))) {
			return getMyError("172004", "IP为空");
		}
		if ((isEmpty(SERVER_PORT))) {
			return getMyError("172005", "端口错误");
		}
		if ((isEmpty(constprops.getAccountSid()))) {
			return getMyError("172006", "主帐号为空");
		}
		if ((isEmpty(constprops.getAccountToken()))) {
			return getMyError("172007", "主帐号TOKEN为空");
		}
		if ((isEmpty(constprops.getAppId()))) {
			return getMyError("172012", "应用ID为空");
		}
		return null;
	}

	@SuppressWarnings("unused")
	private void setBodyType(BodyType bodyType) {
		BODY_TYPE = bodyType;
	}
	
	private void setHttpHeader(HttpRequestBase httpMessage) {
		if (BODY_TYPE == BodyType.Type_JSON) {
			httpMessage.setHeader("Accept", "application/json");
			httpMessage.setHeader("Content-Type", "application/json;charset=utf-8");
		} else {
			httpMessage.setHeader("Accept", "application/xml");
			httpMessage.setHeader("Content-Type", "application/xml;charset=utf-8");
		}
	}

	private StringBuffer getBaseUrl() {
		StringBuffer sb = new StringBuffer("https://");
		sb.append(SERVER_IP).append(":").append(SERVER_PORT);
		sb.append("/2013-12-26");
		return sb;
	}
	
	private HttpRequestBase getHttpRequestBase(Constprops constprops, String get, String action) throws NoSuchAlgorithmException, UnsupportedEncodingException {
		return getHttpRequestBase(constprops, get, action, AccountType.Accounts);
	}

	private HttpRequestBase getHttpRequestBase(Constprops constprops, String get, String action,AccountType mAccountType) throws NoSuchAlgorithmException, UnsupportedEncodingException {
		String timestamp = DateUtil.dateToStr(new Date(), DateUtil.DATE_TIME_NO_SLASH);
		EncryptUtil eu = new EncryptUtil();
		String sig = "";
		String acountName = "";
		String acountType = "";
		if (mAccountType == AccountType.Accounts) {
			acountName = constprops.getAccountSid();
			sig = constprops.getAccountSid() + constprops.getAccountToken() + timestamp;
			acountType = "Accounts";
		} else {
			acountName = SUBACCOUNT_SID;
			sig = SUBACCOUNT_SID + SUBACCOUNT_Token + timestamp;
			acountType = "SubAccounts";
		}
		
		String signature = eu.md5Digest(sig);

		String url = getBaseUrl().append("/" + acountType + "/")
				.append(acountName).append("/" + action + "?sig=")
				.append(signature).toString();
		
		HttpRequestBase mHttpRequestBase = null;
		if (Request_Get.equals(get)){
			System.out.println("*********get*********" + url);
			mHttpRequestBase = new HttpGet(url);
		} else if (Request_Post.equals(get)){
			System.out.println("#########get*********" + url);
			mHttpRequestBase = new HttpPost(url);
		}
		System.out.println("mHttpRequestBase:" + mHttpRequestBase);
		
		setHttpHeader(mHttpRequestBase);

		String src = acountName + ":" + timestamp;

		String auth = eu.base64Encoder(src);
		mHttpRequestBase.setHeader("Authorization", auth);
		System.out.println("请求的Url："+mHttpRequestBase);//打印Url
		return mHttpRequestBase;
		
	}
}
