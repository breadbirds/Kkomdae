package pizza.kkomdae.repository.laptopresult;


import pizza.kkomdae.entity.Device;
import pizza.kkomdae.entity.LaptopTestResult;
import pizza.kkomdae.entity.Student;

import java.util.List;

public interface CustomLapTopTestResultRepository {
    List<LaptopTestResult> findByStudentOrDevice(Student student, Device device);

    LaptopTestResult findByIdWithStudentAndDeviceAndPhotos(long testId);

    LaptopTestResult findByStudentAndStageIsLessThanAndReleaseIsFalse(Student student, Integer stageIsLessThan);
}
