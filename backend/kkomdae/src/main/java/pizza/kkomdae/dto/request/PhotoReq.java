package pizza.kkomdae.dto.request;

import lombok.Getter;
import lombok.Setter;
import pizza.kkomdae.enums.PhotoType;
@Getter
@Setter
public class PhotoReq {
    private int photoType;
    private long testId;
}
