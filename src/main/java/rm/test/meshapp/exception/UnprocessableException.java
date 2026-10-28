package rm.test.meshapp.exception;

import org.springframework.http.HttpStatus;

public class UnprocessableException extends ApiException {

    public UnprocessableException(String message) {
        super(HttpStatus.UNPROCESSABLE_CONTENT, message);
    }
}
