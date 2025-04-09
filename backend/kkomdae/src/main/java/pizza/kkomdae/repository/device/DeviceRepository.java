package pizza.kkomdae.repository.device;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pizza.kkomdae.entity.Device;

import java.util.List;

@Repository
public interface DeviceRepository extends JpaRepository<Device, Long>, CustomDeviceRepository{
    Device findDeviceBySerialNum(String serialNum);
}
