package com.study.mate.httpclient;

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

public class GoogleLogin {
    public static JsonNode getAccessToken(String code) { // 6. 파라미터로 컨트롤러에서 넘어온 code를 받아온다.
        // 7. 6에서 받아온 code를 가지고 access_token을 받아오는 구글 서버 URL을 작성한다.
        final String requestUrl = "https://www.googleapis.com/oauth2/v4/token";

        // 8. POST 방식은 데이터를 URL에 직접적으로 담아서 같이 보낼 수 없기에, 따로 List를 만들어 같이 보낼 수 있도록 만든다.
        final List<NameValuePair> postParams = new ArrayList<NameValuePair>();
        // 8-1. 8에서 만든 List에 URL을 통해 같이 가져가야 할 데이터들을 name/value 쌍으로 담아준다.
        postParams.add(new BasicNameValuePair("grant_type", "authorization_code")); // 권한 타입을 인증 코드로 설정
        postParams.add(new BasicNameValuePair("client_id", "346535144521-qthfl467cl8jrdcdc2g8l3lvpurqsk1h.apps.googleusercontent.com")); // 클라이언트 ID
        postParams.add(new BasicNameValuePair("client_secret", "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")); // 클라이언트 비밀키
        postParams.add(new BasicNameValuePair("redirect_uri", "http://localhost:8888/loginform/googletoken")); // 리다이렉트 URI
        postParams.add(new BasicNameValuePair("code", code)); // 6에서 파라미터로 받아온 code

        // 9. 외부 서버와 통신을 맡아줄 HttpClient를 생성한다.
        final HttpClient client = HttpClientBuilder.create().build();
        // 10. 외부 서버와 통신할 때 사용할 HttpMethod를 생성하고, 파라미터에 7에서 작성한 URL을 전달한다.
        final HttpPost post = new HttpPost(requestUrl); // POST 방식

        // 11. 해당 메소드의 반환 값으로 사용할 변수를 미리 만들어둔다.
        JsonNode returnNode = null;

        // 외부 서버와 통신 시작
        try {
            // 12. 8에서 만든 데이터 List를 HttpMethod에 setter를 통해 전달한다. - POST 방식에만 사용
            post.setEntity(new UrlEncodedFormEntity(postParams));
            // 13. 9에서 생성한 HttpClent에 10에서 생성한 HttpMethod를 전달하여 실행하고, HttpResponse로 반환되는 값을 받아온다.
            final HttpResponse response = client.execute(post);
            // 응답 상태 코드 얻기
//            final int responseCode = response.getStatusLine().getStatusCode();

//            System.out.println("\nSending 'POST' request to URL : " + requestUrl);
//            System.out.println("Post parameters : " + postParams);
//            System.out.println("Response Code : " + responseCode);

            // 14. JSON 형식의 데이터를 파싱하기 위한 ObjectMapper를 생성한다.
            // ObjectMapper - JSON 형식의 데이터를 Java 객체로 역직렬화(Deserialize)하거나, 반대로 Java 객체를 JSON 형식의 데이터로 직렬화(Serialize)할 때 사용하는 Jackson 라이브러리의 클래스이다.
            ObjectMapper mapper = new ObjectMapper();
            // 15. 13에서 받아온 JSON 형식의 반환 값을 11에서 미리 만들어둔 변수에 해당 타입인 JsonNode 객체로 파싱하여 전달한다.
            returnNode = mapper.readTree(response.getEntity().getContent());

        } catch (UnsupportedEncodingException e) { // 지원되지 않는 인코딩 예외
            e.printStackTrace();
        } catch (ClientProtocolException e) { // 클라이언트 프로토콜 예외
            e.printStackTrace();
        } catch (IOException e) { // I/O 예외
            e.printStackTrace();
        }

        // 16. 15에서 전달받은 값을 반환한다.
        return returnNode;
    }

