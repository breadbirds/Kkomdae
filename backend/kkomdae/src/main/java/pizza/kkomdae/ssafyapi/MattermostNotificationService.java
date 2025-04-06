package pizza.kkomdae.ssafyapi;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import pizza.kkomdae.ssafyapi.dto.rtn.PutUserRtn;

import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Mattermost API를 이용하여 알림(메시지) 전송 로직을 구현한 서비스
 */

@Slf4j
@Service
@RequiredArgsConstructor
public class MattermostNotificationService {

    // Mattermost API 기본 URL (예: "https://your-mm-url.com/api/v4")
    @Value("${mattermost.base-url}")
    private String baseUrl;

    // Personal Access teamId
    @Value("${mattermost.teamId}")
    private String teamId;

    @Value("${mattermost.channelid}")
    private String channelId;

    private final RestTemplate restTemplate;


    public void sendGroupInfoMessage(List<String> nicknames) {
        String message = "# :alert_siren: [공지] 꼼대 서비스를 이용해 대여/반납 절차 안내\n" +
                "**지급받은 노트북의 대여/반납을 위해 꼼대를 이용해 노트북 상태를 제출해주세요. **\n" +
                "**2025년04월11일까지 제출을 완료해주세요. **\n" +
                "**절차 미이행시 노트북 사용에 제한이 있을 수 있습니다. 꼭 지정된 날짜까지 프로세스를 완료해주세요.**\n" +
                "## **[꼼대 사용 방법]**\n" +
                "### :one: 꼼대 `애플리케이션`을 다운받아 노트북을 촬영해주세요.\n" +
                "### :two: 꼼대 `노트북 검사 프로그램`을 통해 노트북을 검사해주세요.\n" +
                "### :three: 제출을 완료하고 PDF를 확인해 보세요! \n" +
                "## ** [ :kkomdae_write:  꼼대 다운 받으러 가기 :kkomdae_go: ](https://j12d101.p.ssafy.io)**";  // 발송할 메시지 내용

        putUserToChannel(channelId, nicknames);
        sendMessage(channelId, message);
    }


    public void sendGroupHurryMessage(List<String> nicknames) {
        String message = "빨리 마무리 해주세요\n";
        message += makeTag(nicknames);
        putUserToChannel(channelId, nicknames);
        sendMessage(channelId, message);
    }

    private String makeTag(List<String> nicknames) {
        //nicknames로 사실 찾아야댐
        return "@s12d101user1, @s12d101user2";
    }

    private String createGroupChannel(List<String> userIds) {

        //TODO 여기에 실제 유저를 추가하는 로직을 작성해야하나 mm 제한으로 하드코딩된 채널 아이디를 사용

        return null;
    }

    /**
     * 생성된 채널에 메시지를 전송합니다.
     *
     * @param channelId 채널 ID
     * @param message   전송할 메시지 내용
     */
    private void
    sendMessage(String channelId, String message) {
//      URL 생성

        String url = baseUrl + "/posts";

        URI uri = UriComponentsBuilder.fromUriString(url)
                .queryParam("apiKey", teamId)
                .build()
                .toUri();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, String> body = Map.of(
                "channel_id", channelId,
                "message", message
        );

        log.info(uri.toASCIIString());

        HttpEntity<Map<String, String>> request = new HttpEntity<>(body, headers);
        // 기본 동작 흐름
        try {
            ResponseEntity<PutUserRtn> response = restTemplate.exchange(uri,
                    HttpMethod.POST, request,
                    PutUserRtn.class);
            log.info(response.getBody().getChannel_id());
        } catch (HttpClientErrorException e) {
            // 4xx 오류 시 자동 throw
            String responseBody = e.getResponseBodyAsString(); // 본문 추출
            log.error(responseBody);
        }


    }

    private void putUserToChannel(String channelId, List<String> nicknames) {
//        TODO 여기서 nicknames로 추가해야하지만 mm 제한상 하드코딩된 유저 사용

        // URL 생성
        String sb = baseUrl +
                "/channels" +
                "/" +
                channelId +
                "/members";
        URI uri = UriComponentsBuilder.fromUriString(sb)
                .queryParam("apiKey", teamId)
                .build()
                .toUri();
        log.info("request url : {}", uri.toASCIIString());

        // 요청 본문 생성
        Map<String, String> requestBody = new HashMap<>();
        requestBody.put("user_id", "kbb41umudb89mjet97kocjk5fr");


        // HTTP 헤더 설정
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        // HTTP 엔티티 생성 (요청 본문과 헤더 포함)
        HttpEntity<Map<String, String>> httpEntity = new HttpEntity<>(requestBody, headers);

        try {
            ResponseEntity<PutUserRtn> response = restTemplate.exchange(uri,
                    HttpMethod.POST, httpEntity,
                    PutUserRtn.class);
            if (response.getStatusCode().is2xxSuccessful()) {
                PutUserRtn body = response.getBody();
                log.info("채널 아이디 : {} 에 {} 추가",body.getChannel_id(),body.getUser_id());
            } else {
                log.error("채널에 추가 실패");
                log.error(response.getBody().toString());
                throw new AuthenticationServiceException("채널에 추가 실패",
                        new RuntimeException("Authentication Failed with code " + response.getStatusCode()));
            }
        } catch (HttpClientErrorException ex) {
            throw new RuntimeException("채널에 추가 실패", ex);
        }

    }
}