package org.cookcounty.tax;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.domain.JavaModifier;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.lang.ArchCondition;
import com.tngtech.archunit.lang.ConditionEvents;
import com.tngtech.archunit.lang.SimpleConditionEvent;

import jakarta.persistence.Entity;
import jakarta.persistence.Version;

import org.jspecify.annotations.NullMarked;
import org.junit.jupiter.api.Test;

class ArchitectureTest {
    private static final JavaClasses PRODUCTION_CLASSES =
            new ClassFileImporter()
                    .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
                    .importPackages("org.cookcounty.tax");

    @Test
    void domainDoesNotDependOnFrameworkOrInfrastructure() {
        noClasses()
                .that()
                .resideInAPackage("..domain..")
                .should()
                .dependOnClassesThat()
                .resideInAnyPackage(
                        "..infrastructure..", "org.springframework..", "jakarta.persistence..")
                .check(PRODUCTION_CLASSES);
    }

    @Test
    void inboundAdaptersDoNotDependOnPersistence() {
        noClasses()
                .that()
                .resideInAPackage("..infrastructure.adapter.in..")
                .should()
                .dependOnClassesThat()
                .resideInAPackage("..infrastructure.adapter.out.persistence..")
                .check(PRODUCTION_CLASSES);
    }

    @Test
    void domainAndResponseValuesUseImmutableRecordContracts() {
        classes()
                .that()
                .resideInAnyPackage("..domain.model..", "..domain.contract.dto..")
                .should(
                        new ArchCondition<JavaClass>(
                                "be immutable values or stateless invariant helpers") {
                            @Override
                            public void check(JavaClass type, ConditionEvents events) {
                                if (!type.isRecord()
                                        && !type.isEnum()
                                        && !type.isInterface()
                                        && !isStatelessInvariantHelper(type)
                                        && !type.getSimpleName().equals("package-info")) {
                                    events.add(
                                            SimpleConditionEvent.violated(
                                                    type,
                                                    type.getName()
                                                            + " must be an immutable value or a"
                                                            + " stateless invariant helper"));
                                }
                            }
                        })
                .check(PRODUCTION_CLASSES);
    }

    private static boolean isStatelessInvariantHelper(JavaClass type) {
        return type.getModifiers().contains(JavaModifier.FINAL)
                && type.getConstructors().stream()
                        .allMatch(
                                constructor ->
                                        constructor.getModifiers().contains(JavaModifier.PRIVATE))
                && type.getFields().stream()
                        .allMatch(
                                field ->
                                        field.getModifiers().contains(JavaModifier.STATIC)
                                                && field.getModifiers()
                                                        .contains(JavaModifier.FINAL))
                && type.getMethods().stream()
                        .allMatch(method -> method.getModifiers().contains(JavaModifier.STATIC));
    }

    @Test
    void persistenceEntitiesAreVersionedAndAreNotRecords() {
        classes()
                .that()
                .areAnnotatedWith(Entity.class)
                .should(
                        new ArchCondition<JavaClass>("be versioned mutable persistence classes") {
                            @Override
                            public void check(JavaClass type, ConditionEvents events) {
                                boolean versioned =
                                        type.getAllFields().stream()
                                                .anyMatch(
                                                        field ->
                                                                field.isAnnotatedWith(
                                                                        Version.class));
                                if (type.isRecord() || !versioned) {
                                    events.add(
                                            SimpleConditionEvent.violated(
                                                    type,
                                                    type.getName()
                                                            + " must be a non-record entity with"
                                                            + " @Version"));
                                }
                            }
                        })
                .check(PRODUCTION_CLASSES);
    }

    @Test
    void everyProductionPackageDeclaresItsNullabilityDefault() {
        classes()
                .should(
                        new ArchCondition<JavaClass>("belong to a @NullMarked package") {
                            @Override
                            public void check(JavaClass type, ConditionEvents events) {
                                Package packageInfo = type.reflect().getPackage();
                                if (packageInfo == null
                                        || !packageInfo.isAnnotationPresent(NullMarked.class)) {
                                    events.add(
                                            SimpleConditionEvent.violated(
                                                    type,
                                                    type.getPackageName()
                                                            + " has no @NullMarked package"
                                                            + " declaration"));
                                }
                            }
                        })
                .check(PRODUCTION_CLASSES);
    }
}
