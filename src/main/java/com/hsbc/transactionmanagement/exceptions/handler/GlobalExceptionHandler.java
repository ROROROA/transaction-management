package com.hsbc.transactionmanagement.exceptions.handler;

import com.hsbc.transactionmanagement.exceptions.DuplicateTransactionException;
import com.hsbc.transactionmanagement.exceptions.TransactionNotFoundException;
import com.hsbc.transactionmanagement.exceptions.TransactionValidationException;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@RestControllerAdvice
@ApiResponses({
        @ApiResponse(responseCode = "400", description = "Invalid request data"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Forbidden"),
        @ApiResponse(responseCode = "404", description = "Resource not found"),
        @ApiResponse(responseCode = "409", description = "Conflict"),
        @ApiResponse(responseCode = "422", description = "Validation error"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
})
public class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail handleValidationException(MethodArgumentNotValidException ex) {
        logger.warn("Validation error occurred: {}", ex.getMessage());
        
        ProblemDetail problemDetail = ProblemDetail.forStatus(HttpStatus.BAD_REQUEST);
        problemDetail.setTitle("Validation error");

        Map<String, String> errors = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .collect(Collectors.toMap(
                        FieldError::getField,
                        fieldError -> Optional.ofNullable(fieldError.getDefaultMessage())
                                .orElse("Invalid value")));

        logger.debug("Validation errors detail: {}", errors);
        problemDetail.setProperty("errors", errors);
        return problemDetail;
    }

    @ExceptionHandler(DuplicateTransactionException.class)
    public ProblemDetail handleDuplicateTransaction(DuplicateTransactionException ex) {
        logger.warn("Duplicate transaction detected: {}", ex.getMessage());
        
        ProblemDetail pd = ProblemDetail.forStatus(HttpStatus.CONFLICT);
        pd.setTitle("Duplicate Transaction");
        pd.setDetail(ex.getMessage());
        pd.setProperty("transactionId", ex.getTransactionId());
        pd.setProperty("errorCode", "TRANSACTION_DUPLICATE");
        
        logger.debug("Duplicate transaction details - ID: {}", ex.getTransactionId());
        return pd;
    }

    @ExceptionHandler(TransactionNotFoundException.class)
    public ProblemDetail handleTransactionNotFound(TransactionNotFoundException ex) {
        logger.warn("Transaction not found: {}", ex.getMessage());
        
        ProblemDetail pd = ProblemDetail.forStatus(HttpStatus.NOT_FOUND);
        pd.setTitle("Transaction Not Found");
        pd.setDetail(ex.getMessage());
        pd.setProperty("transactionId", ex.getTransactionId());
        pd.setProperty("errorCode", "TRANSACTION_NOT_FOUND");
        
        logger.debug("Transaction not found details - ID: {}", ex.getTransactionId());
        return pd;
    }

    @ExceptionHandler(TransactionValidationException.class)
    public ProblemDetail handleTransactionValidation(TransactionValidationException ex) {
        logger.warn("Transaction validation failed: {}, error code: {}", ex.getMessage(), ex.getErrorCode());
        
        ProblemDetail pd = ProblemDetail.forStatus(HttpStatus.UNPROCESSABLE_ENTITY);
        pd.setTitle("Transaction Validation Failed");
        pd.setDetail(ex.getMessage());
        pd.setProperty("errorCode", ex.getErrorCode());
        
        return pd;
    }
    
    @ExceptionHandler(Exception.class)
    public ProblemDetail handleGenericException(Exception ex) {
        logger.error("Unhandled exception occurred", ex);
        
        ProblemDetail pd = ProblemDetail.forStatus(HttpStatus.INTERNAL_SERVER_ERROR);
        pd.setTitle("Internal Server Error");
        pd.setDetail("An unexpected error occurred. Please contact support if the problem persists.");
        pd.setProperty("errorCode", "INTERNAL_ERROR");
        
        return pd;
    }
}
