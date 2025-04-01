package pizza.kkomdae.service;

import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import pizza.kkomdae.dto.request.AiPhotoInfo;
import pizza.kkomdae.dto.request.FlaskRequest;
import pizza.kkomdae.dto.request.PhotoReq;
import pizza.kkomdae.dto.respond.FlaskResponse;
import pizza.kkomdae.dto.respond.UserRentTestInfo;
import pizza.kkomdae.entity.LaptopTestResult;
import pizza.kkomdae.entity.Photo;
import pizza.kkomdae.repository.PhotoRepository;
import pizza.kkomdae.repository.laptopresult.LapTopTestResultRepository;
import pizza.kkomdae.s3.S3Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class PhotoService {

    private final PhotoRepository photoRepository;
    private final LapTopTestResultRepository lapTopTestResultRepository;
    private final S3Service s3Service;
    private final FlaskService flaskService;

    @Transactional
    public Photo uploadPhotoSync(PhotoReq photoreq, MultipartFile image) {
        // 1) S3 업로드 후 파일 이름 반환
        String s3FileName = s3Service.upload(photoreq, image);
        if (s3FileName == null) {
            throw new RuntimeException("Failed to upload photo");
        }

        // 2) LaptopTestResult(=test) 조회
        LaptopTestResult test = lapTopTestResultRepository.getReferenceById(photoreq.getTestId());

        // 3)  동일한 test, type을 가진 사진이 있는지 확인
        Optional<Photo> optionalPhoto =
                photoRepository.findByLaptopTestResultAndType(test, photoreq.getPhotoType());

        // 4) Photo 객체 준비
        // 존재 여부에 따라 업데이트, 생성 구분
        Photo photo;
        if (optionalPhoto.isPresent()) {
            photo = optionalPhoto.get();
        } else {
            photo = new Photo();
            photo.setLaptopTestResult(test);
            photo.setType(photoreq.getPhotoType());
        }

        // 5) S3 key에서 prefix를 제거한 파일명 설정
        String fileNameWithoutPrefix = s3FileName.replace("lkm7ln/", "");
        photo.setName(fileNameWithoutPrefix);

        // 6) 사진 단계 저장
        // 업로드된 사진 단계 확인
        int updateStage = photoreq.getPhotoType();
        // 저장된 사진 단계 확인
        int savedStage = test.getPicStage();
        // 업로드된 사진의 스테이지가 6일 때 (마지막 사진일 때)
        if (updateStage == 6 && test.getStage() == 1) {
            test.setStage(2);
        }
        // 업로드된 사진의 단계가 높으면 단계를 업데이트
        if (savedStage < updateStage) {
            test.setPicStage(photoreq.getPhotoType());
        }

        // 7) DB 저장
        photoRepository.save(photo);
        lapTopTestResultRepository.save(test);

        return photo;
    }

    @Transactional
    public void updateStageTo2(long testId) {
        // testId로 LaptopTestResult 조회 후 stage 업데이트
        LaptopTestResult result = lapTopTestResultRepository.findById(testId)
                .orElseThrow(() -> new RuntimeException("Failed to find test result"));
        result.setStage(2);
    }

    @Async
    public void analyzePhoto(Long photoId) {
        Photo photo = photoRepository.findById(photoId)
                .orElseThrow(() -> new RuntimeException("no photo found"));

        // FlaskRequest 객체 생성 (예: S3 key를 사용)
        FlaskRequest flaskRequest = new FlaskRequest();
        flaskRequest.setS3Key(photo.getName());

        // Flask 서버로 분석 요청
        FlaskResponse response = flaskService.analyzeImage(flaskRequest);

        // AI 분석 결과를 DB 업데이트
        photo.setAiName(response.getUploadName());
        photo.setDamage(response.getDamage());

        photoRepository.save(photo);
    }

    public void analyzeRePhoto(Long photoId) {
        Photo photo = photoRepository.findById(photoId)
                .orElseThrow(() -> new RuntimeException("no photo found"));

        // FlaskRequest 객체 생성 (예: S3 key를 사용)
        FlaskRequest flaskRequest = new FlaskRequest();
        flaskRequest.setS3Key(photo.getName());

        // Flask 서버로 분석 요청
        FlaskResponse response = flaskService.analyzeImage(flaskRequest);

        // AI 분석 결과를 DB 업데이트
        photo.setAiName(response.getUploadName());
        photo.setDamage(response.getDamage());

        photoRepository.save(photo);
    }
}
