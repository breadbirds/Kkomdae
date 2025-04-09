package pizza.kkomdae.security.etc;

import jakarta.annotation.Resource;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import pizza.kkomdae.entity.Student;
import pizza.kkomdae.repository.student.StudentRepository;
import pizza.kkomdae.security.dto.CustomUserDetails;


@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {


    private final StudentRepository studentRepository;
    @Lazy
    @Resource
    CustomUserDetailsService self;

    @Override
    public UserDetails loadUserByUsername(String userId) throws UsernameNotFoundException {
        Student student = this.studentRepository.findById(Long.parseLong(userId)).orElseThrow(() -> new UsernameNotFoundException("없는 userId"));
        return new CustomUserDetails(student.getStudentId());
    }


}
