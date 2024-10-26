/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.openhab.automation.java223.internal.codegeneration;

import static org.osgi.framework.wiring.BundleWiring.*;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.jar.Attributes;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;
import java.util.stream.Collectors;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.wiring.BundleWiring;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Create a JAR with some dependencies inside, useful to code from an external IDE.
 * This is purely a convenience JAR
 *
 * @author Gwendal Roulleau - Initial contribution, based on work from Jan N. Klug
 */
@NonNullByDefault
public class DependencyGenerator {

    public static final String CONVENIENCE_DEPENDENCIES_JAR = "convenience-dependencies.jar";

    private static final Logger logger = LoggerFactory.getLogger(DependencyGenerator.class);

    // A set of default dependencies to export
    private static final Set<String> DEFAULT_DEPENDENCIES = Set.of("org.openhab.automation.java223.common",
            "org.openhab.core.audio", "org.openhab.core.automation", "org.openhab.core.automation.events",
            "org.openhab.core.automation.util", "org.openhab.core.automation.module.script",
            "org.openhab.core.automation.module.script.action",
            "org.openhab.core.automation.module.script.defaultscope",
            "org.openhab.core.automation.module.script.rulesupport.shared",
            "org.openhab.core.automation.module.script.rulesupport.shared.simple", "org.openhab.core.common",
            "org.openhab.core.common.registry", "org.openhab.core.config.core", "org.openhab.core.events",
            "org.openhab.core.items", "org.openhab.core.items.events", "org.openhab.core.library.types",
            "org.openhab.core.library.items", "org.openhab.core.library.dimension", "org.openhab.core.model.script",
            "org.openhab.core.model.script.actions", "org.openhab.core.model.rule", "org.openhab.core.persistence",
            "org.openhab.core.persistence.extensions", "org.openhab.core.thing", "org.openhab.core.thing.events",
            "org.openhab.core.thing.binding", "org.openhab.core.transform", "org.openhab.core.transform.actions",
            "org.openhab.core.types", "org.openhab.core.voice", "com.google.gson", "org.openhab.core.library.unit",
            "tech.units.indriya");

    // A set of default classes to export
    private static final Set<String> DEFAULT_CLASSES_DEPENDENCIES = Set.of("org.eclipse.jdt.annotation.NonNull",
            "org.eclipse.jdt.annotation.NonNullByDefault", "org.eclipse.jdt.annotation.Nullable",
            "org.eclipse.jdt.annotation.DefaultLocation", "org.slf4j.LoggerFactory", "org.slf4j.Logger",
            "javax.measure.Quantity", "javax.measure.Unit", "org.slf4j.Marker", "javax.measure.spi.SystemOfUnits");

    // target library directory
    private final Path libDir;
    // additional bundle to export
    private String additionalBundlesConfig;
    // individual additional classes to export
    private String additionalClassesConfig;
    private final BundleContext bundleContext;

    private final Set<String> additionalClassesToExport = new HashSet<>();

    /**
     *
     * @param libDir The target library directory
     * @param additionalBundlesConfig Additional bundle to inspect. We will extract classes from it.
     * @param additionalClassesConfig Additional individual classes to add to the exported JAR.
     * @param bundleContext OSGI Bundle context
     */
    public DependencyGenerator(Path libDir, String additionalBundlesConfig, String additionalClassesConfig,
            BundleContext bundleContext) {
        this.libDir = libDir;
        this.additionalBundlesConfig = additionalBundlesConfig;
        this.additionalClassesConfig = additionalClassesConfig;
        this.bundleContext = bundleContext;
    }

    public void setAdditionalConfig(String additionalBundlesConfig, String additionalClassesConfig) {
        this.additionalBundlesConfig = additionalBundlesConfig;
        this.additionalClassesConfig = additionalClassesConfig;
    }

