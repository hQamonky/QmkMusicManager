package com.qmk.musicmanager.exception

import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import javax.servlet.http.HttpServletRequest

@ControllerAdvice
class ChannelErrorHandler {
    @ExceptionHandler(ChannelNotFoundException::class)
    fun handleChannelNotFoundException(
        servletRequest: HttpServletRequest,
        exception: Exception
    ): ResponseEntity<String> {
        return ResponseEntity("Channel not found", HttpStatus.NOT_FOUND)
    }
}