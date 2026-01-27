/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
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
package org.openhab.automation.java223.internal.strategy.jarloader;

import java.io.FileInputStream;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Predicate;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.tools.FileObject;
import javax.tools.ForwardingJavaFileManager;
import javax.tools.JavaFileManager;
import javax.tools.JavaFileObject;
import javax.tools.JavaFileObject.Kind;
import javax.tools.StandardLocation;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.automation.java223.common.Java223Exception;
import org.openhab.automation.java223.internal.codegeneration.DependencyGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link JarFileManager} is an implementation of {@link JavaFileManager} with extensions for JAR files
 *
 * @author Jan N. Klug - Initial contribution
 * @author Gwendal Roulleau - Use of a factory
 */
@NonNullByDefault
public class JarFileManager<M extends JavaFileManager> extends ForwardingJavaFileManager<M> {
    private static final Logger logger = LoggerFactory.getLogger(JarFileManager.class);

    private final Map<String, List<JavaFileObject>> additionalPackages;
    private final ClassLoader classLoader;

    public JarFileManager(M fileManager, ClassLoader classLoader,
            Map<String, List<JavaFileObject>> additionalPackages) {
        super(fileManager);
        this.classLoader = classLoader;
        this.additionalPackages = additionalPackages;
    }

    @Override
    public @Nullable ClassLoader getClassLoader(@Nullable Location location) {
        return classLoader;
    }

    @Override
    public @NonNullByDefault({}) Iterable<JavaFileObject> list(@Nullable Location location,
            @Nullable String packageName, Set<JavaFileObject.Kind> kinds, boolean recurse) throws IOException {
        Iterable<JavaFileObject> stdResult = fileManager.list(location, packageName, kinds, recurse);

        if (location != StandardLocation.CLASS_PATH || !kinds.contains(JavaFileObject.Kind.CLASS)) {
            return stdResult;
        }

        List<JavaFileObject> mergedFileObjects = new ArrayList<>();
        stdResult.forEach(mergedFileObjects::add);
        mergedFileObjects.addAll(additionalPackages.getOrDefault(packageName, List.of()));

        return mergedFileObjects;
    }

    @Override
    public JavaFileObject getJavaFileForOutput(@Nullable Location location, @Nullable String className,
            @Nullable Kind kind, @Nullable FileObject sibling) throws IOException {
        if (sibling instanceof JarFileObject) {
            URI outFile = URI.create(removeExtension(sibling.toUri().toString()) + ".class");
            return JarFileObject.classFileObject(outFile);
        }
        return fileManager.getJavaFileForOutput(location, className, kind, sibling);
    }

    @Override
    public String inferBinaryName(@Nullable Location location, @Nullable JavaFileObject file) {
        if (file instanceof JarFileObject) {
            return removeExtension(getPath(file.toUri()).replace("/", ".").substring(1));
        }
        return fileManager.inferBinaryName(location, file);
    }

    private static String removeExtension(String name) {
        return name.substring(0, name.lastIndexOf("."));
    }

    private static String getPath(URI uri) {
        if ("jar".equals(uri.getScheme())) {
            String uriString = uri.toString();
            return uriString.substring(uriString.lastIndexOf("!"));
        } else {
            return uri.getPath();
        }
    }

    /**
     * Maintain an internal state to instantiate JarFileManager easily
     *
     * @author Gwendal Roulleau - Use of a factory
     *
     */
    public static class JarFileManagerFactory {

        private static final Lock FILEMANAGER_LOCK = new ReentrantLock();

        private static final Predicate<Path> JAR_FILTER = p -> p.toString().endsWith(".jar");
        private Map<String, List<JavaFileObject>> upToDateAdditionalPackages = Map.of();
        private ClassLoader upToDateClassLoader;

        private final ClassLoader parentClassLoader;
        MessageDigest md5Digest;

        byte[] md5LibSum = new byte[0];

        Path libDirectory;

        private final Set<Path> jarPaths = Collections.synchronizedSet(new HashSet<>());

