package pizza.kkomdae.ssafyapi.dto.rtn;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserRequestForSso {
    private String userId; // 싸피 고유 id
    private String loginId; // 사실 email
    private String name; // 실명
}
