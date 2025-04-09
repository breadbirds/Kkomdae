package pizza.kkomdae.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import pizza.kkomdae.dto.request.FlaskRequest;
import pizza.kkomdae.dto.respond.FlaskResponse;

@Service
public class FlaskService {

    private final RestTemplate restTemplate;

    @Value("${flask.url}")
    private String flaskUrl;
    
    public FlaskService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }
    
    public FlaskResponse analyzeImage(FlaskRequest flaskRequest) {
        
        // HTTP 요청 생성
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<FlaskRequest> requestEntity = new HttpEntity<>(flaskRequest, headers);

        // Flask 서버로 요청 전송
        ResponseEntity<FlaskResponse> responseEntity = restTemplate.exchange(
                flaskUrl,
                HttpMethod.POST,
                requestEntity,
                FlaskResponse.class
        );

        // 응답 반환
        return responseEntity.getBody();
    }
}
