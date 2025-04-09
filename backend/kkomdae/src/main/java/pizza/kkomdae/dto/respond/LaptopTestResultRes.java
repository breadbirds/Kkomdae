package pizza.kkomdae.dto.respond;

import lombok.Getter;
import lombok.Setter;
import pizza.kkomdae.entity.LaptopTestResult;

@Setter
@Getter
public class LaptopTestResultRes {
    private boolean success;
    private Boolean battery_report;
    private Boolean camera_status;
    private Boolean charging_status;
    private Boolean keyboard_status;
    private Boolean usb_status;
    private String failed_keys;
    private String battery_report_url;
    private String failed_ports;

    public LaptopTestResultRes() {
        this.success = false;
        this.battery_report = null;
        this.camera_status = null;
        this.charging_status = null;
        this.keyboard_status = null;
        this.usb_status = null;
        this.failed_keys = null;
        this.battery_report_url = null;
        this.failed_ports = null;
    }

    public static LaptopTestResultRes fromEntity(LaptopTestResult result) {
        LaptopTestResultRes dto = new LaptopTestResultRes();
        dto.setSuccess(result.isAllTestCompleted());
        
        // null 체크 후 값 설정
        dto.setBattery_report(result.getBatteryReport());
        dto.setCamera_status(result.getCameraStatus());
        dto.setCharging_status(result.getChargerStatus());
        dto.setKeyboard_status(result.getKeyboardStatus());
        dto.setUsb_status(result.getUsbStatus());
        
        // 실패한 항목들은 해당하는 상태가 false일 때만 설정
        dto.setFailed_keys(result.getKeyboardStatus() != null && !result.getKeyboardStatus() ? 
            result.getFailedKeys() : null);
        dto.setBattery_report_url(result.getBatteryReport() != null && result.getBatteryReport() ? 
            result.getBatteryReportUrl() : null);
        dto.setFailed_ports(result.getUsbStatus() != null && !result.getUsbStatus() ? 
            result.getFailedPorts() : null);
        
        return dto;
    }
}
