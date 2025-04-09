package pizza.kkomdae.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import pizza.kkomdae.dto.request.DeviceCond;
import pizza.kkomdae.dto.respond.DeviceWithStatus;
import pizza.kkomdae.entity.Device;
import pizza.kkomdae.repository.device.DeviceRepository;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DeviceService {

    private final DeviceRepository deviceRepository;



    public List<DeviceWithStatus> getDevicesWithCond(DeviceCond deviceCond) {
        List<Device> deviceWithStatusByCond = deviceRepository.getDeviceWithStatusByCond(deviceCond);
        List<DeviceWithStatus> results = new ArrayList<>();
        for (Device device : deviceWithStatusByCond) {
            results.add(new DeviceWithStatus(device));
        }
        return results;
    }
}
