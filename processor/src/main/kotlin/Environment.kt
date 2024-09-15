/**
 * Определим аннотацию с целевым уровнем Property для этапов разработки и URL:
 */

@Target(AnnotationTarget.PROPERTY)
annotation class Url(
    val environment: Environment,
    val name: String
)

enum class Environment(val env: String) {
    PROD("prod"),
    TEST("test"),
    DEV("dev")
}