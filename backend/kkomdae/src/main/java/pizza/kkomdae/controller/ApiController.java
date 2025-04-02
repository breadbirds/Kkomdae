package pizza.kkomdae.controller;

import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import pizza.kkomdae.dto.request.ForthStageReq;
import pizza.kkomdae.dto.request.PhotoReq;
import pizza.kkomdae.dto.request.SecondStageReq;
import pizza.kkomdae.dto.request.TestResultReq;
import pizza.kkomdae.dto.request.ThirdStageReq;
import pizza.kkomdae.dto.respond.*;
import pizza.kkomdae.entity.Photo;
import pizza.kkomdae.s3.S3Service;
import pizza.kkomdae.security.dto.CustomUserDetails;
import pizza.kkomdae.service.*;
import javax.crypto.MacSpi;
import pizza.kkomdae.ssafyapi.MattermostNotificationService;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class ApiController {

    private final StudentService studentService;
    private final TestResultService testResultService;
    private final PhotoService photoService;
    private final S3Service s3Service;
    private final FlaskService flaskService;
    private final PdfService pdfService;
    private final MattermostNotificationService mattermostNotificationService;

    @GetMapping("/user-info")
    @Operation(summary = "첫페이지에서 유저의 정보를 조회하는 api", description = "")
    public UserRentTestInfo userInfo(@AuthenticationPrincipal CustomUserDetails userDetails) {
        log.info("jwt 토큰 유저 아이디 : {}", userDetails.getUserId());
        return studentService.getUserRentInfo(userDetails.getUserId());
    }

    @PostMapping("/test")
    @Operation(summary = "테스트 저장을 위한 테스트 생성", description = "테스트가 시작될 때 호출, 테스트 아이디를 반환합니다.<br>" +
            "테스트 아이디를 sharedPreference에 저장하고 사진 업로드할 때 사용 바랍니다.<br>" +
            "반납일 경우 serialNum을 넣어주면 됩니다. 그냥 대여일 시 아무 것도 없이 그냥 전송")
    public long initTest(@AuthenticationPrincipal CustomUserDetails userDetails, @RequestParam(required = false) String serialNum) {
        return testResultService.initTest(userDetails.getUserId(), serialNum);
    }


    @PostMapping(value = "/photo", consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "사진 업로드", description = "사진을 업로드하고 분석 및 저장 / type에 6(마지막 사진) 또는 -1 (음수) 전송 시 전체 Stage를 2로 변경")
    public ApiResponse uploadPhoto(
            @RequestParam("photoType") int photoType,
            @RequestParam("testId") long testId,
            @RequestPart(value = "image", required = false) MultipartFile image) {

        if (photoType < 0) {
            photoService.updateStageTo2(testId);
            return new ApiResponse(true, "파일 없이 stage 2 업데이트 완료");
        } else {
            PhotoReq photoReq = new PhotoReq();
            photoReq.setPhotoType(photoType);
            photoReq.setTestId(testId);
            Photo photo = photoService.uploadPhotoSync(photoReq, image);
            photoService.analyzePhoto(photo.getPhotoId());
            return new ApiResponse(true, "사진 업로드 및 저장 성공");
        }
    }

    @PostMapping(value = "/re-photo", consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "사진 재 업로드", description = "사진을 재 업로드하고 분석 및 저장 / 동기방식")
    public ApiResponse uploadRePhoto(
            @RequestParam("photoType") int photoType,
            @RequestParam("testId") long testId,
            @RequestPart(value = "image", required = false) MultipartFile image) {

            PhotoReq photoReq = new PhotoReq();
            photoReq.setPhotoType(photoType);
            photoReq.setTestId(testId);
            Photo photo = photoService.uploadPhotoSync(photoReq, image);
            photoService.analyzeRePhoto(photo.getPhotoId());

            AiPhotoWithUrl photo1 = testResultService.getAiPhoto(testId, photoType);

            Map<String, Object> result = new HashMap<>();
            result.put("photo_ai_name", photo1.getAiName());
            result.put("photo_ai_url", photo1.getUrl());
            result.put("photo_ai_damage", photo1.getDamage());

            return new ApiResponse(true, "사진 업로드 및 저장 성공", result);
    }


    @GetMapping("photo")
    @Operation(summary = "테스트 id로 테스트의 사진을 얻는 api", description = "List<Map<String,String>>으로 반환")
    public ApiResponse getPhoto(@RequestParam long testId) {
        // 1) photoList 가져오기
        List<PhotoWithUrl> photoList = testResultService.getPhotos(testId);
        // 2) 정렬
        photoList.sort(
                Comparator.comparingInt(PhotoWithUrl::getType)
        );
        // 3) 반환 맵 생성
        Map<String, String> resultList = new HashMap<>();
        // 4) 각 사진마다 매핑
        for (PhotoWithUrl photo : photoList) {
            int type = photo.getType();
            resultList.put("photo" + type +"_name", photo.getName());
            resultList.put("photo" + type +"_url", photo.getUrl());
        }

        return new ApiResponse(true, "사진 url 반환 완료", resultList);
    }

    @GetMapping("ai-photo")
    @Operation(summary = "테스트 아이디로 ai로 분석된 사진을 얻는 api", description = "List<String>으로 반환")
    public ApiResponse getAiPhoto(@RequestParam long testId) {
        // 1) 사진 조회 결과 가져오기
        List<AiPhotoWithUrl> photoList = testResultService.getAiPhotos(testId);
        // 2) 정렬하기
        photoList.sort(
                Comparator.comparingInt(AiPhotoWithUrl::getType)
        );
        // 3) 반환 리스트 생성
        Map<String, Object> resultList = new HashMap<>();
        // 4) 각 사진 매핑
        for (AiPhotoWithUrl photo : photoList) {
            int type = photo.getType();
            resultList.put("photo" + type +"_ai_name", photo.getAiName());
            resultList.put("photo" + type +"_ai_url", photo.getUrl());
            resultList.put("photo" + type +"_ai_damage", photo.getDamage());
        }
        return new ApiResponse(true, "분석 사진 url 반환 완료", resultList);
    }

    @Operation(summary = "파일 이름으로 URL 반환", description = "파일 이름으로 url을 돌려받기")
    @GetMapping("/test-file/{file-name}")
    public UrlResponse getFileName(@PathVariable("file-name") String fileName) {
        // S3에서 URL 생성
        String url = s3Service.generatePresignedUrl(fileName);
        // UrlResponse 객체에 담아 반환
        return new UrlResponse(url);
    }

    @Operation(summary = "qr 정보 입력", description = "2단계 qr 정보 입력 및 단계 저장")
    @PostMapping("/secondStage")
    public ApiResponse secondStage(@AuthenticationPrincipal CustomUserDetails userDetails, @RequestBody SecondStageReq secondStageReq) {
        testResultService.secondStage(userDetails, secondStageReq);
        return new ApiResponse(true,"qr 정보 입력 성공");
    }

    @Operation(summary = "기기 정보 입력", description = "기기 모델명, 시리얼 넘버 등의 정보를 받는 api")
    @PostMapping("/thirdStage")
    public ApiResponse thirdStage(@AuthenticationPrincipal CustomUserDetails userDetails, @RequestBody ThirdStageReq thirdStageReq) {
        testResultService.thirdStage(userDetails, thirdStageReq);
        return new ApiResponse(true, "기기 정보 입력 성공");
    }

    @Operation(summary = "비고 입력",description = "기타 적고 싶은 사항을 적는 곳")
    @PostMapping("/fourthStage")
    public ApiResponse fourthStage(@AuthenticationPrincipal CustomUserDetails userDetails, @RequestBody ForthStageReq forthStageReq) {
        testResultService.fourthStage(forthStageReq);
        return new ApiResponse(true, "비고 입력 성공");
    }

    @Operation(summary = "pdf 생성", description = "testId로 절차를 종료하고 pdf를 생성합니다.")
    @PostMapping("/pdf/{testId}")
    public ApiResponse makePdf(@PathVariable long testId) {
        return new ApiResponse(true, pdfService.makeAndUploadPdf(testId));
    }

    @Operation(summary = "랜덤 키 생성", description = "노트북과 앱 연결을 위한 키 생성")
    @PostMapping("/random-key/{testId}")
    public ApiResponse randomKey(@PathVariable long testId) {
        String randomKey = testResultService.randomKey(testId);
        Map<String, String> result = new HashMap<>();
        result.put("randomKey", randomKey);

        return new ApiResponse(true, "랜덤 키 생성", result);
    }

    @GetMapping("/verify-key")
    @Operation(summary = "랜덤 키 검증", description = "생성된 랜덤 키의 유효성을 검증")
    public ApiResponse verifyKey(@RequestParam String key) {
        boolean isValid = testResultService.verifyRandomKey(key);
        Map<String, String> result = new HashMap<>();
        result.put("isValid", isValid + "");
        return new ApiResponse(isValid, isValid ? "유효한 키입니다" : "유효하지 않은 키입니다",result);
    }

    @PostMapping("/test-result")
    @Operation(
            summary = "테스트 결과 저장",
            description = "각 테스트(키보드, 카메라 등)의 결과를 저장합니다."
    )
    public ApiResponse updateTestResult(@RequestBody TestResultReq testResultReq) {
        try {
            testResultService.updateTestResult(testResultReq);
            return new ApiResponse(
                    true,
                    "테스트 결과가 성공적으로 저장되었습니다."
            );
        } catch (RuntimeException e) {
            return new ApiResponse(
                    false,
                    e.getMessage()
            );
        }
    }

    @Operation(summary = "테스트 최종 결과", description = "테스트 최종 결과를 반환")
    @GetMapping("/laptopTotalResult")
    public LaptopTotalResultRes laptopTotalResult(@RequestParam long testId ) {
        return testResultService.laptopTotalResult(testId);
    }
    @Operation(summary = "관리자 페이지 알림 테스트용", description = "리스트에 교육생 이름을 넣으면 그룹을 만들어서 알림 발송")
    @PostMapping("/notification")
    public void notification(@RequestBody List<String>names) {
//        mattermostNotificationService.createGroupChannel(names);
        mattermostNotificationService.sendGroupMessage(names);
    }
}

