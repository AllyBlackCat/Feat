
package io.github.allyblackcat.feat.common.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Void> handleValidationException(
            MethodArgumentNotValidException ex) {

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .build();
    }
}
