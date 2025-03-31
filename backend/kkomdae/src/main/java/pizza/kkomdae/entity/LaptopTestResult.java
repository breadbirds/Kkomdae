package pizza.kkomdae.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import pizza.kkomdae.dto.request.SecondStageReq;
import pizza.kkomdae.dto.request.ThirdStageReq;

import java.time.LocalDate;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class LaptopTestResult {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long laptopTestResultId;
    @ManyToOne(fetch = FetchType.LAZY)
    Device device;
    private Boolean release;
    private Boolean keyboardStatus;
    private String failedKeys; // TODO 입력 값에 따라 수정해야함
    private Boolean usbStatus;
    private String failedPorts; // TODO 입력 값에 따라 수정해야함
    private Boolean cameraStatus;
    private Boolean chargerStatus;
    private Boolean batteryReport;
    private String batteryReportUrl;
    private LocalDate date;
    private String pdfFileName;
    private Integer laptop;
    private Integer powerCable;
    private Integer adapter;
    private Integer mouse;
    private Integer bag;
    private Integer mousePad;
    private Integer stage = 1;
    private Integer picStage = 0;
    private String description;


    @OneToMany(mappedBy = "laptopTestResult")
    private List<Photo> photos;
    @ManyToOne(fetch = FetchType.LAZY)
    private Student student;
    // Todo ai 결과를 어떻게 저장할 지 고민

    public LaptopTestResult(Student student) {
        this.release = false;
        this.student = student;
    }

    public void saveSecondStage(SecondStageReq secondStageReq) {
        this.stage = 3;
        this.keyboardStatus = secondStageReq.isKeyboardStatus();
        this.failedKeys = secondStageReq.getFailedKeys();
        this.usbStatus = secondStageReq.isUsbStatus();
        this.failedPorts = secondStageReq.getFailedPorts();
        this.cameraStatus = secondStageReq.isCameraStatus();
        this.chargerStatus = secondStageReq.isChargerStatus();
        this.batteryReport = secondStageReq.isBatteryReport();
        this.batteryReportUrl = secondStageReq.getBatteryReportUrl();
    }

    public void saveThirdStage(ThirdStageReq thirdStageReq) {
        this.stage = 4;
        this.release = thirdStageReq.isRelease();
        this.date = thirdStageReq.getLocalDate();
        this.laptop = thirdStageReq.getLaptop();
        this.powerCable = thirdStageReq.getPowerCable();
        this.adapter = thirdStageReq.getAdapter();
        this.mouse = thirdStageReq.getMouse();
        this.bag = thirdStageReq.getBag();
        this.mousePad = thirdStageReq.getMousePad();
    }
}
