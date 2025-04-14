package pizza.kkomdae.dto.respond;

import lombok.Getter;
import pizza.kkomdae.entity.LaptopTestResult;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

@Getter
public class LaptopTestResultWithStudent {
    private final long laptopTestId;
    private final boolean release;
    private final String studentRegion;
    private final String studentName;
    private final String studentNum;
    private String failedKeys = "";
    private String failedPorts = "";
    private final boolean cameraStatus;
    private String batteryPdfUrl;
    private String resultPdfUrl;
    private final LocalDate date;
    private String sumOfDamages;
    private String batteryStatus;

    public LaptopTestResultWithStudent(LaptopTestResult laptopTestResult) {
        this.laptopTestId = laptopTestResult.getLaptopTestResultId();
        this.studentRegion = laptopTestResult.getStudent().getRegion();
        this.studentName = laptopTestResult.getStudent().getName();
        this.studentNum = laptopTestResult.getStudent().getStudentNum();
        this.release = laptopTestResult.getRelease();
        if (laptopTestResult.getFailedKeys() != null) this.failedKeys = laptopTestResult.getFailedKeys();
        if (laptopTestResult.getFailedPorts() != null) this.failedPorts = laptopTestResult.getFailedPorts();
        this.cameraStatus = laptopTestResult.getCameraStatus();
        this.date = laptopTestResult.getDate();
        if (laptopTestResult.getSumOfDamages() != null) {
            this.sumOfDamages = Integer.toString(laptopTestResult.getSumOfDamages());
        } else {
            this.sumOfDamages = "테스트 중";
        }
        this.batteryStatus = abstractLife(laptopTestResult.getBatteryReportSummary());

    }

    private String abstractLife(String summary) {
        StringTokenizer s = new StringTokenizer(summary);
        List<String> list = new ArrayList<>();
        while (s.hasMoreTokens()) {
            String token = s.nextToken();
            list.add(token);
        }
        return list.get(13);
    }

    public void setResultPdfUrl(String resultPdfUrl) {
        this.resultPdfUrl = resultPdfUrl;
    }

    public void setBatteryPdfUrl(String batteryPdfUrl) {
        this.batteryPdfUrl = batteryPdfUrl;
    }
}