    public static JsonNode getGoogleUserInfo(String accessToken) { // 19. 파라미터로 컨트롤러에서 넘어온 accessToken을 받아온다.
        // 20. 19에서 받아온 access_token을 가지고 구글 로그인 유저 정보를 받아오는 구글 서버 URL을 작성한다.
        final String requestUrl = "https://www.googleapis.com/oauth2/v2/userinfo";

        // 21. 외부 서버와 통신을 맡아줄 HttpClient를 생성한다.
        final HttpClient client = HttpClientBuilder.create().build();
        // 22. 외부 서버와 통신할 때 사용할 HttpMethod를 생성하고, 파라미터에 20에서 작성한 URL을 전달한다.
        final HttpGet get = new HttpGet(requestUrl); // GET 방식

        // 23. 해당 메소드의 반환 값으로 사용할 변수를 미리 만들어둔다.
        JsonNode returnNode = null;

        // 외부 서버와 통신 시작
        try {
            // 24. 헤더에 Authorization으로 19에서 파라미터로 받아온 access_token을 추가한다.
            // addHeader - 헤더에 필요한 데이터들을 name/value 쌍으로 추가한다.
            get.addHeader("Authorization", "Bearer " + accessToken);
            // 25. 21에서 생성한 HttpClent에 22에서 생성한 HttpMethod를 전달하여 실행하고, HttpResponse로 반환되는 값을 받아온다.
            final HttpResponse response = client.execute(get);
            // 응답 상태 코드 얻기
//            final int responseCode = response.getStatusLine().getStatusCode();

//            System.out.println("\nSending 'GET' request to URL : " + requestUrl);
//            System.out.println("Response Code : " + responseCode);

            // 26. JSON 형식의 데이터를 파싱하기 위한 ObjectMapper를 생성한다.
            // ObjectMapper - JSON 형식의 데이터를 Java 객체로 역직렬화(Deserialize)하거나, 반대로 Java 객체를 JSON 형식의 데이터로 직렬화(Serialize)할 때 사용하는 Jackson 라이브러리의 클래스이다.
            ObjectMapper mapper = new ObjectMapper();
            // 27. 25에서 받아온 JSON 형식의 반환 값을 23에서 미리 만들어둔 변수에 해당 타입인 JsonNode 객체로 파싱하여 전달한다.
            returnNode = mapper.readTree(response.getEntity().getContent());

        } catch (UnsupportedEncodingException e) { // 지원되지 않는 인코딩 예외
            e.printStackTrace();
        } catch (ClientProtocolException e) { // 클라이언트 프로토콜 예외
            e.printStackTrace();
        } catch (IOException e) { // I/O 예외
            e.printStackTrace();
        }

        // 28. 27에서 전달받은 값을 반환한다.
        return returnNode;
    }

    public static JsonNode getGooglePeople(String accessToken) { // 31. 파라미터로 컨트롤러에서 넘어온 accessToken을 받아온다.
        // 32. 30에서 받아온 access_token을 가지고 구글 로그인 유저 정보를 받아오는 구글 서버 URL을 작성한다.
        //     GET 방식은 데이터를 URL에 직접적으로 담아서 같이 보낼 수 있기에, URL에 파라미터로 같이 보낼 데이터들을 name/value 쌍으로 추가 작성한다.
        final String requestUrl = "https://people.googleapis.com/v1/people/me?" +
                                  "personFields=birthdays" + // 생년월일
                                  "&personFields=genders" + // 성별
                                  "&key=AIzaSyDr3XNA_3hT9py0zIaHxQwIBVibhuIC_3E" + // API 키
                                  "&access_token=" + accessToken; // 30에서 파라미터로 받아온 access_token

        // 33. 외부 서버와 통신을 맡아줄 HttpClient를 생성한다.
        final HttpClient client = HttpClientBuilder.create().build();
        // 34. 외부 서버와 통신할 때 사용할 HttpMethod를 생성하고, 파라미터에 32에서 작성한 URL을 전달한다.
        final HttpGet get = new HttpGet(requestUrl); // GET 방식

        // 35. 해당 메소드의 반환 값으로 사용할 변수를 미리 만들어둔다.
        JsonNode returnNode = null;

        // 외부 서버와 통신 시작
        try {
            // 36. 33에서 생성한 HttpClent에 34에서 생성한 HttpMethod를 전달하여 실행하고, HttpResponse로 반환되는 값을 받아온다.
            final HttpResponse response = client.execute(get);
            // 응답 상태 코드 얻기
//            final int responseCode = response.getStatusLine().getStatusCode();

//            System.out.println("\nSending 'GET' request to URL : " + requestUrl);
//            System.out.println("Response Code : " + responseCode);

            // 37. JSON 형식의 데이터를 파싱하기 위한 ObjectMapper를 생성한다.
            // ObjectMapper - JSON 형식의 데이터를 Java 객체로 역직렬화(Deserialize)하거나, 반대로 Java 객체를 JSON 형식의 데이터로 직렬화(Serialize)할 때 사용하는 Jackson 라이브러리의 클래스이다.
            ObjectMapper mapper = new ObjectMapper();
            // 38. 36에서 받아온 JSON 형식의 반환 값을 35에서 미리 만들어둔 변수에 해당 타입인 JsonNode 객체로 파싱하여 전달한다.
            returnNode = mapper.readTree(response.getEntity().getContent());

        } catch (UnsupportedEncodingException e) { // 지원되지 않는 인코딩 예외
            e.printStackTrace();
        } catch (ClientProtocolException e) { // 클라이언트 프로토콜 예외
            e.printStackTrace();
        } catch (IOException e) { // I/O 예외
            e.printStackTrace();
        }

        // 39. 38에서 전달받은 값을 반환한다.
        return returnNode;
    }
}