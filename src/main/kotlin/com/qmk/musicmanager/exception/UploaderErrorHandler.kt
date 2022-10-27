package com.qmk.musicmanager.exception

import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import javax.servlet.http.HttpServletRequest

@ControllerAdvice
class UploaderErrorHandler {
    @ExceptionHandler(UploaderNotFoundException::class)
    fun handleUploaderNotFoundException(
        servletRequest: HttpServletRequest,
        exception: Exception
    ): ResponseEntity<String> {
        return ResponseEntity("Uploader not found", HttpStatus.NOT_FOUND)
    }
}