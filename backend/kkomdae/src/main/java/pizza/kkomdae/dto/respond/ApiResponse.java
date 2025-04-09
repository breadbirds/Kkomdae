package pizza.kkomdae.dto.respond;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class ApiResponse {
    private boolean success;
    private HttpStatus status = HttpStatus.OK;
    private String message;
    private Object data;

    public ApiResponse(boolean success, String message) {
        this.success = success;
        this.message = message;
    }

    public ApiResponse(boolean success, HttpStatus httpStatus, String message) {
        this.success = success;
        this.message = message;
        this.status = httpStatus;
    }

    public ApiResponse(boolean success, String message, Object data) {
        this.success = success;
        this.message = message;
        this.data = data;
    }

    public ApiResponse(boolean success, HttpStatus status, String message, Object data) {
        this.success = success;
        this.status = status;
        this.message = message;
        this.data = data;
    }
}
