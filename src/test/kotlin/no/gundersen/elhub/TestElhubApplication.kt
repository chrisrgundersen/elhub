package no.gundersen.elhub

import org.springframework.boot.fromApplication
import org.springframework.boot.with


fun main(args: Array<String>) {
	fromApplication<ElhubApplication>().with(TestcontainersConfiguration::class).run(*args)
}
