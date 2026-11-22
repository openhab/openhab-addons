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
package org.openhab.binding.homekit.internal.camera.ffmpeg;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.Duration;
import java.util.Locale;
import java.util.Objects;
import java.util.stream.Stream;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Locates or downloads the ffmpeg binary used to trans-code the camera's AAC-ELD audio for go2rtc. The homebridge
 * static
 * build is downloaded into the binding's cache directory if it is not already present. A ffmpeg on the PATH is never
 * used, because it may lack the codecs (for example libfdk_aac) that the binding requires.
 *
 * @author Andrew Fiddian-Green - Initial contribution
 */
@NonNullByDefault
public final class FfmpegInstaller {

    static final String VERSION = "2.2.0";
    private static final String NAME = "ffmpeg";
    private static final String RELEASE_URL = "https://github.com/homebridge/ffmpeg-for-homebridge/releases/download/v"
            + VERSION + "/";

    private static final Logger LOGGER = LoggerFactory.getLogger(FfmpegInstaller.class);
    private static final HttpClient HTTP_CLIENT = HttpClient.newBuilder().followRedirects(HttpClient.Redirect.NORMAL)
            .connectTimeout(Duration.ofSeconds(30)).build();

    private FfmpegInstaller() {
    }

    /**
     * Returns the command to execute ffmpeg, downloading the binary first if it is not already cached. A ffmpeg on the
     * PATH is never used, so the binding always runs the version it expects.
     * Synchronized so that concurrent callers do not download or extract into the same location at once.
     *
     * @param baseDir the directory in which downloaded binaries are cached
     * @throws IOException if the binary is missing and cannot be downloaded or extracted
     * @throws InterruptedException if interrupted while downloading
     */
    public static synchronized String ensureBinary(Path baseDir) throws IOException, InterruptedException {
        String os = Objects.requireNonNull(System.getProperty("os.name", ""));
        String arch = Objects.requireNonNull(System.getProperty("os.arch", ""));
        String asset = assetName(os, arch);
        Path destDir = baseDir.resolve(NAME);
        Path destBinary = destDir.resolve(NAME + (isWindows(os) ? ".exe" : ""));
        if (!Files.exists(destBinary)) {
            LOGGER.debug("Downloading {} to {}", asset, destBinary);
            download(RELEASE_URL + asset, asset, destDir, destBinary);
        }
        if (!destBinary.toFile().setExecutable(true)) {
            throw new IOException("Cannot make " + destBinary + " executable");
        }
        return destBinary.toString();
    }

    /**
     * Returns the homebridge release asset name for the given platform. Windows is published as a raw executable, all
     * other platforms as tar.gz archives.
     *
     * @throws IOException if the platform is not supported
     */
    static String assetName(String osName, String arch) throws IOException {
        String os = osName.toLowerCase(Locale.ROOT);
        String cpu = arch.toLowerCase(Locale.ROOT);
        boolean x64 = cpu.contains("amd64") || cpu.contains("x86_64") || "x64".equals(cpu);
        boolean arm64 = cpu.contains("aarch64") || cpu.contains("arm64");
        if (os.contains("linux") && x64) {
            return "ffmpeg-alpine-x86_64.tar.gz";
        }
        if (os.contains("linux") && arm64) {
            return "ffmpeg-alpine-aarch64.tar.gz";
        }
        if (os.contains("freebsd") && x64) {
            return "ffmpeg-freebsd-x86_64.tar.gz";
        }
        if (os.contains("mac")) {
            return arm64 || cpu.contains("arm") ? "ffmpeg-darwin-arm64.tar.gz" : "ffmpeg-darwin-x86_64.tar.gz";
        }
        if (os.contains("win")) {
            return "ffmpeg.exe";
        }
        throw new IOException("Unsupported platform for ffmpeg: " + osName + " " + arch);
    }

    private static boolean isWindows(String os) {
        return os.toLowerCase(Locale.ROOT).contains("win");
    }

    private static void download(String url, String asset, Path destDir, Path destBinary)
            throws IOException, InterruptedException {
        Files.createDirectories(destDir);
        Path tmp = Files.createTempFile(destDir, "ffmpeg-download-", ".tmp");
        try {
            HttpRequest request = HttpRequest.newBuilder(URI.create(url)).timeout(Duration.ofMinutes(5)).GET().build();
            HttpResponse<Path> response = HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofFile(tmp));
            if (response.statusCode() != 200) {
                throw new IOException("Unexpected HTTP status " + response.statusCode() + " for " + url);
            }
            if (asset.endsWith(".tar.gz")) {
                extractFromTarGz(tmp, destBinary, url);
            } else {
                Files.move(tmp, destBinary, StandardCopyOption.REPLACE_EXISTING);
            }
        } finally {
            Files.deleteIfExists(tmp);
        }
    }

    /**
     * Extracts the archive with the system tar, as the unifi binding does, and moves the ffmpeg executable it finds
     * in the archive to the destination.
     */
    private static void extractFromTarGz(Path archive, Path destBinary, String url)
            throws IOException, InterruptedException {
        Path extractDir = Files.createTempDirectory(destBinary.getParent(), "ffmpeg-extract-");
        try {
            Process process = new ProcessBuilder("tar", "-xzf", archive.toString(), "-C", extractDir.toString())
                    .redirectErrorStream(true).start();
            process.getInputStream().readAllBytes();
            int exit = process.waitFor();
            if (exit != 0) {
                throw new IOException("Failed to extract " + url + " with tar (exit=" + exit + ")");
            }
            try (Stream<Path> files = Files.walk(extractDir)) {
                Path found = files.filter(p -> Files.isRegularFile(p) && NAME.equals(p.getFileName().toString()))
                        .findFirst().orElseThrow(() -> new IOException("Could not find " + NAME + " inside " + url));
                Files.move(found, destBinary, StandardCopyOption.REPLACE_EXISTING);
            }
        } finally {
            deleteRecursively(extractDir);
        }
    }

    private static void deleteRecursively(Path dir) throws IOException {
        try (Stream<Path> files = Files.walk(dir)) {
            for (Path p : files.sorted((a, b) -> b.compareTo(a)).toList()) {
                Files.deleteIfExists(p);
            }
        }
    }
}
