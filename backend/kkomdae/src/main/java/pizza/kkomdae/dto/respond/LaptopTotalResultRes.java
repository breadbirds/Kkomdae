package pizza.kkomdae.dto.respond;


import lombok.Getter;
import lombok.Setter;
import pizza.kkomdae.entity.Laptop;
import pizza.kkomdae.entity.LaptopTestResult;
import pizza.kkomdae.entity.Photo;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class LaptopTotalResultRes {
    private boolean keyboardStatus;
    private boolean useStatus;
    private boolean cameraStatus;
    private boolean batteryStatus;
    private boolean chargerStatus;
    private String modelCode;
    private String serialNum;
    private String barcodeNum;
    private LocalDate date;
    private int laptopCount;
    private int mouseCount;
    private int powerCableCount;
    private int bagCount;
    private int adapterCount;
    private int mousepadCount;
    private String description;
    private List<String> imageUrls;

    public LaptopTotalResultRes(LaptopTestResult result) {
        Laptop laptop = (Laptop) result.getDevice();
        this.imageUrls = new ArrayList<>();
        this.keyboardStatus = result.getKeyboardStatus();
        this.useStatus = result.getUsbStatus();
        this.cameraStatus = result.getCameraStatus();
        this.batteryStatus = result.getBatteryReport();
        this.chargerStatus = result.getChargerStatus();
        this.modelCode = laptop.getModelCode();
        this.serialNum = laptop.getSerialNum();
        this.barcodeNum = laptop.getLaptopBarcodeNum();
        this.date = result.getDate();
        this.laptopCount = result.getLaptop();
        this.mouseCount = result.getMouse();
        this.mousepadCount = result.getMousePad();
        this.powerCableCount = result.getPowerCable();
        this.bagCount = result.getBag();
        this.adapterCount = result.getAdapter();
        this.description = result.getDescription();
    }
}
