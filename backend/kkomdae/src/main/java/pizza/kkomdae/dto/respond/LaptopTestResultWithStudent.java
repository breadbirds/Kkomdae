package pizza.kkomdae.dto.respond;

import lombok.Getter;
import pizza.kkomdae.entity.LaptopTestResult;

import java.time.LocalDate;
@Getter
public class LaptopTestResultWithStudent {
    private final long laptopTestId;
    private final boolean release;
    private final String studentRegion;
    private final String studentName;
    private final String studentNum;
    private final String failedKeys;
    private final String failedPorts;
    private final boolean cameraStatus;
    private String batteryPdfUrl;
    private  String resultPdfUrl;
    private final LocalDate date;

    public LaptopTestResultWithStudent(LaptopTestResult laptopTestResult) {
        this.laptopTestId = laptopTestResult.getLaptopTestResultId();
        this.studentRegion = laptopTestResult.getStudent().getRegion();
        this.studentName = laptopTestResult.getStudent().getName();
        this.studentNum = laptopTestResult.getStudent().getStudentNum();
        this.release = laptopTestResult.getRelease();
        this.failedKeys = laptopTestResult.getFailedKeys();
        this.failedPorts = laptopTestResult.getFailedPorts();
        this.cameraStatus = laptopTestResult.getCameraStatus();
        this.date = laptopTestResult.getDate();
    }

    public void setResultPdfUrl(String resultPdfUrl) {
        this.resultPdfUrl = resultPdfUrl;
    }

    public void setBatteryPdfUrl(String batteryPdfUrl) {
        this.batteryPdfUrl = resultPdfUrl;
    }
}
