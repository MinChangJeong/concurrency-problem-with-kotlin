package com.example.myapp.support

import org.junit.jupiter.api.extension.*
import java.time.Duration
import java.time.Instant

class TimingExtension : BeforeTestExecutionCallback, AfterTestExecutionCallback {
    private val startTimes = mutableMapOf<String, Instant>()

    override fun beforeTestExecution(context: ExtensionContext) {
        startTimes[key(context)] = Instant.now()
    }

    override fun afterTestExecution(context: ExtensionContext) {
        val start = startTimes.remove(key(context)) ?: return
        val duration = Duration.between(start, Instant.now()).toMillis()
        println("[TIMING] ${context.requiredTestMethod.name} took ${duration}ms")
    }

    private fun key(context: ExtensionContext): String = context.uniqueId
}
