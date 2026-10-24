package no.gundersen.elhub

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class ElhubApplication

fun main(args: Array<String>) {
	runApplication<ElhubApplication>(*args)
}
