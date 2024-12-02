package com.ksmartpia.acube.weatherasos.component;

//import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.http.impl.client.HttpClients;
import org.codehaus.jettison.json.JSONObject;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.http.converter.FormHttpMessageConverter;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.util.concurrent.ListenableFutureCallback;
import org.springframework.web.client.AsyncRestTemplate;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class SpringRestService {
	static long lastCallTime = System.currentTimeMillis();
	static String lastUrl = "Initial";
    private static ClientHttpRequestFactory clientHttpRequestFactory() {
        HttpComponentsClientHttpRequestFactory factory = new HttpComponentsClientHttpRequestFactory(HttpClients.createDefault());
        factory.setReadTimeout(1000 * 60 * 10);
        factory.setConnectTimeout(1000 * 20);				//커넥트 시간  1000= 1초

        return factory;
    }

    public static String doRest(String serverUrl, String serviceName, String uri, JSONObject parms , String httpMethod ) throws HttpClientErrorException {
    	return doRest(serverUrl, serviceName, uri, parms , httpMethod );
    }
    
    public static <E> String doRest(String serverUrl, String serviceName, String uri, List<E> lstParam , String httpMethod ) throws HttpClientErrorException, JsonProcessingException {
    	ObjectMapper jsonStr = new ObjectMapper();  
    	return doRest(serverUrl, serviceName, uri,  jsonStr.writeValueAsString(lstParam) , httpMethod );
    }
    
    public static <K, V> String doRest(String serverUrl, String serviceName, String uri, Map<K, V> lstParam , String httpMethod ) throws HttpClientErrorException, JsonProcessingException {
    	ObjectMapper jsonStr = new ObjectMapper();  
    	return doRest(serverUrl, serviceName, uri,  jsonStr.writeValueAsString(lstParam) , httpMethod );
    }
    
    public static void doAsyncRest(String serverUrl, String serviceName, String uri, JSONObject parms , String httpMethod ,
			ListenableFutureCallback<ResponseEntity<String>> callBack ) throws HttpClientErrorException {
    	doAsyncRest(serviceName, uri, parms , httpMethod, callBack );
    }
    
    public static <E> void doAsyncRest(String serverUrl, String serviceName, String uri, List<E> lstParam , String httpMethod ,
			ListenableFutureCallback<ResponseEntity<String>> callBack ) throws HttpClientErrorException, JsonProcessingException {
    	ObjectMapper jsonStr = new ObjectMapper();  
    	doAsyncRest(serviceName, uri, jsonStr.writeValueAsString(lstParam) , httpMethod, callBack );
    }
    
    public static <K, V> void doAsyncRest(String serverUrl, String serviceName, String uri, Map<K, V> lstParam , String httpMethod ,
			ListenableFutureCallback<ResponseEntity<String>> callBack ) throws HttpClientErrorException, JsonProcessingException {
    	ObjectMapper jsonStr = new ObjectMapper();  
    	doAsyncRest(serviceName, uri, jsonStr.writeValueAsString(lstParam) , httpMethod, callBack );
    }

	private static String doRest(String serverUrl, String serviceName, String uri, Object parms , String httpMethod ) throws HttpClientErrorException {

		HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
//        headers.set("Authorization", "Bearer "+SecurityContextUtils.getAccessToken());
        
        HttpEntity<String> entity = new HttpEntity<String>("", headers);
        if ( parms != null ) {
			entity = new HttpEntity<String>( parms.toString() , headers);
        }

        ResponseEntity<String> responseEntity = null;

        try {

            final  String url = String.format("%s%s%s", serverUrl, serviceName, uri);
    		if(System.currentTimeMillis() - lastCallTime < 200 && lastUrl.equals(url)) {
    			return "";
    		}else {
    			lastCallTime =System.currentTimeMillis();
    			lastUrl=url;
    		}
            RestTemplate restTemplate = new RestTemplate(clientHttpRequestFactory());

            List<HttpMessageConverter<?>> messageConverters = new ArrayList<HttpMessageConverter<?>>();
            messageConverters.add(new FormHttpMessageConverter());
            messageConverters.add(new StringHttpMessageConverter(Charset.forName("UTF-8")));
            restTemplate.setMessageConverters(messageConverters);

            switch ( httpMethod ) {
                case "GET":
                    responseEntity = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);
                    break;
                case "POST":
                    responseEntity = restTemplate.exchange(url, HttpMethod.POST, entity, String.class);
                    break;
                case "PUT":
                    responseEntity = restTemplate.exchange(url, HttpMethod.PUT, entity, String.class);
                    break;
                case "DELETE":
                    responseEntity = restTemplate.exchange(url, HttpMethod.DELETE, entity, String.class);
                    break;
            }

            if ( responseEntity.getStatusCode().is2xxSuccessful() ) {

            	if ( responseEntity.getBody() != null ) {
                    return responseEntity.getBody();
            	} else {
            		return "OK";
            	}
            } else {
				return "Error" + responseEntity.getBody();
            }
        } catch (HttpServerErrorException e) {
        	return "HttpServerErrorException" + e.getResponseBodyAsString();
        }
	}


	private static void doAsyncRest(String appUrl, String urlString, Object parms , String httpMethod,
			ListenableFutureCallback<ResponseEntity<String>> callBack ) {

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

//        if (RestConstants.getAccessToken() != null) {
//            headers.add("Authorization", "Bearer " + RestConstants.getAccessToken());
//        }
        HttpEntity<String> entity = new HttpEntity<String>("", headers);
        if ( parms != null ) {
			entity = new HttpEntity<String>( parms.toString() , headers);
        }

        ListenableFuture<ResponseEntity<String>> futureEntity  = null;
        
        final  String url = appUrl + "/" + urlString;
		AsyncRestTemplate asyncRestTemplate = new AsyncRestTemplate();
        List<HttpMessageConverter<?>> messageConverters = new ArrayList<HttpMessageConverter<?>>();
        messageConverters.add(new FormHttpMessageConverter());
        messageConverters.add(new StringHttpMessageConverter(Charset.forName("UTF-8")));
        asyncRestTemplate.setMessageConverters(messageConverters);

        switch ( httpMethod ) {
            case "GET":
            	futureEntity = asyncRestTemplate.exchange(url, HttpMethod.GET, entity, String.class);
                break;
            case "POST":
            	futureEntity = asyncRestTemplate.exchange(url, HttpMethod.POST, entity, String.class);
                break;
            case "PUT":
            	futureEntity = asyncRestTemplate.exchange(url, HttpMethod.PUT, entity, String.class);
                break;
            case "DELETE":
            	futureEntity = asyncRestTemplate.exchange(url, HttpMethod.DELETE, entity, String.class);
                break;
        }

        futureEntity.addCallback(callBack);
	}

}
