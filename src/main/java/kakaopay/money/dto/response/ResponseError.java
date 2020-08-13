package kakaopay.money.dto.response;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.http.HttpStatus;

@Getter
@Setter
@NoArgsConstructor
public class ResponseError {

    private String message;

    private int status;

    ResponseError(String message, HttpStatus status) {
        this.message = message;
        this.status = status.value();
    }
}
