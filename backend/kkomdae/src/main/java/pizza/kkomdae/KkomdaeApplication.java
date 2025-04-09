package pizza.kkomdae;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.servers.Server;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@OpenAPIDefinition(servers = {@Server(url = "https://j12d101.p.ssafy.io",description = "https"), @Server(url = "http://localhost:8080",description = "로컬")})
@SpringBootApplication
@EnableAsync
public class KkomdaeApplication {

    public static void main(String[] args) {
        SpringApplication.run(KkomdaeApplication.class, args);
    }

}
