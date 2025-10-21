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
package org.openhab.binding.unifiprotect.internal.media;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Locale;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.client.api.Response;
import org.eclipse.jetty.client.util.InputStreamResponseListener;
import org.eclipse.jetty.http.HttpMethod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * NativeHelper.
 *
 * @author Dan Cunningham - Initial contribution
 */
@NonNullByDefault
public class NativeHelper {
    private final Logger logger = LoggerFactory.getLogger(NativeHelper.class);
    private final Path baseDir;
    private final boolean downloadBinaries;
    private final HttpClient httpClient;

    public NativeHelper(Path baseDir, boolean downloadBinaries, HttpClient httpClient) {
        this.downloadBinaries = downloadBinaries;
        this.baseDir = baseDir;
        this.httpClient = httpClient;
    }

    public Path ensureGo2Rtc() throws IOException {
        return ensureBinary("go2rtc", getGo2RtcUrl());
    }

    public Path ensureFfmpeg() throws IOException {
        return ensureBinary("ffmpeg", getFfmpegUrl());
    }

    private Path ensureBinary(String name, String url) throws IOException {
        // if on PATH, just return
        if (isOnPath(name)) {
            return Paths.get(name);
        }

        // download/extract only the target binary, not the whole archive
        String osName = System.getProperty("os.name");
        String arch = System.getProperty("os.arch");
        if (osName == null || arch == null) {
            throw new IllegalStateException("Unsupported platform " + osName + " " + arch);
        }
        String osArch = osName.toLowerCase(Locale.ROOT) + "-" + arch.toLowerCase(Locale.ROOT);
        Path destDir = baseDir.resolve(osArch);
        Files.createDirectories(destDir);
        Path destBinary = destDir.resolve(name + (isWindows() ? ".exe" : ""));
        logger.debug("Checking for {} in {}", name, destBinary);
        if (!Files.exists(destBinary)) {
            if (!downloadBinaries) {
                throw new IOException("Binary " + name + " not found and downloadBinaries is false");
            }
            logger.debug("Downloading {} for {} at {}", name, osArch, url);
            if (url.isBlank()) {
                throw new IOException("No download URL configured for " + name + " on " + osArch);
            }
            downloadBinary(url, destBinary);
        }
        if (!isWindows() && Files.exists(destBinary)) {
            destBinary.toFile().setExecutable(true);
        }
        return destBinary;
    }

    private boolean isOnPath(String cmd) {
        String pathEnv = System.getenv("PATH");
        if (pathEnv == null) {
            return false;
        }
        for (String p : pathEnv.split(File.pathSeparator)) {
            Path bin = Paths.get(p, cmd + (isWindows() ? ".exe" : ""));
            if (Files.isExecutable(bin)) {
                return true;
            }
        }
        return false;
    }

    private boolean isWindows() {
        String osName = System.getProperty("os.name");
        return osName != null && osName.toLowerCase(Locale.ROOT).contains("win");
    }

