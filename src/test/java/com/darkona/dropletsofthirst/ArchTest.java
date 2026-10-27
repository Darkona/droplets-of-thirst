package com.darkona.dropletsofthirst;

import com.tngtech.archunit.base.DescribedPredicate;
import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.domain.JavaClasses;
import dev.darkona.jdlib.testkit.ArchZones;
import org.junit.jupiter.api.Test;

import static com.tngtech.archunit.base.DescribedPredicate.not;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

class ArchTest {

    private static final String MOD = "com.darkona.dropletsofthirst";
    private static final JavaClasses CLASSES = ArchZones.importFolders("droplets.classes");
    private static final DescribedPredicate<JavaClass> PLATFORM = ArchZones.packages("java..", "javax..", "jdk..", "net.minecraft..",
            "com.mojang..", "net.neoforged..", "it.unimi.dsi.fastutil..", "org.jetbrains.annotations..", "org.jspecify.annotations..",
            "org.joml..", "org.slf4j..", "com.electronwill.nightconfig..", "com.google..", "io.netty..", "org.apache..", "dev.darkona.jdlib..", MOD + "..");
    private static final DescribedPredicate<JavaClass> USES_CLIENT = new DescribedPredicate<>("use client classes or the client-only JEI API") {
        @Override
        public boolean test(JavaClass type) {
            return type.getDirectDependenciesFromSelf().stream().anyMatch(dependency -> isClientPackage(dependency.getTargetClass().getPackageName()));
        }
    };

    private static boolean isClientPackage(String name) {
        return name.startsWith("net.minecraft.client") || name.startsWith("net.neoforged.neoforge.client") || name.startsWith("mezz.jei");
    }

    @Test
    void deferredRegistersLiveInIndexOrCompat() {
        noClasses().that().resideInAPackage(MOD + "..").and().resideOutsideOfPackages(MOD + ".index..", MOD + ".compat..")
                .should().dependOnClassesThat().haveNameMatching("net\\.neoforged\\.neoforge\\.registries\\.DeferredRegister(\\$.*)?")
                .check(CLASSES);
    }

    @Test
    void configSpecsLiveInConfig() {
        noClasses().that().resideInAPackage(MOD + "..").and().resideOutsideOfPackage(MOD + ".config..")
                .should().dependOnClassesThat().haveNameMatching("net\\.neoforged\\.neoforge\\.common\\.ModConfigSpec(\\$Builder)?")
                .check(CLASSES);
    }

    @Test
    void dataProvidersLiveInData() {
        classes().that().areAssignableTo("net.minecraft.data.DataProvider")
                .should().resideInAPackage(MOD + ".data..")
                .allowEmptyShould(true)
                .check(CLASSES);
    }

    @Test
    void payloadsLiveInNetwork() {
        classes().that().areAssignableTo("net.minecraft.network.protocol.common.custom.CustomPacketPayload")
                .should().resideInAPackage(MOD + ".network..")
                .check(CLASSES);
    }

    @Test
    void theClientCodeOfAFeatureLivesInItsClientSubpackage() {
        classes().that().resideInAnyPackage(MOD + ".content..", MOD + ".core..").and(USES_CLIENT)
                .should().resideInAPackage("..client..")
                .because("renderers, tooltips and client event handlers go in the client subpackage of the feature, next to what they draw")
                .check(CLASSES);
    }

    @Test
    void serverCodeDoesNotNameClientCode() {
        noClasses().that().resideInAPackage(MOD + "..").and(not(USES_CLIENT))
                .and().doNotHaveFullyQualifiedName(MOD + ".DropletsClient").and().resideOutsideOfPackage(MOD + ".index.client..")
                .should().dependOnClassesThat(USES_CLIENT.and(JavaClass.Predicates.resideInAPackage(MOD + "..")))
                .check(CLASSES);
    }

    @Test
    void onlyCompatAndMixinsNameOtherMods() {
        classes().that().resideInAPackage(MOD + "..").and().resideOutsideOfPackages(MOD + ".compat..", MOD + ".mixin..")
                .should().onlyDependOnClassesThat(PLATFORM)
                .check(CLASSES);
    }

    @Test
    void mixinPackagesHoldOnlyMixins() {
        classes().that().resideInAPackage(MOD + ".mixin..").and().areTopLevelClasses()
                .should().beAnnotatedWith("org.spongepowered.asm.mixin.Mixin")
                .orShould().beAssignableTo("org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin")
                .check(CLASSES);
    }
}
