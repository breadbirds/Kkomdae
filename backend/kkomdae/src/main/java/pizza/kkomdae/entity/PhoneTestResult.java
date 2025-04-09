package pizza.kkomdae.entity;

import jakarta.persistence.*;

import java.util.List;

@Entity
public class PhoneTestResult {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long phoneTestResultId;
    @ManyToOne
    private Device device;
    @OneToMany(mappedBy = "phoneTestResult")
    private List<Photo> photo;
}
