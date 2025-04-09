package pizza.kkomdae.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.List;

@Entity
@Getter
@Setter
public class Rent {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long rentId;
    private LocalDate releaseDateTime;
    private LocalDate rentDateTime;
    @ManyToOne(fetch = FetchType.LAZY)
    private Student student;
    @ManyToOne(fetch = FetchType.LAZY)
    private Device device;
    @OneToMany(mappedBy = "rent")
    private List<LaptopTestResult> laptopTestResults;
}
