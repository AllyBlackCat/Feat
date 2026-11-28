
package io.github.allyblackcat.feat.common.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import io.github.allyblackcat.feat.auth.AuthenticationFailedException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(
            MethodArgumentNotValidException ex) {
        
        ErrorResponse error = new ErrorResponse(
            "ERROR-01002",
            "入力内容に不備があります。"
        );

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(error);
    }

    @ExceptionHandler(AuthenticationFailedException.class)
    public ResponseEntity<ErrorResponse> handleAuthenticationFailed(
            AuthenticationFailedException ex) {

        ErrorResponse error = new ErrorResponse(
            "ERROR-01001",
            "認証に失敗しました。"
        );

        return ResponseEntity
            .status(HttpStatus.UNAUTHORIZED)
            .body(error);
    }

}
