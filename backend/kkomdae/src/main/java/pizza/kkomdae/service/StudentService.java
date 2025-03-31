package pizza.kkomdae.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import pizza.kkomdae.dto.request.StudentWithRentCond;
import pizza.kkomdae.dto.respond.StudentWithRent;
import pizza.kkomdae.dto.respond.UserRentTestInfo;
import pizza.kkomdae.dto.respond.UserRentTestRes;
import pizza.kkomdae.entity.LaptopTestResult;
import pizza.kkomdae.entity.Photo;
import pizza.kkomdae.entity.Rent;
import pizza.kkomdae.entity.Student;
import pizza.kkomdae.repository.laptopresult.LapTopTestResultRepository;
import pizza.kkomdae.repository.rent.RentRepository;
import pizza.kkomdae.repository.student.StudentRepository;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class StudentService {

    private final StudentRepository studentRepository;
    private final RentRepository rentRepository;
    private final LapTopTestResultRepository lapTopTestResultRepository;

    // 노트북 현황은 반납하지 않은 rent 값이 true인 것이 있으면
    public List<StudentWithRent> findByKeyword(String searchType, String searchKeyword) {
        List<Student> byKeywordWithStatus = studentRepository.findByKeywordWithStatus(searchType, searchKeyword);

        List<StudentWithRent> results = new ArrayList<>();
        for (Student student : byKeywordWithStatus) {
            StudentWithRent tmp = new StudentWithRent(student);
            tmp.setStatus(true);
            for (Rent rent : student.getRent()) {
                if (rent.getReleaseDateTime() == null) {
                    tmp.setStatus(false);
                    break;
                }
            }
            results.add(tmp);
        }
        return results;
    }

    public UserRentTestInfo getUserRentInfo(long studentId) {
        Student student = studentRepository.getReferenceById(studentId);
        StudentWithRentCond cond = new StudentWithRentCond();
        cond.setStudent(student);
        List<Rent> laptopTestResults = rentRepository.getRentsByStudentInfo(cond);
        List<UserRentTestRes> results = new ArrayList<>();
        for (Rent rent : laptopTestResults) {
            results.add(new UserRentTestRes(rent));
        }
        UserRentTestInfo info = new UserRentTestInfo();
        info.setName(student.getName());
        info.setUserRentTestRes(results);
        log.info("진행 중인 대여 절차 확인 쿼리");
        LaptopTestResult testResult = lapTopTestResultRepository.findByStudentAndStageIsLessThanAndDeviceIsNull(student, 6);
        if (testResult != null) {
            log.info("진행 중인 대여 절차 쿼리 정보 {} {}", testResult.getStage(), testResult.getLaptopTestResultId());
            info.setOnGoingTestId(testResult.getLaptopTestResultId());
            info.setStage(testResult.getStage());
            info.setPicStage(testResult.getPicStage());
        }
        return info;

    }

}

