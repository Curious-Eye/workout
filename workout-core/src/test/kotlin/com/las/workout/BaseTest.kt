package com.las.workout

import com.las.workout.test.DataHelper
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.TestInstance
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.util.TestPropertyValues
import org.springframework.context.ApplicationContextInitializer
import org.springframework.context.ConfigurableApplicationContext
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.junit4.SpringRunner
import org.springframework.test.web.reactive.server.WebTestClient
import org.testcontainers.containers.MongoDBContainer
import org.testcontainers.utility.DockerImageName

const val MONGO_TEST_DB_NAME = "workout-test"

@ActiveProfiles("test")
@RunWith(SpringRunner::class)
@SpringBootTest(
    classes = [LasWorkoutApplication::class],
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT
)
@ContextConfiguration(classes = [DataHelper::class], initializers = [ApplicationInitializer::class])
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class BaseTest {

    @Autowired
    lateinit var dataHelper: DataHelper

    @Autowired
    lateinit var webTestClient: WebTestClient

    @BeforeEach
    fun beforeEach() {
        dataHelper.clearData()
    }

}

internal class ApplicationInitializer : ApplicationContextInitializer<ConfigurableApplicationContext> {

    private val mongo = SharedMongoContainer.instance

    override fun initialize(applicationContext: ConfigurableApplicationContext) {
        TestPropertyValues.of(
            "server.shutdown=immediate",
            "spring.data.mongodb.uri=${mongo.replicaSetUrl.replace("test", MONGO_TEST_DB_NAME)}",
            "spring.data.mongodb.database=$MONGO_TEST_DB_NAME",
        ).applyTo(applicationContext)
    }
}


private const val MONGO_IMAGE_AND_TAG = "mongo:4.4"

object SharedMongoContainer {
    val instance by lazy {
        val mongoDBContainer = MongoDBContainer(DockerImageName.parse(MONGO_IMAGE_AND_TAG))
        mongoDBContainer.start()
        mongoDBContainer
    }
}
