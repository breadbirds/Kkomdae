package pizza.kkomdae.dto.respond;


import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import pizza.kkomdae.entity.Device;
import pizza.kkomdae.entity.Laptop;
import pizza.kkomdae.entity.Phone;
import pizza.kkomdae.entity.Student;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Getter
@Setter
public class StudentWithRent {
    private long studentId;
    private String studentNum;
    private String name;
    private String region;
    private String classNum;
    private boolean status;
    private List<DeviceRentHistory> deviceRentHistory;

    public StudentWithRent(Student student) {
        this.studentId = student.getStudentId();
        this.studentNum = student.getStudentNum();
        this.name = student.getName();
        this.region = student.getRegion();
        this.classNum = student.getClassNum();
        this.status = true;
        this.deviceRentHistory = new ArrayList<>();
    }

    @Getter
    @Setter
    public static class DeviceRentHistory {
        private final long deviceId;
        private final String type;
        private final String modelCode;
        private final String serialNum;
        private boolean status = true;
        public DeviceRentHistory(Device device) {
            this.deviceId=device.getDeviceId();
            log.info("Device id : {}", deviceId);
            if (device instanceof Laptop laptop) {
                this.type = laptop.getDeviceType();
                this.modelCode = laptop.getModelCode();
                this.serialNum = laptop.getSerialNum();
            } else {
                Phone phone = (Phone) device;
                this.type = phone.getDeviceType();
                this.modelCode = phone.getModelCode();
                this.serialNum = phone.getSerialNum();
            }


        }
    }
}
