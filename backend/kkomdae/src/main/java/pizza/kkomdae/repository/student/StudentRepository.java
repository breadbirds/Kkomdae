package pizza.kkomdae.repository.student;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pizza.kkomdae.entity.Student;

@Repository
public interface StudentRepository extends JpaRepository<Student,Long>, CustomStudentRepository {
    Student findByEmail(String email);

    Student findBySsafyId(String ssafyId);
}
