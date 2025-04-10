package pizza.kkomdae.ssafyapi.dto.rtn;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class PutUserRtn {
    private String channel_id;
    private String user_id;
    private String roles;
    private String id;
}
