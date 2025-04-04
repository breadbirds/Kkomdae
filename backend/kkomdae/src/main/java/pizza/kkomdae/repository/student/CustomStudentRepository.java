package pizza.kkomdae.repository.student;

import pizza.kkomdae.dto.request.StudentWithRentCond;
import pizza.kkomdae.entity.Student;

import java.util.List;

public interface CustomStudentRepository {
    List<Student> findByKeywordWithStatus(String searchType, String searchKeyword);

    List<Student> getStudentsByStudentInfo(StudentWithRentCond student);
}