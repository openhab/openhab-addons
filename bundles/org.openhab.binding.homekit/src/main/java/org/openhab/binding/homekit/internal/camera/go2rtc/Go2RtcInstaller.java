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
package org.openhab.binding.homekit.internal.camera.go2rtc;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.Duration;
import java.util.Enumeration;
import java.util.Locale;
import java.util.Objects;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Locates or downloads the go2rtc binary. The release asset is downloaded into the binding's cache directory if it is
 * not already present. A go2rtc binary on the PATH is never used, so the binding always runs the version it expects.
 *
 * @author Andrew Fiddian-Green - Initial contribution
 */
@NonNullByDefault
public class Go2RtcInstaller {

    static final String VERSION = "1.9.14";
    private static final String NAME = "go2rtc";
    private static final String RELEASE_URL = "https://github.com/AlexxIT/go2rtc/releases/download/v" + VERSION + "/";

    private final Logger logger = LoggerFactory.getLogger(Go2RtcInstaller.class);
    private final Path baseDir;
    private final HttpClient httpClient;

    public Go2RtcInstaller(Path baseDir) {
        logger.debug("Go2RtcInstaller() called");
        this.baseDir = baseDir;
        this.httpClient = HttpClient.newBuilder().followRedirects(HttpClient.Redirect.NORMAL)
                .connectTimeout(Duration.ofSeconds(30)).build();
    }

    /**
     * Returns the command to execute go2rtc, downloading the binary first if it is not available.
     *
     * @throws IOException if the binary is missing and cannot be downloaded or extracted
     * @throws InterruptedException if interrupted while downloading
     */
    public Path ensureBinary() throws IOException, InterruptedException {
        String os = Objects.requireNonNull(System.getProperty("os.name", ""));
        String arch = Objects.requireNonNull(System.getProperty("os.arch", ""));
        logger.debug("Go2RtcInstaller() os={}, arch={}", os, arch);
        String asset = assetName(os, arch);
        Path path = baseDir.resolve(NAME);
        Path binary = path.resolve(NAME + (isWindows(os) ? ".exe" : ""));
        logger.debug("ensureBinary() destBinary={}", binary);
        if (!Files.exists(binary)) {
            logger.debug("Downloading {} to {}", asset, binary);
            download(RELEASE_URL + asset, asset, path, binary, os);
        }
        if (!binary.toFile().setExecutable(true)) {
            logger.debug("ensureBinary() cannot make {} executable", binary);
            throw new IOException("Cannot make " + binary + " executable");
        }
        return binary;
    }

    /**
     * Returns the release asset name for the given platform.
     * Linux amd64 and arm64 are published as raw binaries, all other platforms as zip archives.
     *
     * @throws IOException if the platform is not supported
     */
    static String assetName(String osName, String arch) throws IOException {
        String os = osName.toLowerCase(Locale.ROOT);
        String cpu = arch.toLowerCase(Locale.ROOT);
        boolean x64 = cpu.contains("amd64") || cpu.contains("x86_64") || "x64".equals(cpu);
        boolean arm64 = cpu.contains("aarch64") || cpu.contains("arm64");
        if (os.contains("linux") && x64) {
            return "go2rtc_linux_amd64";
        }
        if (os.contains("linux") && arm64) {
            return "go2rtc_linux_arm64";
        }
        if (os.contains("freebsd") && x64) {
            return "go2rtc_freebsd_amd64.zip";
        }
        if (os.contains("freebsd") && arm64) {
            return "go2rtc_freebsd_arm64.zip";
        }
        if (os.contains("mac")) {
            return arm64 || cpu.contains("arm") ? "go2rtc_mac_arm64.zip" : "go2rtc_mac_amd64.zip";
        }
        if (os.contains("win")) {
            return "go2rtc_win64.zip";
        }
        throw new IOException("Unsupported platform for go2rtc: " + osName + " " + arch);
    }

    private static boolean isWindows(String os) {
        return os.toLowerCase(Locale.ROOT).contains("win");
    }

    /**
     * Downloads the given URL to a temporary file, then moves it to the destination path.
     * If the asset is a zip archive, extracts the expected binary from it.
     */
    private void download(String url, String asset, Path destDir, Path destBinary, String os)
            throws IOException, InterruptedException {
        Files.createDirectories(destDir);
        Path tmp = Files.createTempFile(destDir, "go2rtc-download-", ".tmp");
        try {
            HttpRequest request = HttpRequest.newBuilder(URI.create(url)).timeout(Duration.ofMinutes(2)).GET().build();
            HttpResponse<Path> response = httpClient.send(request, HttpResponse.BodyHandlers.ofFile(tmp));
            if (response.statusCode() != 200) {
                throw new IOException("Unexpected HTTP status " + response.statusCode() + " for " + url);
            }

            if (asset.endsWith(".zip")) {
                extractFromZip(tmp, destBinary, url);
            } else {
                Files.move(tmp, destBinary, StandardCopyOption.REPLACE_EXISTING);
            }
        } finally {
            Files.deleteIfExists(tmp);
        }
        if (!isWindows(os) && !destBinary.toFile().setExecutable(true)) {
            throw new IOException("Cannot make " + destBinary + " executable");
        }
    }

    /**
     * Extracts the expected binary from the given zip file to the destination path.
     *
     * @throws IOException if the zip file cannot be read or does not contain the expected binary
     */
    private static void extractFromZip(Path zipFile, Path destBinary, String url) throws IOException {
        String expected = destBinary.getFileName().toString();
        try (ZipFile zip = new ZipFile(zipFile.toFile())) {
            ZipEntry entry = null;
            Enumeration<? extends ZipEntry> entries = zip.entries();
            while (entries.hasMoreElements()) {
                ZipEntry candidate = entries.nextElement();
                if (candidate.isDirectory()) {
                    continue;
                }
                Path fileName = Paths.get(candidate.getName()).getFileName();
                if (fileName != null && expected.equals(fileName.toString())) {
                    entry = candidate;
                    break;
                }
            }
            if (entry == null) {
                throw new IOException("Could not find " + expected + " inside archive " + url);
            }
            try (InputStream in = zip.getInputStream(entry)) {
                Files.copy(in, destBinary, StandardCopyOption.REPLACE_EXISTING);
            }
        }
    }
}
