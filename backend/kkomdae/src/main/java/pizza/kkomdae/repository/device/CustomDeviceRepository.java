package pizza.kkomdae.repository.device;

import pizza.kkomdae.dto.request.DeviceCond;
import pizza.kkomdae.entity.Device;

import java.util.List;

public interface CustomDeviceRepository {
    List<Device> getDeviceWithStatusByCond(DeviceCond deviceCond);
}
