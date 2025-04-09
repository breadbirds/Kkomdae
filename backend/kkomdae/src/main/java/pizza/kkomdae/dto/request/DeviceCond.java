package pizza.kkomdae.dto.request;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class DeviceCond {
    private String searchType;
    private String searchKeyword;
    private String deviceType;
}
