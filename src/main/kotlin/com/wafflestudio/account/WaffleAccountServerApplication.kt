package com.wafflestudio.account

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class WaffleAccountServerApplication

fun main(args: Array<String>) {
    runApplication<WaffleAccountServerApplication>(*args)
}
