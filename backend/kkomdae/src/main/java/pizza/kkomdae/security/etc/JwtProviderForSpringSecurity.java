package pizza.kkomdae.security.etc;

import org.springframework.stereotype.Component;
import pizza.kkomdae.security.dto.JwtPropertiesForSpringSecurity;

@Component
public class JwtProviderForSpringSecurity extends AbstractJwtProvider {
    public JwtProviderForSpringSecurity(JwtPropertiesForSpringSecurity jwtProperties) {
        super(jwtProperties.getSecret(), jwtProperties.getExpiration());
    }
}
