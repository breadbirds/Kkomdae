package pizza.kkomdae.repository.laptopresult;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pizza.kkomdae.entity.Device;
import pizza.kkomdae.entity.LaptopTestResult;
import pizza.kkomdae.entity.Student;

import java.util.List;
import java.util.Optional;

@Repository
public interface LapTopTestResultRepository extends JpaRepository<LaptopTestResult,Long>, CustomLapTopTestResultRepository {

    LaptopTestResult findByStudentAndStageIsLessThan(Student student, int stageIsLessThan);

    LaptopTestResult findByStudentAndLaptopTestResultId(Student student, long laptopTestResultId);

    LaptopTestResult findByStudentAndStageIsLessThanAndDeviceIsNull(Student student, int stepIsLessThan);

    Optional<LaptopTestResult> findByRandomKey(String randomKey);

}
