package pizza.kkomdae.repository.rent;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pizza.kkomdae.entity.Device;
import pizza.kkomdae.entity.LaptopTestResult;
import pizza.kkomdae.entity.Rent;
import pizza.kkomdae.entity.Student;

import java.util.List;

@Repository
public interface RentRepository extends JpaRepository<Rent, Long>, CustomRentRepository {
    Rent findByDeviceAndStudent(Device device, Student student);
}
