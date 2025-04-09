package pizza.kkomdae.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import pizza.kkomdae.entity.Admin;
import pizza.kkomdae.repository.AdminRepository;

@Service
@RequiredArgsConstructor
public class AdminService {

    private final AdminRepository adminRepository;
    public Admin getByCode(String adminCode) {
        return adminRepository.getByCode(adminCode);
    }
}
