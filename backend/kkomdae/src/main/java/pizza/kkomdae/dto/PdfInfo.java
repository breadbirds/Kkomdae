package pizza.kkomdae.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import pizza.kkomdae.entity.*;

import java.time.LocalDate;
import java.util.List;

@Slf4j
@Getter
@Setter
public class PdfInfo {
    private String name;
    private String region;
    private String email;
    private String serial;
    private String barcode;
    private int laptopCount;
    private int powerCableCount;
    private int adapterCount;
    private int mouseCount;
    private int bagCount;
    private int mousePadCount;
    private String description;
    private LocalDate returnDate;
    private LocalDate rentDate;
    private List<Photo>photos;
    private boolean release;
    private int rentLaptopCount;
    private int rentPowerCableCount;
    private int rentAdapterCount;
    private int rentMouseCount;
    private int rentBagCount;
    private int rentMousePadCount;

    public PdfInfo(LaptopTestResult result, LaptopTestResult rent) {
        Student student = result.getStudent();
        this.name = student.getName();
        this.region = student.getRegion();
        this.email = student.getEmail();
        Device device = result.getDevice();
        this.serial = device.getSerialNum();
        Laptop laptop = (Laptop) device;
        this.barcode = laptop.getLaptopBarcodeNum();
        this.laptopCount = result.getLaptop();
        this.powerCableCount = result.getPowerCable();
        this.adapterCount = result.getAdapter();
        this.mouseCount = result.getMouse();
        this.bagCount = result.getBag();
        this.mousePadCount = result.getBag();
        this.photos = result.getPhotos();
        this.release=result.getRelease();
        if (result.getRelease()) {
            this.rentDate = rent.getDate();
            this.returnDate = result.getDate();
            log.info("rentDate : {}",this.rentDate);
            log.info("returnDate : {}",this.returnDate);
        }else{
            this.rentDate = result.getDate();
            log.info("rentDate : {}",this.rentDate);
        }
        this.description = result.getDescription();
        this.rentMousePadCount = rent.getMousePad();
        this.rentMouseCount = rent.getMouse();
        this.rentBagCount = rent.getBag();
        this.rentAdapterCount = rent.getAdapter();
        this.rentLaptopCount = rent.getLaptop();
        this.rentPowerCableCount = rent.getPowerCable();
    }
}
