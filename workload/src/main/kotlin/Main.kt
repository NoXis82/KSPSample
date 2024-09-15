internal fun main() {
//    println(SampleConfigUrl(EnvironmentSettings()).url)

}

@EnvironmentConfig
internal interface SampleConfig {
    @Url(environment = Environment.PROD, name = "https://www.prod.com")
    val prod: String

    @Url(environment = Environment.TEST, name = "https://www.test.com")
    val test: String
}

internal class EnvironmentSettings {
    val stage get() = Environment.PROD.env

    enum class Environment(val env: String) {
        PROD("prod"),
        TEST("test"),
        DEV("dev")
    }
}