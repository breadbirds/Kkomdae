package pizza.kkomdae.repository.laptopresult;

import com.querydsl.core.types.Predicate;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import pizza.kkomdae.entity.*;

import java.util.List;

public class CustomLapTopTestResultRepositoryImpl implements CustomLapTopTestResultRepository {

    private final JPAQueryFactory query;

    public CustomLapTopTestResultRepositoryImpl(EntityManager em) {
        this.query = new JPAQueryFactory(em);
    }

    @Override
    public List<LaptopTestResult> findByStudentOrDevice(Student student, Device device) {
        return query.selectFrom(QLaptopTestResult.laptopTestResult)
                .leftJoin(QLaptopTestResult.laptopTestResult.device, QDevice.device).fetchJoin()
                .leftJoin(QLaptopTestResult.laptopTestResult.student, QStudent.student).fetchJoin()
                .leftJoin(QRent.rent).on(QRent.rent.device.eq(QDevice.device).and(QRent.rent.student.eq(QStudent.student)))
                .where(isCond(student, device))
                .orderBy(QLaptopTestResult.laptopTestResult.date.desc())
                .fetch()
                ;
    }

    @Override
    public LaptopTestResult findByIdWithStudentAndDeviceAndPhotos(long testId) {
        QLaptopTestResult result = QLaptopTestResult.laptopTestResult;
        QStudent student = QStudent.student;
        QDevice device = QDevice.device;
        QPhoto photo = QPhoto.photo;
        return query.selectFrom(result)
                .leftJoin(result.student, student).fetchJoin()
                .leftJoin(result.device, device).fetchJoin()
                .leftJoin(result.photos,photo).fetchJoin()
                .where(result.laptopTestResultId.eq(testId))
                .orderBy(photo.type.asc())
                .fetchOne();
    }

    @Override
    public LaptopTestResult findByStudentAndStageIsLessThanAndReleaseIsFalse(Student student, Integer stageIsLessThan) {
        QLaptopTestResult laptopTestResult = QLaptopTestResult.laptopTestResult;
        QStudent student1 = QStudent.student;

        return query.selectFrom(laptopTestResult)
                .where(laptopTestResult.stage.lt(stageIsLessThan).and(laptopTestResult.release.eq(false).and(laptopTestResult.student.eq(student1))))
                .fetchOne();
    }

    private Predicate isCond(Student student, Device device) {
        if (student != null && device != null) {
            return QStudent.student.eq(student).and(QDevice.device.eq(device));
        } else if (student != null) {
            return QStudent.student.eq(student);
        } else if (device != null) {
            return QDevice.device.eq(device);
        }
        return null;
    }
}
