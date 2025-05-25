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
package org.openhab.binding.matter.internal.client;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.api.Response;
import org.eclipse.jetty.client.util.InputStreamResponseListener;
import org.eclipse.jetty.http.HttpMethod;
import org.eclipse.jetty.http.HttpStatus;
import org.openhab.core.OpenHAB;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

/**
 * Manages the Node.js runtime for the Matter binding
 * 
 * This class provides methods for checking the system installed version of Node.js,
 * downloading and extracting the latest version of Node.js, and finding the Node.js
 * executable.
 * 
 * @author Dan Cunningham - Initial contribution
 */
@NonNullByDefault
class NodeJSRuntimeManager {
    private final Logger logger = LoggerFactory.getLogger(NodeJSRuntimeManager.class);

    private static final String NODE_BASE_VERSION = "v22";
    private static final String NODE_DEFAULT_VERSION = "v22.12.0";
    private static final String NODE_MIN_VERSION = "v18.0.0";

    private static final String NODE_INDEX_URL = "https://nodejs.org/dist/index.json";

    private static final String BASE_URL = "https://nodejs.org/dist/";
    private static final String CACHE_DIR = Paths
            .get(OpenHAB.getUserDataFolder(), "cache", "org.openhab.binding.matter", "node_cache").toString();

    private String platform = "";
    private String arch = "";
    private String nodeExecutable = "";
    private org.eclipse.jetty.client.HttpClient client;

    public NodeJSRuntimeManager(HttpClient client) {
        this.client = client;
        detectPlatformAndArch();
    }

    public String getNodePath() throws IOException {
        // Check if system installed node is at least the minimum required version
        if (checkSystemInstalledVersion(NODE_MIN_VERSION)) {
            logger.debug("Using system installed node");
            return nodeExecutable;
        }

        // Download the latest version of Node.js if not already installed
        String version = getLatestVersion();
        String cacheDir = CACHE_DIR + File.separator + platform + "-" + arch + File.separator + version;
        Path nodePath = findNodeExecutable(cacheDir, version);

        if (nodePath == null) {
            downloadAndExtract(cacheDir, version);
            nodePath = findNodeExecutable(cacheDir, version);
            if (nodePath == null) {
                throw new IOException("Unable to locate Node.js executable after download and extraction");
            }
        }

        return nodePath.toString();
    }

    private void detectPlatformAndArch() {
        String os = Optional.ofNullable(System.getProperty("os.name")).orElseGet(() -> "unknown").toLowerCase();
        String arch = Optional.ofNullable(System.getProperty("os.arch")).orElseGet(() -> "unknown").toLowerCase();

        if (os.contains("win")) {
            platform = "win";
            nodeExecutable = "node.exe";
        } else if (os.contains("mac")) {
            platform = "darwin";
            nodeExecutable = "node";
        } else if (os.contains("nux")) {
            platform = "linux";
            nodeExecutable = "node";
        } else {
            throw new UnsupportedOperationException("Unsupported operating system");
        }

        if (arch.contains("amd64") || arch.contains("x86_64")) {
            this.arch = "x64";
        } else if (arch.contains("aarch64") || arch.contains("arm64")) {
            this.arch = "arm64";
        } else if (arch.contains("arm")) {
            this.arch = "armv7l";
        } else {
            throw new UnsupportedOperationException("Unsupported architecture");
        }
    }

    private String getLatestVersion() {
        try {
            ContentResponse response = client.newRequest(NODE_INDEX_URL).method(HttpMethod.GET).send();
            String json = response.getContentAsString();
            Gson gson = new Gson();
            Type listType = new TypeToken<List<NodeVersion>>() {
            }.getType();
            List<NodeVersion> versions = gson.fromJson(json, listType);
            if (versions != null) {
                NodeVersion latest = versions.stream().filter(v -> v.version.startsWith(NODE_BASE_VERSION + "."))
                        .max(Comparator.comparing(v -> v.version)).orElse(null);
                if (latest != null) {
                    return latest.version;
                } else {
                    logger.debug("Could not find latest version of Node.js, using default version: {}",
                            NODE_DEFAULT_VERSION);
                }
            }
        } catch (Exception e) {
            logger.debug("Could not fetch latest version of Node.js, using default version: {}", NODE_DEFAULT_VERSION,
                    e);
        }
        return NODE_DEFAULT_VERSION;
    }

