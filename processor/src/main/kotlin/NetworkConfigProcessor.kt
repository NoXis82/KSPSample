import com.google.devtools.ksp.processing.*
import com.google.devtools.ksp.symbol.*
import java.io.OutputStream
import kotlin.properties.Delegates

/**
 * реализация интерфейса SymbolProcessor.
 * @param codeGenerator - отвечает за создание и управление файлами
 * @param KSPLogger - предназначена для логирования предупреждений и ошибок KSP
 */

class NetworkConfigProcessor(
    val codeGenerator: CodeGenerator,
    val logger: KSPLogger
) : SymbolProcessor {
    override fun process(resolver: Resolver): List<KSAnnotated> {
        //Для получения всех символов под аннотацией
        val symbols = resolver.getSymbolsWithAnnotation(checkNotNull(EnvironmentConfig::class.qualifiedName))

        //отфильтруем только интерфейсы, поскольку с ними будет работать процессор
        symbols.filter {
            val classDeclaration = it as? KSClassDeclaration
            if (classDeclaration?.classKind != ClassKind.INTERFACE) {
                logger.error("Use kotlin interface with @EnvironmentConfig!", classDeclaration)
            }
            classDeclaration?.classKind == ClassKind.INTERFACE
        }.forEach {
            //для каждого отфильтрованного экземпляра вызываем метод accept
            it.accept(NetworkConfigVisitor(), Unit)
        }

        return emptyList()
    }

    /**
     * Существует несколько предопределенных Visitor-классов
     * KSVisitorVoid - он прост и не содержит никакой логики
     */
    inner class NetworkConfigVisitor : KSVisitorVoid() {

        private var file by Delegates.notNull<OutputStream>()

        //является точкой входа в Visitor-класс
        override fun visitClassDeclaration(classDeclaration: KSClassDeclaration, data: Unit) {
            val properties = classDeclaration.getAllProperties()
            //Пакет файла совпадает с аннотированным интерфейсом
            val packageName = classDeclaration.containingFile!!.packageName.asString()
            //имя файла представляет собой конкатенацию имени интерфейса и строки Url
            val className = "${classDeclaration.simpleName.asString()}Url"
            file = codeGenerator.createNewFile(
                Dependencies(
                    true,
                    classDeclaration.containingFile!!
                ),
                packageName,
                className
            )

            file.appendText("package $packageName\n\n")
            file.appendText("import com.strukov.processor.Environment\n\n")
            file.appendText("public class $className(private val environmentSettings: EnvironmentSettings) {\n")
            file.appendText("\tinternal val url get() = environments[environmentSettings.stage].orEmpty()\n")
            file.appendText("\tprivate val environments = mapOf<String, String>(")

            val iterator = properties.iterator()

            while (iterator.hasNext()) {
                visitPropertyDeclaration(iterator.next(), Unit)
                if (iterator.hasNext()) file.appendText(",")
            }

            file.appendText("\n\t)\n")

            file.appendText("}")
            file.close()
        }

        //служит для свойств
        override fun visitPropertyDeclaration(property: KSPropertyDeclaration, data: Unit) {
            //найдем Url аннотации и вызовем метод accept
            property.annotations.find { it.shortName.asString() == Url::class.java.simpleName }
                ?.accept(this, Unit)
            //в случае его отсутствия, выведем ошибку
                ?: logger.error("Use @Url for property!", property)
        }

        //предназначен для обработки аннотаций и записи в файл Pair для интерфейса Map
        override fun visitAnnotation(annotation: KSAnnotation, data: Unit) {
            //Найдем оба необходимых аргумента environment и name и запишем их значения в файл
            val environment = annotation.arguments.find { it.name?.asString() == "environment" }
                ?.value.toString().substringAfter("processor.")

            val name = annotation.arguments.find { it.name?.asString() == "name" }

            file.appendText("\n\t\t${environment}.env to \"${name!!.value as String}\"")

        }

        private fun OutputStream.appendText(str: String) {
            this.write(str.toByteArray())
        }
    }


    /**
     * Теперь необходимо сообщить KSP о нашем процессоре.
     * Для этого нужно создать новый класс Provider, который реализует SymbolProcessorProvider.
     * зарегистрируем его!!!
     */
    internal class NetworkConfigProcessorProvider : SymbolProcessorProvider {
        override fun create(environment: SymbolProcessorEnvironment): SymbolProcessor {
            return NetworkConfigProcessor(environment.codeGenerator, environment.logger)
        }

    }
}