package com.qmk.musicmanager

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class MusicmanagerApplication

fun main(args: Array<String>) {
	runApplication<MusicmanagerApplication>(*args)
}
