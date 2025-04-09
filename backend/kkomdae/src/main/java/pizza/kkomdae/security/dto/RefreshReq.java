package pizza.kkomdae.security.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RefreshReq {
    private String refreshToken;
}
