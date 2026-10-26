package br.com.schedule.architecture;

import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;
import static org.assertj.core.api.Assertions.assertThat;

class DomainIsolationTest {

    private static final JavaClasses DOMAIN_CLASSES = new ClassFileImporter()
            .importPath(Path.of("target", "classes"));

    @Test
    void domainShouldNotDependOnFrameworkClasses() {
        noClasses()
                .that().resideInAPackage("br.com.schedule.domain..")
                .should().dependOnClassesThat().resideInAnyPackage(
                        "org.springframework..",
                        "org.bson..",
                        "com.mongodb..",
                        "jakarta..")
                .check(DOMAIN_CLASSES);
    }

    @Test
    void domainShouldNotDependOnOtherApplicationLayers() {
        noClasses()
                .that().resideInAPackage("br.com.schedule.domain..")
                .should().dependOnClassesThat().resideInAnyPackage(
                        "br.com.schedule.application..",
                        "br.com.schedule.infrastructure..",
                        "br.com.schedule.api..")
                .check(DOMAIN_CLASSES);
    }

    @Test
    void shouldImportDomainClasses() {
        assertThat(DOMAIN_CLASSES)
                .extracting(JavaClass::getName)
                .contains("br.com.schedule.domain.model.Ronda", "br.com.schedule.domain.model.AgendamentoRonda");
    }
}
