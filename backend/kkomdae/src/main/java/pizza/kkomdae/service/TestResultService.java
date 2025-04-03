package pizza.kkomdae.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pizza.kkomdae.dto.request.ForthStageReq;
import pizza.kkomdae.dto.request.SecondStageReq;
import pizza.kkomdae.dto.request.TestResultReq;
import pizza.kkomdae.dto.request.ThirdStageReq;
import pizza.kkomdae.dto.respond.AiPhotoWithUrl;
import pizza.kkomdae.dto.respond.LaptopTestResultWithStudent;
import pizza.kkomdae.dto.respond.LaptopTotalResultRes;
import pizza.kkomdae.dto.respond.PhotoWithUrl;
import pizza.kkomdae.entity.*;
import pizza.kkomdae.repository.PhotoRepository;
import pizza.kkomdae.repository.device.DeviceRepository;
import pizza.kkomdae.repository.rent.RentRepository;
import pizza.kkomdae.repository.student.StudentRepository;
import pizza.kkomdae.repository.laptopresult.LapTopTestResultRepository;
import pizza.kkomdae.s3.S3Service;
import pizza.kkomdae.security.dto.CustomUserDetails;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class TestResultService {
    private final LapTopTestResultRepository lapTopTestResultRepository;
    private final StudentRepository studentRepository;
    private final DeviceRepository deviceRepository;
    private final PhotoRepository photoRepository;
    private final RentRepository rentRepository;
    private final S3Service s3Service;

    public List<LaptopTestResultWithStudent> getByStudentOrDevice(Long studentId, Long deviceId, String deviceType) {
        Student referenceStudentById = null;
        if (studentId != null) {
            referenceStudentById = studentRepository.getReferenceById(studentId);
        }
        Device referenceDeviceById = null;
        if (deviceId != null) {
            referenceDeviceById = deviceRepository.getReferenceById(deviceId);
        }
        List<LaptopTestResult> laptopTestResults = lapTopTestResultRepository.findByStudentOrDevice(referenceStudentById, referenceDeviceById);
        List<LaptopTestResultWithStudent> results = new ArrayList<>();
        for (LaptopTestResult laptopTestResult : laptopTestResults) {
            LaptopTestResultWithStudent laptopTestResultWithStudent = new LaptopTestResultWithStudent(laptopTestResult);
            laptopTestResultWithStudent.setResultPdfUrl(s3Service.generatePresignedUrl(laptopTestResult.getPdfFileName()));
            results.add(laptopTestResultWithStudent);
        }
        return results;
    }

    @Transactional
    public long initTest(long userId, String serialNum) {
        Student student = studentRepository.getReferenceById(userId);
        LaptopTestResult laptopTestResult = lapTopTestResultRepository.findByStudentAndStageIsLessThan(student, 6);
        if (laptopTestResult == null) {
            laptopTestResult = new LaptopTestResult(student);
            if (serialNum != null) {
                Device device = deviceRepository.findDeviceBySerialNum(serialNum);
                laptopTestResult.setDevice(device);
                laptopTestResult.setRelease(true);
            }
            lapTopTestResultRepository.save(laptopTestResult);
            laptopTestResult.setStage(1);
        }
        return laptopTestResult.getLaptopTestResultId();
    }

    public List<PhotoWithUrl> getPhotos(long testId) {
        // 테스트에 해당하는 laptopTestResult 조회
        LaptopTestResult laptopResult = lapTopTestResultRepository.getReferenceById(testId);
        // 해당하는 테스트에 연관된 Photo 목록 조회
        List<Photo> photos = photoRepository.getPhotosByLaptopTestResult(laptopResult);

        // 각 Photo마다 presigned URL 생성 후 PhotoWithUrl DTO로 변환
        return photos.stream()
                .map(photo -> {
                    String presignedUrl = s3Service.generatePresignedUrl(photo.getName());
                    return new PhotoWithUrl(photo, presignedUrl);
                })
                .collect(Collectors.toList());
    }

    public List<AiPhotoWithUrl> getAiPhotos(long testId) {
        LaptopTestResult laptopResult = lapTopTestResultRepository.getReferenceById(testId);
        List<Photo> photos = photoRepository.getPhotosByLaptopTestResult(laptopResult);
        return photos.stream()
                .map(photo -> {
                    String presignedUrl = s3Service.generatePresignedUrl(photo.getAiName());
                    return new AiPhotoWithUrl(photo, presignedUrl);
                })
                .collect(Collectors.toList());
    }

    public AiPhotoWithUrl getAiPhoto(long testId, int type) {
        LaptopTestResult laptopResult = lapTopTestResultRepository.getReferenceById(testId);
        Photo photo = photoRepository.getPhotoByLaptopTestResultAndType(laptopResult, type);
        String presignedUrl = s3Service.generatePresignedUrl(photo.getAiName());
        return new AiPhotoWithUrl(photo, presignedUrl);
    }

    @Transactional
    public void secondStage(CustomUserDetails userDetails, SecondStageReq secondStageReq) {
        Student student = studentRepository.getReferenceById(userDetails.getUserId());
        LaptopTestResult testResult = lapTopTestResultRepository.findByStudentAndLaptopTestResultId(student, secondStageReq.getTestId());
        if (testResult == null) {
            throw new RuntimeException("저장된 테스트 없음");
        }
        testResult.saveSecondStage(secondStageReq);

    }

    @Transactional
    public void thirdStage(CustomUserDetails userDetails, ThirdStageReq thirdStageReq) {
        Student student = studentRepository.getReferenceById(userDetails.getUserId());
        LaptopTestResult testResult = lapTopTestResultRepository.findByStudentAndLaptopTestResultId(student, thirdStageReq.getTestId());
        if (testResult == null) {
            throw new RuntimeException("저장된 테스트 없음");
        }
        testResult.saveThirdStage(thirdStageReq);
        Device device = deviceRepository.findDeviceBySerialNum(thirdStageReq.getSerialNum());
        if (device == null) {
            throw new RuntimeException("deivce 시리얼 에러");
        }
        Rent rent;
        if (testResult.getRelease() == false) { // 대여로직
            rent = new Rent();
            rent.setStudent(student);
            rent.setRentDateTime(thirdStageReq.getLocalDate());
            testResult.setDevice(device);
            rent.setDevice(device);
        } else { // 반납 로직
            rent = rentRepository.findByDeviceAndStudent(device, student);
            rent.setReleaseDateTime(testResult.getDate());
        }

        rentRepository.save(rent);
    }

    @Transactional
    public String randomKey(long testId) {
        // DB에서 해당 testId의 LaptopTestResult 조회
        LaptopTestResult testResult = lapTopTestResultRepository.findById(testId)
            .orElseThrow(() -> new RuntimeException("해당 테스트 결과가 존재하지 않습니다."));

        // 알파벳 소문자 (a-z)
        char letter = (char) ('a' + Math.random() * 26);
        // 9000까지 의 랜덤 숫자 생성 후 1000을 더해 4자리 숫자 생성
        int number = (int) (Math.random() * 9000) + 1000;

        // 랜덤 키 생성
        String random_key = String.format("%c%d", letter, number);

        // 랜덤 키 저장
        testResult.setRandomKey(random_key);

        // DB에 저장
        lapTopTestResultRepository.save(testResult);

        // 랜덤 키를 반환
        return random_key;
    }

    @Transactional(readOnly = true)
    public boolean verifyRandomKey(String key) {
        // DB에서 랜덤 키가 존재하는지 확인
        return lapTopTestResultRepository.findByRandomKey(key).isPresent();
    }

    @Transactional
    public void updateTestResult(TestResultReq testResultReq) {
        // 1. 입력값 검증
        if (testResultReq.getRandomKey() == null || testResultReq.getRandomKey().isEmpty()) {
            throw new RuntimeException("랜덤키가 필요합니다.");
        }

        // 2. 테스트 결과 조회
        LaptopTestResult testResult = lapTopTestResultRepository
                .findByRandomKey(testResultReq.getRandomKey())
                .orElseThrow(() -> new RuntimeException("저장된 테스트가 없습니다."));

        // 3. 테스트 결과 업데이트
        testResult.updateTestResult(
                testResultReq.getTestType(),
                testResultReq.isSuccess(),
                (List) testResultReq.getDetail(),
                testResultReq.getSummary()
        );

        // 4. 저장
        lapTopTestResultRepository.save(testResult);

        // 5. 로깅 추가
        log.info("테스트 결과 업데이트 완료 - randomKey: {}, testType: {}, success: {}",
                testResultReq.getRandomKey(),
                testResultReq.getTestType(),
                testResultReq.isSuccess()
        );
    }

    @Transactional
    public void fourthStage(ForthStageReq forthStageReq) {
        LaptopTestResult result = lapTopTestResultRepository.findById(forthStageReq.getTestId()).orElseThrow(()->new RuntimeException("testId 오류"));
        result.setDescription(forthStageReq.getDescription());
        result.setStage(5);
    }

    public LaptopTotalResultRes laptopTotalResult(long testId) {
        LaptopTestResult result = lapTopTestResultRepository.findByIdWithStudentAndDeviceAndPhotos(testId);
        LaptopTotalResultRes res = new LaptopTotalResultRes(result);
        List<Photo> photos = result.getPhotos();
        List<String> urls = new ArrayList<>();
        for (Photo photo : photos) {
            urls.add(s3Service.generatePresignedUrl(photo.getName()));
        }
        res.setImageUrls(urls);
        
        return res;
    }
}
