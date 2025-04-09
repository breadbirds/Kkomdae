package pizza.kkomdae.repository.rent;

import pizza.kkomdae.dto.request.StudentWithRentCond;
import pizza.kkomdae.entity.Rent;
import pizza.kkomdae.entity.Student;

import java.util.List;

public interface CustomRentRepository {
    List<Rent> getRentsByStudentInfo(StudentWithRentCond studentWithRentCond);
}
