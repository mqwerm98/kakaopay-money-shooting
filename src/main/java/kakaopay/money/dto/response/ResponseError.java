package kakaopay.money.dto.response;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class ResponseError {

    private final String message;

    private final int status;

    ResponseError(String message, HttpStatus status) {
        this.message = message;
        this.status = status.value();
    }
}
