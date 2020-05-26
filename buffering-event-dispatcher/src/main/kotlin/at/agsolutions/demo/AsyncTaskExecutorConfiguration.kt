package at.agsolutions.demo

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.task.AsyncTaskExecutor
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor
import org.springframework.web.context.annotation.ApplicationScope

@Configuration
class AsyncTaskExecutorConfiguration {

    @ApplicationScope
    @Bean(name = ["uiTaskExecutor"])
    fun uiTaskExecutor(): AsyncTaskExecutor {
        val executor = ThreadPoolTaskExecutor()
        executor.corePoolSize = 10
        executor.maxPoolSize = 40
        executor.setWaitForTasksToCompleteOnShutdown(false)
        executor.setThreadNamePrefix("UI-ASYNC")
        executor.initialize()
        return executor
    }
}
