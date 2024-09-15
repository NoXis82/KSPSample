internal fun main() {
    println(SampleConfigUrl(EnvironmentSettings()).url)
}

//конфигурационный файл с помощью аннотаций
@EnvironmentConfig
internal interface SampleConfig {
    @Url(environment = Environment.PROD, name = "https://www.prod.com")
    val prod: String

    @Url(environment = Environment.TEST, name = "https://www.test.com")
    val test: String
}

/**
 * Создадим класс-заглушку, который возвращает текущую среду.
 */
class EnvironmentSettings {
    val stage get() = Environment.PROD.env

    enum class Environment(val env: String) {
        PROD("prod"),
        TEST("test"),
        DEV("dev")
    }
}