package com.jerry.http_client_util;

import com.jerry.http_client_util.utility.HttpClientUtil;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.HashMap;
import java.util.Map;

@RunWith(SpringRunner.class)
@SpringBootTest
public class HttpClientUtilApplicationTests {

	@Test
	public void contextLoads() {
	}

	@Test
	public void fileUpLoadTest(){

		String uri = "http://146.122.219.108:9002/testUploadFile";
		HttpPost httpPost = new HttpPost(uri);
		CloseableHttpResponse chResponse = HttpClientUtil.sendHttpsPostWithFile(httpPost, "C:\\Users\\x1twbm\\Desktop\\owl\\wine.owl");

		String response = HttpClientUtil.getResponseEntity(chResponse);

		System.out.println("code:" + HttpClientUtil.getResponseCode(chResponse));
	}

	@Test
	public void postFormTest(){

		String uri = "http://146.122.219.56:8080/login";
		Map<String, Object> map = new HashMap<>();
		map.put("username", "zhang xiao");
		map.put("password", "hahah");
		CloseableHttpResponse closeableHttpResponse = HttpClientUtil.sendHttpPostWithForm(uri, map);

		System.out.println("code:" + HttpClientUtil.getResponseCode(closeableHttpResponse));
		System.out.println("content:" + HttpClientUtil.getResponseEntity(closeableHttpResponse));
	}

}
