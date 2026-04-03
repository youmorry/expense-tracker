package com.youmorry.expensetracker;

import static com.tngtech.archunit.base.DescribedPredicate.not;
import static com.tngtech.archunit.core.domain.JavaClass.Predicates.resideInAPackage;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;
import static com.tngtech.archunit.library.Architectures.layeredArchitecture;
import static com.tngtech.archunit.library.dependencies.SlicesRuleDefinition.slices;

import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;
import com.youmorry.expensetracker.shared.exception.AppException;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RestController;

@AnalyzeClasses(
    packages = "com.youmorry.expensetracker",
    importOptions = ImportOption.DoNotIncludeTests.class)
class ArchitectureTest {

  // レイヤー依存ルール
  @ArchTest
  static final ArchRule layerDependencies =
      layeredArchitecture()
          .consideringAllDependencies()
          .layer("Domain")
          .definedBy("..domain..")
          .layer("Application")
          .definedBy("..application..")
          .layer("Infrastructure")
          .definedBy("..infrastructure..")
          .layer("Presentation")
          .definedBy("..presentation..")
          .whereLayer("Domain")
          .mayOnlyBeAccessedByLayers("Application", "Infrastructure", "Presentation")
          .whereLayer("Application")
          .mayOnlyBeAccessedByLayers("Presentation", "Infrastructure")
          .whereLayer("Infrastructure")
          .mayNotBeAccessedByAnyLayer()
          .whereLayer("Presentation")
          .mayNotBeAccessedByAnyLayer();

  // shared パッケージの制約: domain は shared に依存しない
  @ArchTest
  static final ArchRule domainShouldNotDependOnShared =
      noClasses()
          .that()
          .resideInAPackage("..domain..")
          .should()
          .dependOnClassesThat()
          .resideInAPackage("..shared..");

  // shared パッケージの制約: shared は他の層に依存しない
  @ArchTest
  static final ArchRule sharedShouldNotDependOnAnyLayer =
      noClasses()
          .that()
          .resideInAPackage("..shared..")
          .should()
          .dependOnClassesThat()
          .resideInAnyPackage(
              "..domain..", "..application..", "..infrastructure..", "..presentation..");

  // ドメイン純粋性: domain は Spring Data 以外の Spring に依存しない
  @ArchTest
  static final ArchRule domainShouldNotDependOnSpring =
      noClasses()
          .that()
          .resideInAPackage("..domain..")
          .should()
          .dependOnClassesThat(
              resideInAPackage("org.springframework..")
                  .and(not(resideInAPackage("org.springframework.data.annotation..")))
                  .and(
                      not(
                          resideInAPackage(
                              "org.springframework.data.relational.core.mapping.."))));

  // コントローラーからリポジトリへの直接アクセス禁止
  @ArchTest
  static final ArchRule controllersShouldNotAccessRepositories =
      noClasses()
          .that()
          .resideInAPackage("..presentation..")
          .should()
          .dependOnClassesThat()
          .haveSimpleNameEndingWith("Repository");

  // 例外階層: shared.exception 内の例外クラスは AppException を継承すること
  @ArchTest
  static final ArchRule exceptionsShouldExtendAppException =
      classes()
          .that()
          .resideInAPackage("..shared.exception..")
          .and()
          .areAssignableTo(RuntimeException.class)
          .and()
          .areNotAssignableFrom(AppException.class)
          .should()
          .beAssignableTo(AppException.class);

  // @RestController 付きクラスは *Controller で命名し presentation パッケージに配置
  @ArchTest
  static final ArchRule restControllerNamingAndPlacement =
      classes()
          .that()
          .areAnnotatedWith(RestController.class)
          .should()
          .haveSimpleNameEndingWith("Controller")
          .andShould()
          .resideInAPackage("..presentation..");

  // @Service 付きクラスは *Service で命名し application パッケージに配置
  @ArchTest
  static final ArchRule serviceNamingAndPlacement =
      classes()
          .that()
          .areAnnotatedWith(Service.class)
          .should()
          .haveSimpleNameEndingWith("Service")
          .andShould()
          .resideInAPackage("..application..");

  // 循環依存禁止
  @ArchTest
  static final ArchRule noCyclicDependencies =
      slices().matching("com.youmorry.expensetracker.(*)..").should().beFreeOfCycles();
}
