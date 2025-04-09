package pizza.kkomdae.repository.device;

import com.querydsl.core.types.Predicate;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import pizza.kkomdae.dto.request.DeviceCond;
import pizza.kkomdae.entity.Device;
import pizza.kkomdae.entity.QDevice;

import java.util.List;

public class CustomDeviceRepositoryImpl implements CustomDeviceRepository {
    private final JPAQueryFactory query;

    public CustomDeviceRepositoryImpl(EntityManager em) {
        this.query = new JPAQueryFactory(em);
    }

    @Override
    public List<Device> getDeviceWithStatusByCond(DeviceCond deviceCond) {
        QDevice device = QDevice.device;
        return query.selectFrom(device)
                .where(isCond(deviceCond.getSearchType(), deviceCond.getSearchKeyword()),isType(deviceCond.getDeviceType()))
                .orderBy(device.deviceId.asc())
                .fetch();
    }

    private Predicate isType(String deviceType) {
        if(deviceType!=null&&!deviceType.isBlank()) return QDevice.device.deviceType.eq(deviceType);
        return null;
    }

    private Predicate isCond(String searchType, String keyword) {
        if (searchType != null && !searchType.isBlank()
                && keyword != null && !keyword.isBlank()
        ) {
            switch (searchType) {
                case "시리얼 번호" -> {
                    return QDevice.device.serialNum.like("%"+keyword+"%");
                }
                case "모델코드" -> {
                    return QDevice.device.modelCode.like("%"+keyword+"%");
                }
            }
        }
        return null;
    }


}
