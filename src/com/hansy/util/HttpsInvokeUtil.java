package com.hansy.util;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.BufferedHttpEntity;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

/**
 * https工具类
 * 
 * @author Furious
 *
 */
public final class HttpsInvokeUtil {

	// 请求超时时间
	private static final int REQ_TIMEOUT = 30 * 1000;
	// 响应超时时间
	private static final int RESP_TIMEOUT = 30 * 1000;
	// 状态
	private static final String STATUS = "status";
	// 响应体
	private static final String RESP_BODY = "respbody";

	private static CloseableHttpClient httpclient = null;
	private static RequestConfig config = null;
	private static HttpGet httpGet = null;
	private static HttpPost httpPost = null;
	private static CloseableHttpResponse response = null;

	static {
		httpclient = DefaultSSLClient.createSSLClient();
		// httpclient = HttpClients.createDefault();

		config = RequestConfig.custom()//
				.setConnectTimeout(REQ_TIMEOUT)//
				.setSocketTimeout(RESP_TIMEOUT)//
				.build();
	}

	/**
	 * get请求
	 * 
	 * @param url
	 *            请求URL
	 * @param params
	 *            请求参数
	 * @return
	 */
	public static Map<String, Object> sendGet(String url, Map<String, String> params) {
		Map<String, Object> rsMap = new HashMap<String, Object>();
		// 参数不为空时，转换map参数为get请求参数格式
		if (params != null && params.size() > 0) {
			StringBuilder sb = new StringBuilder();
			for (String key : params.keySet()) {
				sb.append("&");
				sb.append(key);
				sb.append("=");
				sb.append(params.get(key));
			}
			if (url.contains("?")) {
				url = url + sb.toString();
			} else {
				url = url + "?" + sb.toString().substring(1);
			}
		}
		httpGet = new HttpGet(url);
		httpGet.setConfig(config);
		try {
			// 发送请求
			response = httpclient.execute(httpGet);
			HttpEntity entity = response.getEntity();
			String result = EntityUtils.toString(entity, "utf-8");
			rsMap.put(STATUS, response.getStatusLine().getStatusCode());
			rsMap.put(RESP_BODY, result);
		} catch (Exception e) {
			e.printStackTrace();
			rsMap.put(RESP_BODY, e.getMessage());
		} finally {
			// 关闭资源
			try {
				if (response != null) {
					response.close();
				}
				if (httpclient != null) {
					httpclient.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return rsMap;
	}

	/**
	 * post请求（请求数据为表单格式）
	 * 
	 * @param url
	 *            请求URL
	 * @param params
	 *            请求参数
	 * @return
	 */
	public static Map<String, Object> postForm(String url, Map<String, String> params) {
		Map<String, Object> rsMap = new HashMap<String, Object>();
		// 参数不为空时，转换map参数为post请求参数格式
		List<NameValuePair> nvps = new ArrayList<NameValuePair>();
		if (params != null && params.size() > 0) {
			for (String key : params.keySet()) {
				nvps.add(new BasicNameValuePair(key, params.get(key)));
			}
		}
		httpPost = new HttpPost(url);
		httpPost.setConfig(config);
		try {
			httpPost.setEntity(new UrlEncodedFormEntity(nvps));
			response = httpclient.execute(httpPost);
			HttpEntity entity = response.getEntity();
			String result = EntityUtils.toString(entity, "utf-8");
			rsMap.put(STATUS, response.getStatusLine().getStatusCode());
			rsMap.put(RESP_BODY, result);
		} catch (Exception e) {
			e.printStackTrace();
			rsMap.put(RESP_BODY, e.getMessage());
		} finally {
			// 关闭资源
			try {
				if (response != null) {
					response.close();
				}
				if (httpclient != null) {
					httpclient.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return rsMap;
	}

	/**
	 * post请求（请求数据为json格式）
	 * 
	 * @param url
	 *            请求URL
	 * @param jsonStr
	 *            请求参数（该字符串为json格式）
	 * @return
	 */
	public static Map<String, Object> postJson(String url, String jsonStr) {
		Map<String, Object> rsMap = new HashMap<String, Object>();
		// 设置json的请求格式
		httpPost = new HttpPost(url);
		httpPost.setConfig(config);
		httpPost.addHeader("Content-type", "application/json; charset=utf-8");
		httpPost.setHeader("Accept", "application/json");
		httpPost.setEntity(new StringEntity(jsonStr, Charset.forName("utf-8")));
		try {
			response = httpclient.execute(httpPost);
			String result = EntityUtils.toString(response.getEntity(), "utf-8");
			rsMap.put(STATUS, response.getStatusLine().getStatusCode());
			rsMap.put(RESP_BODY, result);
		} catch (Exception e) {
			e.printStackTrace();
			rsMap.put(RESP_BODY, e.getMessage());
		} finally {
			try {
				if (httpclient != null)
					httpclient.close();
				if (response != null) {
					response.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return rsMap;
	}

	/**
	 * 以流的方式处理文件上传
	 * 
	 * @param url
	 *            请求URL
	 * @param input
	 *            输入流
	 * @param form
	 *            表单键值对
	 * @return
	 */
	public static Map<String, Object> postStream(String url, InputStream input, Map<String, String> form) {
		Map<String, Object> rsMap = new HashMap<String, Object>();
		try {
			httpPost = new HttpPost(url);
			MultipartEntityBuilder builder = MultipartEntityBuilder.create();
			// 这里拿不到原来的文件名，所以用了个随机名字替换
			builder.addBinaryBody("file", input, ContentType.MULTIPART_FORM_DATA,
					UUID.randomUUID().toString().replaceAll("-", ""));// 文件流
			// 封装表单数据
			if (form != null && form.size() > 0) {
				for (String key : form.keySet()) {
					builder.addTextBody(key, form.get(key));// 类似浏览器表单提交，对应input的name和value
				}
			}
			HttpEntity entity = builder.build();
			httpPost.setEntity(entity);
			HttpResponse response = httpclient.execute(httpPost);// 执行提交
			HttpEntity responseEntity = response.getEntity();
			if (responseEntity != null) {
				// 将响应内容转换为字符串
				String result = EntityUtils.toString(responseEntity, Charset.forName("UTF-8"));
				rsMap.put(RESP_BODY, result);
			}
			rsMap.put(STATUS, response.getStatusLine().getStatusCode());
		} catch (Exception e) {
			e.printStackTrace();
			rsMap.put(RESP_BODY, e.getMessage());
		} finally {
			try {
				if (httpclient != null)
					httpclient.close();
				if (response != null) {
					response.close();
				}
				if (input != null) {
					input.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return rsMap;
	}

	/**
	 * 以post请求的方式获取inputStream流
	 * 
	 * @param url
	 *            请求URL
	 * @param form
	 *            表单键值对内容
	 * @return
	 */
	public static Map<String, Object> getInputStreamByPost(String url, Map<String, String> form) {
		Map<String, Object> rsMap = new HashMap<String, Object>();
		// 参数不为空时，转换map参数为post请求参数格式
		List<NameValuePair> nvps = new ArrayList<NameValuePair>();
		if (form != null && form.size() > 0) {
			for (String key : form.keySet()) {
				nvps.add(new BasicNameValuePair(key, form.get(key)));
			}
		}
		httpPost = new HttpPost(url);
		httpPost.setConfig(config);
		try {
			httpPost.setEntity(new UrlEncodedFormEntity(nvps));
			response = httpclient.execute(httpPost);
			HttpEntity entity = response.getEntity();
			if (entity != null) {
				BufferedHttpEntity bufferedEntity = new BufferedHttpEntity(entity);
				InputStream input = new ByteArrayInputStream(EntityUtils.toByteArray(bufferedEntity));
				rsMap.put(RESP_BODY, input);
			}
			rsMap.put(STATUS, response.getStatusLine().getStatusCode());
		} catch (Exception e) {
			e.printStackTrace();
			rsMap.put(RESP_BODY, e.getMessage());
		} finally {
			// 关闭资源
			try {
				if (response != null) {
					response.close();
				}
				if (httpclient != null) {
					httpclient.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return rsMap;
	}

	/**
	 * 以get请求的方式获取inputStream流
	 * 
	 * @param url
	 *            请求URL
	 * @param form
	 *            请求参数
	 * @return
	 */
	public static Map<String, Object> getInputStreamByGet(String url, Map<String, String> params) {
		Map<String, Object> rsMap = new HashMap<String, Object>();
		// 参数不为空时，转换map参数为get请求参数格式
		if (params != null && params.size() > 0) {
			StringBuilder sb = new StringBuilder();
			for (String key : params.keySet()) {
				sb.append("&");
				sb.append(key);
				sb.append("=");
				sb.append(params.get(key));
			}
			if (url.contains("?")) {
				url = url + sb.toString();
			} else {
				url = url + "?" + sb.toString().substring(1);
			}
		}
		httpGet = new HttpGet(url);
		httpGet.setConfig(config);
		try {
			// 发送请求
			response = httpclient.execute(httpGet);
			HttpEntity entity = response.getEntity();
			if (entity != null) {
				BufferedHttpEntity bufferedEntity = new BufferedHttpEntity(entity);
				InputStream input = new ByteArrayInputStream(EntityUtils.toByteArray(bufferedEntity));
				rsMap.put(RESP_BODY, input);
			}
			rsMap.put(STATUS, response.getStatusLine().getStatusCode());
		} catch (Exception e) {
			e.printStackTrace();
			rsMap.put(RESP_BODY, e.getMessage());
		} finally {
			// 关闭资源
			try {
				if (response != null) {
					response.close();
				}
				if (httpclient != null) {
					httpclient.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return rsMap;
	}

}