    private @Nullable Path findNodeExecutable(String cacheDir, String version) throws IOException {
        Path rootDir = Paths.get(cacheDir);
        if (!Files.exists(rootDir)) {
            return null;
        }

        try (DirectoryStream<Path> stream = Files.newDirectoryStream(rootDir)) {
            for (Path path : stream) {
                if (Files.isDirectory(path) && path.getFileName().toString().startsWith("node-" + version)) {
                    Path execPath = path.resolve("bin").resolve(nodeExecutable);
                    if (Files.exists(execPath)) {
                        return execPath;
                    }

                    // windows does not have a 'bin' directory
                    execPath = path.resolve(nodeExecutable);
                    if (Files.exists(execPath)) {
                        return execPath;
                    }
                }
            }
        }
        return null;
    }

    private void downloadAndExtract(String cacheDir, String version) throws IOException {
        String fileName = "node-" + version + "-" + platform + "-" + arch
                + ("win".equals(platform) ? ".zip" : ".tar.gz");
        String downloadUrl = BASE_URL + version + "/" + fileName;

        Path downloadPath = Paths.get(cacheDir, fileName);
        Files.createDirectories(downloadPath.getParent());

        logger.info("Downloading Node.js from: {}", downloadUrl);
        try {
            InputStreamResponseListener listener = new InputStreamResponseListener();
            client.newRequest(downloadUrl).method(HttpMethod.GET).send(listener);
            Response response = listener.get(5, TimeUnit.SECONDS);

            if (response.getStatus() == HttpStatus.OK_200) {
                try (InputStream responseContent = listener.getInputStream()) {
                    Files.copy(responseContent, downloadPath, StandardCopyOption.REPLACE_EXISTING);
                }
            } else {
                throw new IOException("Failed to download Node.js: HTTP " + response.getStatus());
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IOException("Download interrupted", e);
        } catch (TimeoutException | ExecutionException e) {
            throw new IOException("Failed to download Node.js", e);
        }

        logger.debug("Extracting Node.js");
        if ("win".equals(platform)) {
            unzip(downloadPath.toString(), cacheDir);
        } else {
            untar(downloadPath.toString(), cacheDir);
        }

        Files.delete(downloadPath);
    }

    private void unzip(String zipFilePath, String destDir) throws IOException {
        try (ZipInputStream zis = new ZipInputStream(new FileInputStream(zipFilePath))) {
            ZipEntry zipEntry;
            while ((zipEntry = zis.getNextEntry()) != null) {
                Path newPath = Paths.get(destDir, zipEntry.getName());
                if (zipEntry.isDirectory()) {
                    Files.createDirectories(newPath);
                } else {
                    Files.createDirectories(newPath.getParent());
                    Files.copy(zis, newPath, StandardCopyOption.REPLACE_EXISTING);
                }
                zis.closeEntry();
            }
        }
    }

    private void untar(String tarFilePath, String destDir) throws IOException {
        ProcessBuilder pb = new ProcessBuilder("tar", "-xzf", tarFilePath, "-C", destDir);
        Process p = pb.start();
        try {
            p.waitFor();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IOException("Interrupted while extracting tar file", e);
        }
    }

    private boolean checkSystemInstalledVersion(String requiredVersion) {
        try {
            Process process = new ProcessBuilder(nodeExecutable, "--version").start();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                String versionLine = reader.readLine();
                if (versionLine == null || !versionLine.startsWith("v")) {
                    logger.debug("unexpected node output {}", versionLine);
                    return false;
                }
                logger.debug("node found {}", versionLine);
                String currentVersion = versionLine.substring(1); // Remove the leading 'v'
                return compareVersions(currentVersion, requiredVersion) >= 0;
            }
        } catch (Exception e) {
            return false;
        }
    }

    private int compareVersions(String version1, String version2) {
        if (version1.indexOf("v") == 0) {
            version1 = version1.substring(1);
        }
        if (version2.indexOf("v") == 0) {
            version2 = version2.substring(1);
        }
        String[] parts1 = version1.split("\\.");
        String[] parts2 = version2.split("\\.");

        int length = Math.max(parts1.length, parts2.length);
        for (int i = 0; i < length; i++) {
            int v1 = i < parts1.length ? Integer.parseInt(parts1[i]) : 0;
            int v2 = i < parts2.length ? Integer.parseInt(parts2[i]) : 0;

            if (v1 != v2) {
                return Integer.compare(v1, v2);
            }
        }
        return 0;
    }

    static class NodeVersion {
        public String version = "";

        @Override
        public String toString() {
            return version;
        }
    }
}
