package pizza.kkomdae.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Entity
@Getter
@Setter
public class Student {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long studentId;
    private String ssafyId;
    private String name;
    private String email;
    private String edu;
    private String studentNum;
    private String region;
    private String classNum;
    private String retireYn;
    private String refreshToken;
    @OneToMany(mappedBy = "student")
    private List<Rent> rent;
    @OneToMany(mappedBy = "student")
    private List<LaptopTestResult> laptopTestResults;
}
