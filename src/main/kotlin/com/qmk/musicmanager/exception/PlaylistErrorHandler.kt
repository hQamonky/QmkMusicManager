package com.qmk.musicmanager.exception

import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import javax.servlet.http.HttpServletRequest

@ControllerAdvice
class PlaylistErrorHandler {
    @ExceptionHandler(PlaylistNotFoundException::class)
    fun handlePlaylistNotFoundException(
        servletRequest: HttpServletRequest,
        exception: Exception
    ): ResponseEntity<String> {
        return ResponseEntity("Playlist not found", HttpStatus.NOT_FOUND)
    }
}