        public JarFileManagerFactory(Path libDirectory, ClassLoader parentClassLoader) {
            this.parentClassLoader = parentClassLoader;
            this.libDirectory = libDirectory;
            // temporary/default use of the parent :
            this.upToDateClassLoader = parentClassLoader;
            try {
                this.md5Digest = MessageDigest.getInstance("MD5");
            } catch (NoSuchAlgorithmException e) {
                throw new Java223Exception("Cannot instantiate md5 digest. Should not happen");
            }
        }

        public Set<Path> getAllJarPaths() {
            return jarPaths;
        }

        public JarFileManager<JavaFileManager> create(JavaFileManager fileManager) {
            return new JarFileManager<>(fileManager, upToDateClassLoader, upToDateAdditionalPackages);
        }

        public void rebuildLibPackages() {
            FILEMANAGER_LOCK.lock();

            jarPaths.clear();
            try (Stream<Path> libFileStream = Files.list(libDirectory)) {
                List<Path> libFiles = libFileStream.filter(JAR_FILTER) //
                        .filter((path) -> !path.getFileName().toString() // exclude convenience lib
                                .equals(DependencyGenerator.CONVENIENCE_DEPENDENCIES_JAR)) //
                        .collect(Collectors.toList());

                // first check if it's really needed, in case we overwrite a file with the same content
                for (Path path : libFiles) {
                    md5Digest.update(Files.readAllBytes(path));
                }
                byte[] newMd5LibSum = md5Digest.digest();
                if (Arrays.equals(newMd5LibSum, md5LibSum)) {
                    logger.debug("No change and no need to rebuild lib package classloader");
                    return;
                }

                logger.info("Full rebuild of java223 classpath");
                logger.debug("Libraries to load from '{}' to memory: {}", libDirectory, libFiles);

                JarClassLoader newClassLoader = new JarClassLoader(parentClassLoader);
                Map<String, List<JavaFileObject>> additionalPackages = new HashMap<>();
                libFiles.forEach(libFile -> processLibrary(libFile, newClassLoader, additionalPackages));

                upToDateClassLoader = newClassLoader;
                upToDateAdditionalPackages = additionalPackages;

                md5LibSum = newMd5LibSum;

            } catch (IOException e) {
                logger.warn("Could not load libraries: {}", e.getMessage());
            } finally {
                FILEMANAGER_LOCK.unlock();
            }
        }

        public void addLibPackage(Path newLib) {
            if (newLib.getFileName().toString() //
                    .equals(DependencyGenerator.CONVENIENCE_DEPENDENCIES_JAR)) {
                return;
            }
            jarPaths.add(newLib);
            try {
                FILEMANAGER_LOCK.lock();
                logger.debug("Library to load to memory: {}", newLib);
                if (upToDateClassLoader instanceof JarClassLoader upToDateJarClassLoader) {
                    processLibrary(newLib, upToDateJarClassLoader, upToDateAdditionalPackages);
                } else {
                    throw new Java223Exception("Initialization error. The class loader should have been initialized");
                }
            } finally {
                FILEMANAGER_LOCK.unlock();
            }
        }

        private void processLibrary(Path jarFile, JarClassLoader jarClassLoader,
                Map<String, List<JavaFileObject>> additionalPackages) {
            try (JarInputStream jis = new JarInputStream(new FileInputStream(jarFile.toFile()))) {
                JarEntry entry;
                while ((entry = jis.getNextJarEntry()) != null) {
                    if (!entry.getName().endsWith(JarClassLoader.CLASS_FILE_TYPE)) {
                        continue;
                    }
                    String entryName = entry.getName();
                    int fileNameStart = entryName.lastIndexOf("/");
                    String packageName = fileNameStart == -1 ? ""
                            : entry.getName().substring(0, fileNameStart).replace("/", ".");
                    URI classUri = URI.create("jar:" + jarFile.toUri() + "!/" + entryName);

                    Objects.requireNonNull(additionalPackages.computeIfAbsent(packageName, k -> new ArrayList<>()))
                            .add(JarFileObject.classFileObject(classUri));
                    logger.trace("Added entry {} to additional libraries with package {}.", entry, packageName);
                }
            } catch (IOException e) {
                logger.warn("Failed to process {}: {}", jarFile, e.getMessage());
            }

            jarClassLoader.addJar(jarFile);
            logger.info("JAR loaded in the java223 script classpath: {}", jarFile);
        }
    }
}
