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

/**
 * フィーチャーファースト（クリーンアーキテクチャ）構成のアーキテクチャルール。
 *
 * <p>各フィーチャー（transaction, user, category, analytics, auth）はトップレベルパッケージとして配置され、 内部に domain /
 * application / infrastructure / presentation のサブパッケージを持つ。
 */
@AnalyzeClasses(
    packages = "com.youmorry.expensetracker",
    importOptions = ImportOption.DoNotIncludeTests.class)
class ArchitectureTest {

  private static final String ROOT = "com.youmorry.expensetracker.";

  // --- フィーチャー内レイヤー依存ルール ---

  // domain <- application, infrastructure, presentation（各フィーチャー横断で適用）
  // application <- presentation, infrastructure
  // infrastructure, presentation は他のレイヤーからアクセスされない
  @ArchTest
  static final ArchRule featureInternalLayerDependencies =
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

  // --- クロスフィーチャー制約 ---

  // 各フィーチャーの application 層は他フィーチャーの application に依存しない
  @ArchTest
  static final ArchRule crossFeatureApplicationIsolation =
      slices()
          .matching(ROOT + "(*).application..")
          .should()
          .notDependOnEachOther()
          .as("フィーチャー間の application 層は互いに依存しない");

  // 各フィーチャーの infrastructure 層は他フィーチャーの infrastructure に依存しない
  @ArchTest
  static final ArchRule crossFeatureInfrastructureIsolation =
      slices()
          .matching(ROOT + "(*).infrastructure..")
          .should()
          .notDependOnEachOther()
          .as("フィーチャー間の infrastructure 層は互いに依存しない");

  // 各フィーチャーの presentation 層は他フィーチャーの presentation に依存しない
  // ただし auth.presentation -> user.presentation（AuthResponse -> UserResponse）は許容
  @ArchTest
  static final ArchRule crossFeaturePresentationIsolation =
      slices()
          .matching(ROOT + "(*).presentation..")
          .should()
          .notDependOnEachOther()
          .ignoreDependency(
              resideInAPackage("..auth.presentation.."), resideInAPackage("..user.presentation.."))
          .as("フィーチャー間の presentation 層は互いに依存しない（auth -> user は許容）");

  // --- shared パッケージの制約 ---

  // domain は shared.exception に依存しない
  @ArchTest
  static final ArchRule domainShouldNotDependOnShared =
      noClasses()
          .that()
          .resideInAPackage("..domain..")
          .should()
          .dependOnClassesThat()
          .resideInAPackage("..shared..");

  // shared.exception は他のパッケージに依存しない
  @ArchTest
  static final ArchRule sharedExceptionShouldBeIndependent =
      noClasses()
          .that()
          .resideInAPackage("..shared.exception..")
          .should()
          .dependOnClassesThat()
          .resideInAnyPackage(
              "..transaction..", "..user..", "..category..", "..analytics..", "..auth..");

  // --- ドメイン純粋性 ---

  // domain は Spring Data 以外の Spring に依存しない
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
                      not(resideInAPackage("org.springframework.data.relational.core.mapping.."))));

  // --- コントローラーからリポジトリへの直接アクセス禁止 ---

  @ArchTest
  static final ArchRule controllersShouldNotAccessRepositories =
      noClasses()
          .that()
          .resideInAPackage("..presentation..")
          .should()
          .dependOnClassesThat()
          .haveSimpleNameEndingWith("Repository");

  // --- 例外階層 ---

  // shared.exception 内の例外クラスは AppException を継承すること
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

  // --- 命名・配置規約 ---

  // @RestController は *Controller で命名し presentation パッケージに配置
  @ArchTest
  static final ArchRule restControllerNamingAndPlacement =
      classes()
          .that()
          .areAnnotatedWith(RestController.class)
          .should()
          .haveSimpleNameEndingWith("Controller")
          .andShould()
          .resideInAPackage("..presentation..");

  // @Service は *Service で命名し application パッケージに配置
  @ArchTest
  static final ArchRule serviceNamingAndPlacement =
      classes()
          .that()
          .areAnnotatedWith(Service.class)
          .should()
          .haveSimpleNameEndingWith("Service")
          .andShould()
          .resideInAPackage("..application..");

  // application 層に port サブパッケージを持たない
  @ArchTest
  static final ArchRule applicationShouldNotHavePortPackage =
      noClasses()
          .should()
          .resideInAPackage("..application..port..")
          .as("application 層に port サブパッケージを配置しない");

  // --- 循環依存禁止 ---

  // shared は複数フィーチャーの domain に依存し、各フィーチャーは shared.exception に依存するため除外
  @ArchTest
  static final ArchRule noCyclicDependencies =
      slices()
          .matching(ROOT + "(*)..")
          .should()
          .beFreeOfCycles()
          .ignoreDependency(
              resideInAPackage("..shared.."), resideInAPackage("com.youmorry.expensetracker.."))
          .ignoreDependency(
              resideInAPackage("com.youmorry.expensetracker.."), resideInAPackage("..shared.."));
}
