package pizza.kkomdae.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import pizza.kkomdae.dto.request.AiPhotoInfo;
import pizza.kkomdae.enums.PhotoType;

@Entity
@Getter
@NoArgsConstructor
@Setter
public class Photo {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long photoId;
    private String name;
    private String aiName;
    private Integer damage;
    private int type;
    @ManyToOne
    private LaptopTestResult laptopTestResult;
    @ManyToOne
    private PhoneTestResult phoneTestResult;

    public Photo(AiPhotoInfo aiPhotoInfo) {
        this.name = aiPhotoInfo.getName();
        this.type = aiPhotoInfo.getType();
    }
}

