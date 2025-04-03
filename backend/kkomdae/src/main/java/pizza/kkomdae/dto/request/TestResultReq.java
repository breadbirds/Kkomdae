package pizza.kkomdae.dto.request;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class TestResultReq {
    private String randomKey;
    private String testType;  // "키보드", "카메라", "USB", "충전", "배터리"
    private boolean success;
    private List detail = null;
    private String summary = null;
}
