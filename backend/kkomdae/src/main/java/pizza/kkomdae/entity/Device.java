package pizza.kkomdae.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Entity
@Getter
@Setter
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "device_type" )
public class Device {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long deviceId;

//  device_type 접근을 위한 필드 추가. 절대 변경 해서는 안됨
    @Column(name = "device_type", insertable = false, updatable = false)
    private String deviceType;

    private String serialNum;
    private String modelCode;

    private boolean release;
    @OneToMany(mappedBy = "device")
    private List<PhoneTestResult> phoneTestResults;
    @OneToMany(mappedBy = "device")
    private List<LaptopTestResult> laptopTestResults;
    @OneToMany(mappedBy = "device")
    private List<Rent> rent;
}
