package pizza.kkomdae.security.etc;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Slf4j
public abstract class AbstractJwtProvider {
    protected long expiration;

    protected SecretKey key;

    protected AbstractJwtProvider(String secret, long expiration) {
        this.expiration = expiration;
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }


    public String generateToken(long userId) {
        return Jwts.builder().subject(Long.toString(userId)).expiration(new Date(System.currentTimeMillis() + expiration)).signWith(this.key).compact();
    }

    public String refreshToken(long userId) {
        return Jwts.builder().subject(Long.toString(userId)).expiration(new Date(System.currentTimeMillis() + 1000 * 60 * 60 * 24 * 7)).signWith(this.key).compact();
    }

    public String extractUserId(String token) {
        return this.getClaims(token).getSubject();
    }

    public boolean validateToken(String token) {

        try {
            if (StringUtils.hasText(token)) {
                Jwts.parser().verifyWith(this.key).build().parseSignedClaims(token);
                return true;
            }
        } catch (MalformedJwtException ex) {
            log.error("Invalid JWT token : {}", token, ex);
        } catch (ExpiredJwtException ex) {
            log.error("Expired JWT token : {}", token, ex);
            throw ex;
        } catch (UnsupportedJwtException ex) {
            log.error("Unsupported JWT token : {}", token, ex);
        } catch (IllegalArgumentException ex) {
            log.error("JWT claims string is empty. : {}", token, ex);
        } catch (Exception ex) {
            log.error("Invalid JWT token : {}", token, ex);
        }
        return false;
    }

    private Claims getClaims(String token) {
        return Jwts.parser().verifyWith(this.key).build().parseSignedClaims(token).getPayload();
    }
}
