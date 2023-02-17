package com.study.soju.httpclient;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicNameValuePair;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

public class IamPortPass {
    public static JsonNode getToken(){
        final String RequestUrl = "https://api.iamport.kr/users/getToken";

        final List<NameValuePair> postParams = new ArrayList<NameValuePair>();
        postParams.add(new BasicNameValuePair("imp_key", "4623303517205650"));
        postParams.add(new BasicNameValuePair("imp_secret", "QAU7SorhdVjluWumDn0SzHuKelzHKL2uYD9KmUMNT6eyjTtpFGVB5qeGYhWF2sul8aLu9V2ufpsLFF6h"));

        final HttpClient client = HttpClientBuilder.create().build();
        final HttpPost post = new HttpPost(RequestUrl);
        JsonNode returnJson = null;

        try {
            post.setEntity(new UrlEncodedFormEntity(postParams));
            final HttpResponse response = client.execute(post);
            final int responseCode = response.getStatusLine().getStatusCode();

            System.out.println("\nSending 'POST' request to URL : " + RequestUrl);
            System.out.println("Post parameters : " + postParams);
            System.out.println("Response Code : " + responseCode);

            //JSON 형태로 반환값 처리
            ObjectMapper mapper = new ObjectMapper();
            returnJson = mapper.readTree(response.getEntity().getContent());
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (ClientProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return returnJson;
    }

    public static JsonNode getUserInfo(String impUid, String accessToken) {
        final String RequestUrl = "https://api.iamport.kr/certifications/" + impUid;

        final HttpClient client = HttpClientBuilder.create().build();
        final HttpGet get = new HttpGet(RequestUrl);

        JsonNode returnJson = null;


        get.addHeader("Authorization", "Bearer " + accessToken);
        try {
            final HttpResponse response = client.execute(get);
            final int responseCode = response.getStatusLine().getStatusCode();

            ObjectMapper mapper = new ObjectMapper();
            returnJson = mapper.readTree(response.getEntity().getContent());

            System.out.println("\nSending 'GET' request to URL : " + RequestUrl);
            System.out.println("Response Code : " + responseCode);

        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (ClientProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return returnJson;
    }
}
