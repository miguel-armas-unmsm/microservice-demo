package com.demo.bbq.order.util.exception;

import com.demo.bbq.order.util.constant.MessageConstant;
import com.demo.bbq.order.util.exception.impl.model.ApiException;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@AllArgsConstructor
@Getter
public enum ExceptionCatalog {

  ERROR0001(MessageConstant.NO_RECORDS_FOUND, HttpStatus.NOT_FOUND);

  private final String description;
  private final HttpStatus httpStatus;

  public ApiException buildException(Throwable cause) {
    return (cause instanceof ApiException)
        ? (ApiException) cause
        : ApiException.builder(this.name(), this.getDescription(), this.getHttpStatus())
        .cause(cause)
        .build();
  }

  public ApiException buildException() {
    return ApiException.builder(this.name(), this.getDescription(), this.getHttpStatus())
        .build();
  }

}
