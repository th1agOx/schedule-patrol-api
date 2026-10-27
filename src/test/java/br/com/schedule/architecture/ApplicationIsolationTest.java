package br.com.schedule.architecture;

import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;
import static org.assertj.core.api.Assertions.assertThat;

class ApplicationIsolationTest {

    private static final JavaClasses APPLICATION_CLASSES = new ClassFileImporter()
            .importPath(Path.of("target", "classes"));

    @Test
    void applicationShouldNotDependOnInfrastructureOrApi() {
        noClasses()
                .that().resideInAPackage("br.com.schedule.application..")
                .should().dependOnClassesThat().resideInAnyPackage(
                        "br.com.schedule.infrastructure..",
                        "br.com.schedule.api..")
                .check(APPLICATION_CLASSES);
    }

    @Test
    void applicationShouldNotDependOnFrameworkClasses() {
        noClasses()
                .that().resideInAPackage("br.com.schedule.application..")
                .should().dependOnClassesThat().resideInAnyPackage(
                        "org.springframework..",
                        "org.bson..",
                        "com.mongodb..")
                .check(APPLICATION_CLASSES);
    }

    @Test
    void shouldImportApplicationClasses() {
        assertThat(APPLICATION_CLASSES)
                .extracting(JavaClass::getName)
                .contains("br.com.schedule.application.usecase.ronda.CriarRondaUseCase",
                        "br.com.schedule.application.usecase.agendamento.AgendarRondaUseCase");
    }
}