    /**
     * Generate a JAR with useful classes for a client project / IDE
     * This JAR is not needed; it's just a convenience for writing scripts smoothly
     */
    public synchronized void createCoreDependencies() {
        try (FileOutputStream outFile = new FileOutputStream(libDir.resolve(CONVENIENCE_DEPENDENCIES_JAR).toFile())) {
            Manifest manifest = new Manifest();
            manifest.getMainAttributes().put(Attributes.Name.MANIFEST_VERSION, "1.0");

            try (JarOutputStream target = new JarOutputStream(outFile, manifest)) {

                Set<String> dependencies = new HashSet<>(DEFAULT_DEPENDENCIES);
                dependencies.addAll(Arrays.asList(additionalBundlesConfig.split(",")));

                Set<String> searchIn = new HashSet<>();
                // search all dependencies
                for (String packageName : dependencies) {
                    // a bundle can have the exact name of the package we search, but also the name of a parent package,
                    // so we add them all in our list of search
                    String[] packageComponents = packageName.split("\\.");
                    for (int i = 1; i <= packageComponents.length; i++) {
                        searchIn.add(String.join(".", Arrays.copyOfRange(packageComponents, 0, i)));
                    }
                }

                // browse all bundle and search for matches with the list established above
                Set<String> packagesSuccessfullyExported = new HashSet<>();
                for (Bundle bundle : bundleContext.getBundles()) {
                    if (searchIn.contains(bundle.getSymbolicName())) { // matches !
                        copyExportedPackagesByBundleInspection(dependencies, bundle, target,
                                packagesSuccessfullyExported);
                    }
                }

                // we want to warn about the list of packages we didn't find
                if (logger.isWarnEnabled()) {
                    Set<String> packagesNotFound = new HashSet<>(DEFAULT_DEPENDENCIES);
                    packagesSuccessfullyExported.stream().map(s -> s.replaceAll("/", ".")).toList()
                            .forEach(packagesNotFound::remove);
                    for (String remainingPackage : packagesNotFound) {
                        logger.warn("Failed to found classes to export in package {}", remainingPackage);
                    }
                }

                // now the individual classes :
                Set<String> classesDependencies = new HashSet<>(DEFAULT_CLASSES_DEPENDENCIES);
                classesDependencies.addAll(Arrays.asList(additionalClassesConfig.split(",")));
                classesDependencies.addAll(additionalClassesToExport);
                copyExportedClassesByClassLoader(classesDependencies, target);
            }
        } catch (IOException e) {
            logger.warn("Failed to create dependencies jar in '{}': {}", libDir, e.getMessage());
        }
    }

    private static void copyExportedClassesByClassLoader(Set<String> classesToExtract, JarOutputStream target) {
        for (String classToExtract : classesToExtract) {
            if (classToExtract.isEmpty()) {
                continue;
            }
            String path = classToExtract.replaceAll("\\.", "/") + ".class";
            ClassLoader classLoader = DependencyGenerator.class.getClassLoader();
            if (classLoader == null) {
                logger.warn("Failed (no classloader) to copy from classpath : {}", classToExtract);
                return;
            }
            try (InputStream stream = classLoader.getResourceAsStream(path)) {
                if (stream != null) {
                    addEntryToJar(target, path, stream);
                } else {
                    logger.warn("InputStream {} from classpath is null", classToExtract);
                }
            } catch (IOException e) {
                logger.warn("Failed to copy classes '{}' from classpath : {}", classToExtract, e.getMessage());
            }
        }
    }

