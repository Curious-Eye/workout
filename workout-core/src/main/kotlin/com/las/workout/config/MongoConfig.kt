package com.las.workout.config

import org.springframework.boot.autoconfigure.domain.EntityScan
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.mongodb.ReactiveMongoDatabaseFactory
import org.springframework.data.mongodb.ReactiveMongoTransactionManager
import org.springframework.data.mongodb.config.EnableReactiveMongoAuditing
import org.springframework.data.mongodb.repository.config.EnableReactiveMongoRepositories
import org.springframework.transaction.ReactiveTransactionManager
import org.springframework.transaction.annotation.EnableTransactionManagement

@Configuration
@EntityScan("com.las.workout")
@EnableReactiveMongoAuditing
@EnableReactiveMongoRepositories(basePackages = ["com.las.workout"])
@EnableTransactionManagement
class MongoConfig {

    @Bean
    fun transactionManager(factory: ReactiveMongoDatabaseFactory): ReactiveTransactionManager {
        return ReactiveMongoTransactionManager(factory)
    }

}