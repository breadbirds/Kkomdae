package pizza.kkomdae.entity;

import jakarta.persistence.Entity;
import lombok.Getter;
import lombok.NoArgsConstructor;
import pizza.kkomdae.dto.request.ThirdStageReq;

@Entity
@Getter
@NoArgsConstructor
public class Laptop extends Device{
    String laptopBarcodeNum;

    public Laptop(ThirdStageReq thirdStageReq) {
        this.setRelease(false);
        this.setDeviceType("Laptop");
        this.laptopBarcodeNum = thirdStageReq.getBarcodeNum();
        this.setModelCode(thirdStageReq.getModelCode());
        this.setSerialNum(thirdStageReq.getSerialNum());
    }
}