package pizza.kkomdae.dto.respond;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import pizza.kkomdae.entity.LaptopTestResult;
import pizza.kkomdae.entity.Rent;

import java.time.LocalDate;

@Slf4j
@Getter
public class UserRentTestRes {
    private final String modelCode;
    private final LocalDate dateTime;
    private boolean release;
    private final String rentPdfName;
    private String releasePdfName;
    private long onGoingTestId = 0;
    private int stage = 0;
    private int picStage = 0;

    public UserRentTestRes(Rent rent) {
        this.modelCode = rent.getDevice().getModelCode();
        this.rentPdfName = rent.getDevice().getLaptopTestResults().get(0).getPdfFileName();
//        log.info("테스트 번호 {}",rent.getDevice().getLaptopTestResults().get(0).getLaptopTestResultId());
        if (rent.getReleaseDateTime() != null) { // 반납했다면
            this.dateTime = rent.getReleaseDateTime();
            this.release = true;
            this.releasePdfName = rent.getDevice().getLaptopTestResults().get(1).getPdfFileName();
        } else {
            this.dateTime = rent.getRentDateTime();
            this.release = false;
        }
        if (rent.getDevice().getLaptopTestResults().size() > 1) { // 진행 중인 테스트가 있다면 getRentsByStudentInfo order by를 device Id, laptopId로 해두어서 0번이 대여, 1번이 반납인 것을 확정
            LaptopTestResult result = rent.getDevice().getLaptopTestResults().get(1);
            this.onGoingTestId = result.getLaptopTestResultId();
            this.stage = result.getStage();
            this.picStage = result.getPhotos().size();
        }
    }
}
