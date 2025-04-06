package pizza.kkomdae.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import pizza.kkomdae.dto.request.StudentWithRentCond;
import pizza.kkomdae.dto.respond.StudentWithRent;
import pizza.kkomdae.entity.*;
import pizza.kkomdae.repository.rent.RentRepository;
import pizza.kkomdae.repository.student.StudentRepository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class RentService {
    private final RentRepository rentRepository;
    private final StudentRepository studentRepository;


    public List<StudentWithRent> getStudentsWithRent(StudentWithRentCond studentWithRentCond) {
        List<Student> students = studentRepository.getStudentsByStudentInfo(studentWithRentCond);
        List<Rent> rents = rentRepository.getRentsByStudentInfo(studentWithRentCond);
        Map<Student, List<Rent>> studentAndRents = new HashMap<>();
        for (Student student : students) {
            studentAndRents.put(student, new ArrayList<>());
        }
        for (Rent rent : rents) {
            List<Rent> list = studentAndRents.get(rent.getStudent());
            list.add(rent);
        }
        List<StudentWithRent> results = new ArrayList<>();

        StudentWithRent studentWithRent = null;

        for (Student student : students) {
            List<Rent> rentList = studentAndRents.get(student);
            studentWithRent = new StudentWithRent(student);
            for (Rent rent : rentList) {
                studentWithRent.getDeviceRentHistory().add(new StudentWithRent.DeviceRentHistory(rent.getDevice()));
                if (rent.getReleaseDateTime() == null) studentWithRent.setStatus(false);
            }
            results.add(studentWithRent);
        }



//        for (Rent rent : rents) {
//            if (student == null) {
//                student = rent.getStudent();
//                studentWithRent = new StudentWithRent(student);
//            } else if (!student.equals(rent.getStudent())) {
//                results.add(studentWithRent);
//                student = rent.getStudent();
//                studentWithRent = new StudentWithRent(student);
//            }
//            StudentWithRent.DeviceRentHistory deviceRentHistory = new StudentWithRent.DeviceRentHistory(rent.getDevice());
//            if (rent.getReleaseDateTime() == null) studentWithRent.setStatus(false);
//            studentWithRent.getDeviceRentHistory().add(deviceRentHistory);
//            log.info("{} {} {}", rent.getDevice().isRelease(), deviceRentHistory.getModelCode(), rent.getDevice().getDeviceType());
//
//        }
//        if (studentWithRent != null) results.add(studentWithRent);
        return results;
    }
}
