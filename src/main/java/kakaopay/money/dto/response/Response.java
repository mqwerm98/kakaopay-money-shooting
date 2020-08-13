package kakaopay.money.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.validation.BindingResult;
import org.springframework.validation.Errors;

@Getter
@AllArgsConstructor
public class Response<T> {

    private final boolean success;

    private final T response;

    private final ResponseError error;

    public static <T> Response<T> OK() {
        return new Response<>(true, null, null);
    }

    public static <T> Response<T> OK(T response) {
        return new Response<>(true, response, null);
    }

    public static Response ERROR(String errorMessage, HttpStatus status) {
        return new Response<>(false, null, new ResponseError(errorMessage, status));
    }

    public static Response BAD_REQUEST_ERROR() {
        return new Response<>(false, null, new ResponseError(HttpStatus.BAD_REQUEST.getReasonPhrase(), HttpStatus.BAD_REQUEST));
    }

    public static Response BAD_REQUEST_ERROR(String errorMessage) {
        return new Response<>(false, null, new ResponseError(errorMessage, HttpStatus.BAD_REQUEST));
    }

    public static Response BAD_REQUEST_ERROR(Errors result) {
        String errorMessage = String.format("[%s] %s", result.getFieldError().getField(), result.getAllErrors().get(0).getDefaultMessage());
        return BAD_REQUEST_ERROR(errorMessage);
    }

}
