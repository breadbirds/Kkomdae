package pizza.kkomdae.dto.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Getter;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

@Getter
public class ThirdStageReq {
    private long testId;
    private boolean release;
    private String modelCode; // rent 생성시 필요한 것들
    private String serialNum; //
    private String barcodeNum; //
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate localDate;
    private int laptop;
    private int powerCable;
    private int adapter;
    private int mouse;
    private int bag;
    private int mousePad;
}

