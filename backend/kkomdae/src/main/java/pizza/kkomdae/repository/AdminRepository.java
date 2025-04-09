package pizza.kkomdae.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pizza.kkomdae.entity.Admin;

@Repository
public interface AdminRepository extends JpaRepository<Admin,Long> {
    Admin getByCode(String code);
}
