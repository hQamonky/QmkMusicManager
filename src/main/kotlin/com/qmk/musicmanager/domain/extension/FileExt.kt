package com.qmk.musicmanager.domain.extension

import java.io.File

fun File.moveTo(target: File, overwrite: Boolean = false) {
    this.let { sourceFile ->
        sourceFile.copyTo(target = target, overwrite = overwrite)
        sourceFile.delete()
    }
}

fun File.moveTo(targetPath: String, overwrite: Boolean = false) {
    this.let { sourceFile ->
        sourceFile.copyTo(target = File(targetPath), overwrite = overwrite)
        sourceFile.delete()
    }
}