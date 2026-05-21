package sh.whoa.narada

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class NaradaApplication

fun main(args: Array<String>) {
	runApplication<NaradaApplication>(*args)
}
