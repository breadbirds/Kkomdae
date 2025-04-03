package pizza.kkomdae.dto.respond;

import lombok.Getter;
import lombok.Setter;
import pizza.kkomdae.entity.LaptopTestResult;

@Setter
@Getter
public class LaptopTestResultRes {
    private boolean battery_report;
    private boolean camera_status;
    private boolean charging_status;
    private boolean keyboard_status;
    private boolean usb_status;
    private String failed_keys;
    private String battery_report_url;
    private String failed_ports;

    public LaptopTestResultRes() {
        this.battery_report = false;
        this.camera_status = false;
        this.charging_status = false;
        this.keyboard_status = false;
        this.usb_status = false;
        this.failed_keys = null;
        this.battery_report_url = null;
        this.failed_ports = null;
    }

    public static LaptopTestResultRes fromEntity(LaptopTestResult result) {
        LaptopTestResultRes dto = new LaptopTestResultRes();
        dto.setBattery_report(result.getBatteryReport() != null && result.getBatteryReport());
        dto.setCamera_status(result.getCameraStatus() != null && result.getCameraStatus());
        dto.setCharging_status(result.getChargerStatus() != null && result.getChargerStatus());
        dto.setKeyboard_status(result.getKeyboardStatus() != null && result.getKeyboardStatus());
        dto.setUsb_status(result.getUsbStatus() != null && result.getUsbStatus());
        dto.setFailed_keys(result.getFailedKeys());
        dto.setBattery_report_url(result.getBatteryReportUrl());
        dto.setFailed_ports(result.getFailedPorts());
        return dto;

    }
}
