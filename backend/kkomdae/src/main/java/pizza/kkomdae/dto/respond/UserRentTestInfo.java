package pizza.kkomdae.dto.respond;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Setter
@Getter
public class UserRentTestInfo {
    private List<UserRentTestRes> userRentTestRes;
    private long onGoingTestId = 0;
    private int stage = 0;
    private int picStage = 0;
    private String name;
}