    private static void copyExportedPackagesByBundleInspection(Set<String> dependencies, Bundle bundle,
            JarOutputStream target, Set<String> classesSuccessfullyExported) {
        String exportPackage = bundle.getHeaders().get("Export-Package");
        if (exportPackage == null) {
            logger.warn("Bundle '{}' does not export any package!", bundle.getSymbolicName());
            return;
        }
        List<String> exportedPackages = Arrays.stream(exportPackage //
                .split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)")) // split only on comma, not in double quotes
                .map(s -> s.split(";")[0]) // get only the package name and drop uses, version, etc.
                .map(b -> b.replace(".", "/")).toList();
        Set<String> dependenciesWithSlash = dependencies.stream().map(b -> b.replace(".", "/"))
                .collect(Collectors.<String> toSet());

        bundle.adapt(BundleWiring.class).listResources("", "*.class", LISTRESOURCES_LOCAL + LISTRESOURCES_RECURSE)
                .forEach(classFile -> {
                    try {
                        int classNameStart = classFile.lastIndexOf("/");
                        if (classNameStart != -1) {
                            String packageName = classFile.substring(0, classNameStart);
                            if (!exportedPackages.contains(packageName)
                                    || !dependenciesWithSlash.contains(packageName)) {
                                return;
                            }

                            URL urlEntry = bundle.getEntry(classFile);
                            if (urlEntry == null) {
                                logger.warn("URL for {} is empty, skipping", classFile);
                            } else {
                                try (InputStream stream = urlEntry.openStream()) {
                                    addEntryToJar(target, classFile, stream);
                                    classesSuccessfullyExported.add(packageName);
                                }
                            }
                        }
                    } catch (IOException e) {
                        logger.warn("Failed to copy class '{}' from '{}': {}", classFile, bundle.getSymbolicName(),
                                e.getMessage());
                    }
                });
    }

    private static void addEntryToJar(JarOutputStream jar, String name, InputStream content) throws IOException {
        JarEntry jarEntry = new JarEntry(name);
        jar.putNextEntry(jarEntry);
        jar.write(content.readAllBytes());
        jar.closeEntry();
    }

    private boolean excludeFromExport(String classToExport) {
        return classToExport.startsWith("java.") || DEFAULT_DEPENDENCIES.stream() //
                .anyMatch(classToExport::startsWith);
    }

    private static void getAllInterfaces(@Nullable Class<?> cls, final Set<String> interfacesFound) {
        @Nullable
        Class<?> clsLocal = cls;
        while (clsLocal != null) {
            final Class<?>[] interfaces = clsLocal.getInterfaces();
            for (final Class<?> i : interfaces) {
                if (interfacesFound.add(i.getCanonicalName())) {
                    getAllInterfaces(i, interfacesFound);
                }
            }
            clsLocal = clsLocal.getSuperclass();
        }
    }

    private static List<String> getAllSuperclasses(final Class<?> cls) {
        final List<String> classes = new ArrayList<>();
        Class<?> superclass = cls.getSuperclass();
        while (superclass != null) {
            classes.add(superclass.getCanonicalName());
            superclass = superclass.getSuperclass();
        }
        return classes;
    }

    /**
     * Add classes to export inside the dependencies JAR
     * Purely for convenience
     *
     * @param allClassesToExport Classes to export inside the JAR
     */
    public void setClassesToAddToDependenciesLib(Set<String> allClassesToExport) {

        Set<String> newAdditionalClassesToExport = new HashSet<>();
        for (String clazzAsString : allClassesToExport) {
            Class<?> clazz;
            try {
                clazz = Class.forName(clazzAsString);
                newAdditionalClassesToExport.add(clazzAsString);
                newAdditionalClassesToExport.addAll(getAllSuperclasses(clazz));
                getAllInterfaces(clazz, newAdditionalClassesToExport);
            } catch (ClassNotFoundException e) {
                logger.warn("Cannot inspect class {} to add it as a dependency", clazzAsString);
            }
        }

        newAdditionalClassesToExport.removeIf(this::excludeFromExport);
        // check if there are new classes :
        newAdditionalClassesToExport.removeAll(additionalClassesToExport);
        if (!newAdditionalClassesToExport.isEmpty()) {
            additionalClassesToExport.addAll(newAdditionalClassesToExport);
            createCoreDependencies();
        }
    }
}