    private void downloadBinary(String url, Path destBinary) throws IOException {
        String lower = url.toLowerCase(Locale.ROOT);
        boolean isZip = lower.endsWith(".zip");

        Files.createDirectories(destBinary.getParent());

        Path tmp = Files.createTempFile("native-helper-", isZip ? ".zip" : ".bin");
        // Stream download via Jetty to avoid buffering large files in memory
        Request request = httpClient.newRequest(url).method(HttpMethod.GET).timeout(120, TimeUnit.SECONDS);
        InputStreamResponseListener listener = new InputStreamResponseListener();
        request.send(listener);
        try {
            Response response;
            try {
                response = listener.get(120, TimeUnit.SECONDS);
            } catch (Exception e) {
                throw new IOException("Failed to download " + url, e);
            }
            int status = response.getStatus();
            if (status != 200) {
                throw new IOException("Unexpected HTTP status " + status + " for " + url);
            }
            try (InputStream in = listener.getInputStream()) {
                Files.copy(in, tmp, StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (IOException e) {
            throw e;
        }

        try {
            if (isZip) {
                try (ZipFile zip = new ZipFile(tmp.toFile())) {
                    String expectedFileName = destBinary.getFileName().toString();
                    ZipEntry match = zip.stream().filter(Objects::nonNull).filter(entry -> !entry.isDirectory())
                            .filter(entry -> {
                                String entryBaseName = Paths.get(entry.getName()).getFileName().toString();
                                return expectedFileName.equals(entryBaseName);
                            }).findFirst().orElseThrow(() -> new IOException(
                                    "Could not find " + expectedFileName + " inside archive: " + url));

                    try (InputStream zin = zip.getInputStream(match)) {
                        Files.copy(zin, destBinary, StandardCopyOption.REPLACE_EXISTING);
                    }
                }
            } else {
                Files.move(tmp, destBinary, StandardCopyOption.REPLACE_EXISTING);
            }
        } finally {
            Files.deleteIfExists(tmp);
        }

        if (!isWindows()) {
            destBinary.toFile().setExecutable(true);
        }
    }

    private String getGo2RtcUrl() {
        String os = System.getProperty("os.name");
        String arch = System.getProperty("os.arch");
        if (os == null || arch == null) {
            throw new IllegalStateException("Unsupported platform " + os + " " + arch);
        }
        os = os.toLowerCase(Locale.ROOT);
        arch = arch.toLowerCase(Locale.ROOT);
        if (os.contains("linux")
                && (arch.contains("amd64") || arch.contains("x86_64") || "x64".equals(arch) || "amd64".equals(arch))) {
            return "https://github.com/AlexxIT/go2rtc/releases/latest/download/go2rtc_linux_amd64";
        }
        if (os.contains("linux") && (arch.contains("aarch64") || arch.contains("arm64"))) {
            return "https://github.com/AlexxIT/go2rtc/releases/latest/download/go2rtc_linux_arm64";
        }
        if (os.contains("freebsd")
                && (arch.contains("amd64") || arch.contains("x86_64") || "x64".equals(arch) || "amd64".equals(arch))) {
            return "https://github.com/AlexxIT/go2rtc/releases/download/v1.9.9/go2rtc_freebsd_amd64.zip";
        }
        if (os.contains("freebsd") && (arch.contains("aarch64") || arch.contains("arm64"))) {
            return "https://github.com/AlexxIT/go2rtc/releases/download/v1.9.9/go2rtc_freebsd_arm64.zip";
        }
        if (os.contains("mac") && arch.contains("arm")) {
            return "https://github.com/AlexxIT/go2rtc/releases/latest/download/go2rtc_mac_arm64.zip";
        }
        if (os.contains("mac")) {
            return "https://github.com/AlexxIT/go2rtc/releases/latest/download/go2rtc_mac_amd64.zip";
        }
        if (os.contains("win")) {
            return "https://github.com/AlexxIT/go2rtc/releases/latest/download/go2rtc_win64.zip";
        }
        throw new IllegalStateException("Unsupported platform " + os + " " + arch);
    }

    private String getFfmpegUrl() {
        String os = System.getProperty("os.name");
        String arch = System.getProperty("os.arch");
        if (os == null || arch == null) {
            throw new IllegalStateException("Unsupported platform " + os + " " + arch);
        }
        os = os.toLowerCase(Locale.ROOT);
        arch = arch.toLowerCase(Locale.ROOT);
        if (os.contains("linux") && arch.contains("amd64")) {
            return "https://github.com/digitaldan/openhab-addons/releases/download/unifiprotect/ffmpeg-master-latest-linux64-gpl.zip";
        }
        if (os.contains("linux") && arch.contains("aarch64")) {
            return "https://github.com/digitaldan/openhab-addons/releases/download/unifiprotect/ffmpeg-master-latest-linuxarm64-gpl.zip";
        }
        if (os.contains("mac") && arch.contains("amd64")) {
            return "https://github.com/digitaldan/openhab-addons/releases/download/unifiprotect/ffmpeg-8.0-darwin.zip";
        }
        if (os.contains("win")) {
            return "https://github.com/digitaldan/openhab-addons/releases/download/unifiprotect/ffmpeg-master-latest-win64-gpl-shared.zip";
        }
        throw new IllegalStateException("Unsupported platform " + os + " " + arch);
    }
}
