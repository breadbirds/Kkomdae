package pizza.kkomdae.dto.request;

import lombok.Getter;
import lombok.Setter;
import pizza.kkomdae.entity.Student;

@Getter
@Setter
public class StudentWithRentCond {
    private String region;
    private String className;
    private String searchType; //학번, 이름
    private String keyword;
    private Student student;
}
