package pizza.kkomdae.controller;

import io.swagger.v3.oas.annotations.Hidden;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import pizza.kkomdae.dto.request.DeviceCond;
import pizza.kkomdae.dto.request.StudentWithRentCond;
import pizza.kkomdae.dto.respond.DeviceWithStatus;
import pizza.kkomdae.dto.respond.LaptopTestResultWithStudent;
import pizza.kkomdae.dto.respond.PhotoWithUrl;
import pizza.kkomdae.dto.respond.StudentWithRent;
import pizza.kkomdae.entity.*;
import pizza.kkomdae.service.*;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Controller
@Hidden
@RequiredArgsConstructor
@RequestMapping("/api/admin")
public class AdminController {

    private final TestResultService testResultService;
    private final StudentService studentService;
    private final AdminService adminService;
    private final DeviceService deviceService;
    private final RentService rentService;
    private final MattermostNotificationService mattermostNotificationService;

    // 관리자 로그인 페이지
    @GetMapping("/index")
    public String login() {
        return "index";
    }

    // pdf 정보 입력 페이지 테스트용
    @GetMapping("/pdf-page")
    public String pdfPage() {
        return "pdf-page";
    }

    // adminCode 확인로직
    @PostMapping("/verify")
    public String verify(@RequestParam String adminCode) {
        Admin byCode = adminService.getByCode(adminCode);
        //TODO 레디스 세션 추가
        if (byCode != null) {
            return "redirect:/api/admin/students";
        } else {
            return "redirect:/error";
        }
    }
    // 세션 삭제
    @PostMapping("/logout")
    public String logout() {
        //Todo 세션 삭제
        return "redirect:/admin/index";
    }

    // 학생목록 + 학생 별 대여 품목
    @GetMapping("/students")
    public String students(StudentWithRentCond studentWithRentCond, Model model) {
        List<StudentWithRent> results = rentService.getRentByStudent(studentWithRentCond);
        log.info("{}", results.size());
        model.addAttribute("students", results);
        return "students";
    }
    

    @GetMapping("/devices")
    public String devices(DeviceCond deviceCond, Model model) {
        List<DeviceWithStatus> results = deviceService.getDevicesWithCond(deviceCond);
        model.addAttribute("deviceList", results);
        return "devices";
    }

    @GetMapping("/test-results")
    public String testResults(HttpServletRequest request, @RequestParam(required = false) Long studentId,
                              @RequestParam(required = false) Long deviceId, @RequestParam String deviceType, Model model) {
        List<LaptopTestResultWithStudent> laptopTestResultWithStudent = testResultService.getByStudentOrDevice(studentId, deviceId, deviceType);
        String referer = request.getHeader("referer");
        model.addAttribute("referer", referer);
        model.addAttribute("laptopTestResultWithStudent", laptopTestResultWithStudent);
        model.addAttribute("studentId", studentId);
        model.addAttribute("deviceId", deviceId);
        return "test-results";
    }

    @GetMapping("/photos")
    public String photos(@RequestParam long testResultId, Model model) {
//        TODO 실제 사진 링크로 변경
        List<PhotoWithUrl> photos = testResultService.getPhotos(testResultId);

        log.info("urls size : {}", photos.size());
        model.addAttribute("photos", photos);
        return "photos";
    }

    @GetMapping("/index-hj")
    public String indexHj() {
        return "index-hj";
    }

    @GetMapping("/login-hj")
    public String loginHj() {
        return "login-hj";
    }

    @GetMapping("/login")
    public String ssafyLogin() {
        return "index-ss.html";
    }

    @GetMapping("/login-local")
    public String ssafyLoginLocal() {
        return "index-redirect-local.html";
    }
    /**
     * 선택한 학생들에게 Mattermost 알림을 전송하는 엔드포인트
     * (예: 학생 목록 페이지에서 "알림 발송" 버튼 클릭 시 호출)
     *
     * @param selectedStudents 선택된 학생들의 ID 리스트 (폼 파라미터)
     * @param redirectAttributes 리다이렉트 후 메시지 전달
     * @return 학생 목록 페이지로 리다이렉트
     */
    @PostMapping("/notification")
    public String sendNotification(
            @RequestParam("selectedStudents") List<Long> selectedStudents,
            RedirectAttributes redirectAttributes) {

        try {
            // 예시: 학생 ID를 Mattermost 사용자 ID로 변환
            // 실제 프로젝트에서는 studentService를 통해 학생의 MM 사용자 ID를 조회해야 함
            List<String> mmUserIds = selectedStudents.stream()
                    .map(String::valueOf) // 여기서는 단순히 Long 값을 문자열로 변환하는 예시
                    .collect(Collectors.toList());

            String message = "안녕하세요! 대여 장비 관련 알림입니다.";  // 발송할 메시지 내용
            mattermostNotificationService.sendNotification(mmUserIds, message);

            redirectAttributes.addFlashAttribute("message", "알림 발송 완료");
        } catch (Exception e) {
            log.error("알림 발송 실패", e);
            redirectAttributes.addFlashAttribute("error", "알림 발송 실패");
        }
        return "redirect:/api/admin/students";
    }
}
