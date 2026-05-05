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
package org.openhab.binding.homeconnectdirect.internal.service.profile;

import static org.openhab.binding.homeconnectdirect.internal.HomeConnectDirectBindingConstants.BINDING_PROFILES_PATH;
import static org.openhab.binding.homeconnectdirect.internal.HomeConnectDirectBindingConstants.LOCALE;
import static org.openhab.binding.homeconnectdirect.internal.common.utils.StringUtils.endsWith;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.homeconnectdirect.internal.common.json.adapter.OffsetDateTimeAdapter;
import org.openhab.binding.homeconnectdirect.internal.service.profile.json.adapter.ApplianceProfileAdapter;
import org.openhab.binding.homeconnectdirect.internal.service.profile.model.ApplianceProfile;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ServiceScope;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParseException;
import com.google.gson.ToNumberPolicy;

/**
 *
 * Home Connect Direct appliance profile service.
 *
 * @author Jonas Brüstel - Initial contribution
 */
@NonNullByDefault
@Component(service = ApplianceProfileService.class, scope = ServiceScope.SINGLETON, immediate = true)
public class ApplianceProfileService {

    private static final String JSON_EXTENSION = ".json";
    private static final String XML_EXTENSION = ".xml";

    private final Logger logger;
    private final Gson gson;
    private final String profileDirectory;

    @Activate
    public ApplianceProfileService() {
        this(BINDING_PROFILES_PATH);
    }

    public ApplianceProfileService(String profileDirectory) {
        this.logger = LoggerFactory.getLogger(ApplianceProfileService.class);
        this.gson = new GsonBuilder().registerTypeAdapter(OffsetDateTime.class, new OffsetDateTimeAdapter())
                .registerTypeAdapter(ApplianceProfile.class, new ApplianceProfileAdapter())
                .setObjectToNumberStrategy(ToNumberPolicy.LONG_OR_DOUBLE).create();
        this.profileDirectory = profileDirectory;

        createProfileDirectory();
    }

    public List<ApplianceProfile> getProfiles() {
        var profiles = new ArrayList<ApplianceProfile>();
        try {
            File directory = new File(profileDirectory);
            File[] jsonFiles = directory.listFiles((dir, name) -> name.toLowerCase(LOCALE).endsWith(JSON_EXTENSION));

            if (jsonFiles != null) {
                for (File jsonFile : jsonFiles) {
                    profiles.add(
                            gson.fromJson(new FileReader(jsonFile, StandardCharsets.UTF_8), ApplianceProfile.class));
                }
            }
        } catch (SecurityException | JsonParseException | IOException e) {
            logger.error("Could not read profile files! error={}", e.getMessage());
        }

        return profiles;
    }

    public @Nullable ApplianceProfile getProfile(String haId) {
        return getProfiles().stream().filter(applianceProfile -> Objects.equals(applianceProfile.haId(), haId))
                .findFirst().orElse(null);
    }

    public void deleteProfile(String haId) {
        try {
            File directory = new File(profileDirectory);
            File[] jsonFiles = directory.listFiles((dir, name) -> name.toLowerCase(LOCALE).endsWith(JSON_EXTENSION));

            if (jsonFiles != null) {
                for (File jsonFile : jsonFiles) {
                    var profile = gson.fromJson(new FileReader(jsonFile, StandardCharsets.UTF_8),
                            ApplianceProfile.class);
                    if (Objects.equals(profile.haId(), haId)) {
                        Files.delete(safePath(profileDirectory, profile.featureMappingFileName()));
                        Files.delete(safePath(profileDirectory, profile.deviceDescriptionFileName()));
                        Files.delete(safePath(profileDirectory, jsonFile.getName()));
                    }
                }
            }
        } catch (SecurityException | IOException | JsonParseException e) {
            logger.error("Could not delete profile files! error={}", e.getMessage());
        }
    }

    public Optional<ApplianceProfile> uploadProfileZip(InputStream inputStream) {
        Path profilePath = null;
        List<Path> extractedFiles = new ArrayList<>();

        try (ZipInputStream zipInputStream = new ZipInputStream(inputStream)) {
            ZipEntry entry;
            while ((entry = zipInputStream.getNextEntry()) != null) {
                if (!entry.isDirectory()
                        && (endsWith(entry.getName(), JSON_EXTENSION) || endsWith(entry.getName(), XML_EXTENSION))) {
                    var path = safePath(profileDirectory, entry.getName());
                    Files.copy(zipInputStream, path, StandardCopyOption.REPLACE_EXISTING);
                    extractedFiles.add(path);

                    if (endsWith(entry.getName(), JSON_EXTENSION)) {
                        profilePath = path;
                    }
                }
            }

            if (profilePath != null) {
                try (FileReader reader = new FileReader(profilePath.toFile(), StandardCharsets.UTF_8)) {
                    return Optional.of(gson.fromJson(reader, ApplianceProfile.class));
                }
            }
        } catch (IOException e) {
            logger.debug("Could not save profile! error={}", e.getMessage());
        }

        // Rollback extracted files on failure
        for (Path file : extractedFiles) {
            try {
                Files.deleteIfExists(file);
            } catch (IOException e) {
                logger.debug("Could not delete extracted file! path={}", file, e);
            }
        }

        return Optional.empty();
    }

    public void downloadProfileZip(String haId, OutputStream outputStream) {
        var profile = getProfile(haId);
        if (profile == null) {
            return;
        }

        var profileJsonContent = gson.toJson(profile);
        try (ZipOutputStream zos = new ZipOutputStream(outputStream, StandardCharsets.UTF_8);
                OutputStreamWriter writer = new OutputStreamWriter(zos, StandardCharsets.UTF_8)) {
            // json
            ZipEntry zipEntry = new ZipEntry(haId + JSON_EXTENSION);
            zos.putNextEntry(zipEntry);
            writer.write(profileJsonContent);
            writer.flush();
            zos.closeEntry();

            // original XMLs
            for (Path path : List.of(safePath(profileDirectory, profile.deviceDescriptionFileName()),
                    safePath(profileDirectory, profile.featureMappingFileName()))) {
                if (Files.exists(path)) {
                    ZipEntry fileEntry = new ZipEntry(path.getFileName().toString());
                    zos.putNextEntry(fileEntry);
                    Files.copy(path, zos);
                    zos.closeEntry();
                } else {
                    logger.warn("Profile file {} does not exist!", profile.deviceDescriptionFileName());
                }
            }
        } catch (IOException ignored) {
            // ignore exception
        }
    }

    private Path safePath(String directory, String fileName) throws IOException {
        Path resolved = Path.of(directory, fileName).normalize();
        if (!resolved.startsWith(Path.of(directory).normalize())) {
            throw new IOException("Path traversal attempt detected: " + fileName);
        }
        return resolved;
    }

    private void createProfileDirectory() {
        try {
            Files.createDirectories(Paths.get(profileDirectory));
        } catch (IOException e) {
            logger.error("Could not create profile directory! directory={}", profileDirectory, e);
        }
    }
}
