package pizza.kkomdae.ssafyapi.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class UserInfo {
    private String name;
    private String email;
    private String edu;
    private String entRegn;
    private String retireYn;
    private String clss;
    private List<TeamInfo> teams;
}
