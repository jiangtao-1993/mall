package com.leyou.common.advice;

import com.leyou.common.exception.ExceptionResult;
import com.leyou.common.exception.LyException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice //controller中的通知器 ，只要controller有异常，这个业务就会自动生效，相当于在适配的controller中添加try
@Slf4j
public class BasicExceptionAdvice {

    @ExceptionHandler(LyException.class) //异常处理器  catch
    public ResponseEntity<ExceptionResult> handleException(LyException e) {
        return ResponseEntity.status(e.getStatus()).body(new ExceptionResult(e));//ResponseEntity中会返回两个值，一个是状态码，另外一个消息主体
    }
}