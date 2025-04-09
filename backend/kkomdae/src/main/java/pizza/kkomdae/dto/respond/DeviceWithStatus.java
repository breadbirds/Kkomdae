package pizza.kkomdae.dto.respond;

import lombok.Getter;
import pizza.kkomdae.entity.Device;
import pizza.kkomdae.entity.Laptop;
@Getter
public class DeviceWithStatus {
    private final long deviceId;
    private final String deviceType;
    private final String modelCode;
    private final String serialNum;
    private final String barcodeNum;
    private final boolean release;

    public DeviceWithStatus(Device device) {
        this.deviceId = device.getDeviceId();
        this.deviceType = device.getDeviceType();
        this.modelCode = device.getModelCode();
        this.serialNum = device.getSerialNum();
        if(device.getDeviceType().equals("Laptop")) {
            Laptop laptop = (Laptop) device;
            this.barcodeNum = laptop.getLaptopBarcodeNum();
        }else{
            this.barcodeNum=null;
        }
        this.release = device.isRelease();
    }
}
