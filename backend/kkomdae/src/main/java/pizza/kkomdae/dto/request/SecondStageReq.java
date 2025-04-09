package pizza.kkomdae.dto.request;

import lombok.Getter;

@Getter
public class SecondStageReq {
    private long testId;
    private boolean keyboardStatus;
    private String failedKeys;
    private boolean usbStatus;
    private String failedPorts;
    private boolean cameraStatus;
    private boolean chargerStatus;
    private boolean batteryReport;
    private String batteryReportUrl;
}
