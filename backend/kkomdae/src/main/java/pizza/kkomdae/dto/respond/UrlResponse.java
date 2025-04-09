package pizza.kkomdae.dto.respond;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

// Lombok 어노테이션을 사용하여 생성자와 getter/setter를 자동 생성
@Getter
@Setter
@AllArgsConstructor
public class UrlResponse {
    // URL 정보를 담을 변수
    private String url;
}
