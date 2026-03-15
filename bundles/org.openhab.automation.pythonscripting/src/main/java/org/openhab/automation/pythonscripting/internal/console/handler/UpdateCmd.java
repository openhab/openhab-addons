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
package org.openhab.automation.pythonscripting.internal.console.handler;

import java.io.IOException;
import java.lang.module.ModuleDescriptor.Version;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.automation.pythonscripting.internal.PythonScriptEngineConfiguration;
import org.openhab.automation.pythonscripting.internal.PythonScriptEngineHelper;
import org.openhab.core.io.console.Console;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

/**
 * Update command implementations
 *
 * @author Holger Hees - Initial contribution
 */
@NonNullByDefault
public class UpdateCmd {
    private static final String UPDATE_RELEASES_URL = "https://api.github.com/repos/openhab/openhab-python/releases";
    private static final String UPDATE_LATEST_URL = "https://api.github.com/repos/openhab/openhab-python/releases/latest";

    private final PythonScriptEngineConfiguration config;
    private final Console console;

    public UpdateCmd(PythonScriptEngineConfiguration config, Console console) {
        this.config = config;
        this.console = console;
    }

    public void updateList() {
        JsonElement rootElement = getReleaseData(UPDATE_RELEASES_URL);
        if (rootElement != null) {
            Version installedVersion = config.getInstalledHelperLibVersion();
            Version providedVersion = config.getProvidedHelperLibVersion();
            console.println("Version             Released            Active");
            console.println("----------------------------------------------");
            if (rootElement.isJsonArray()) {
                JsonArray list = rootElement.getAsJsonArray();
                for (JsonElement element : list.asList()) {
                    String tagName = element.getAsJsonObject().get("tag_name").getAsString();
                    String publishString = element.getAsJsonObject().get("published_at").getAsString();

                    boolean isInstalled = false;
                    try {
                        Version availableVersion = PythonScriptEngineConfiguration.parseHelperLibVersion(tagName);
                        if (availableVersion.equals(installedVersion)) {
                            isInstalled = true;
                        } else if (availableVersion.compareTo(providedVersion) < 0) {
                            continue;
                        }

                        DateTimeFormatter df = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss", Locale.getDefault());
                        OffsetDateTime publishDate = OffsetDateTime.parse(publishString);
                        console.println(
                                String.format("%-19s", tagName) + " " + String.format("%-19s", df.format(publishDate))
                                        + " " + String.format("%-6s", (isInstalled ? "*" : "  ")));
                    } catch (IllegalArgumentException e) {
                        // ignore not parseable version
                    }
                }
            } else {
                console.println("Fetching releases failed. Invalid data");
            }
        }
    }

    public void updateCheck() {
        Version installedVersion = config.getInstalledHelperLibVersion();
        if (installedVersion == null) {
            console.println("Helper libs disabled. Skipping update.");
        } else {
            JsonElement rootElement = getReleaseData(UPDATE_LATEST_URL);
            if (rootElement != null) {
                JsonElement tagName = rootElement.getAsJsonObject().get("tag_name");
                Version latestVersion = PythonScriptEngineConfiguration.parseHelperLibVersion(tagName.getAsString());
                if (latestVersion.compareTo(installedVersion) > 0) {
                    console.println("Update from version '" + installedVersion + "' to version '"
                            + latestVersion.toString() + "' available.");
                } else {
                    console.println("Latest version '" + installedVersion + "' already installed.");
                }
            }
        }
    }

    public void updateInstall(String requestedVersionString) {
        JsonObject releaseObj = null;
        Version releaseVersion = null;
        if ("latest".equals(requestedVersionString)) {
            JsonElement rootElement = getReleaseData(UPDATE_LATEST_URL);
            if (rootElement != null) {
                releaseObj = rootElement.getAsJsonObject();//
                JsonElement tagName = releaseObj.get("tag_name");
                releaseVersion = PythonScriptEngineConfiguration.parseHelperLibVersion(tagName.getAsString());
            }
        } else {
            try {
                Version requestedVersion = PythonScriptEngineConfiguration
                        .parseHelperLibVersion(requestedVersionString);
                JsonElement rootElement = getReleaseData(UPDATE_RELEASES_URL);
                if (rootElement != null) {
                    if (rootElement.isJsonArray()) {
                        JsonArray list = rootElement.getAsJsonArray();
                        for (JsonElement element : list.asList()) {
                            JsonElement tagName = element.getAsJsonObject().get("tag_name");
                            try {
                                releaseVersion = PythonScriptEngineConfiguration
                                        .parseHelperLibVersion(tagName.getAsString());
                            } catch (IllegalArgumentException e) {
                                continue;
                            }
                            if (releaseVersion.compareTo(requestedVersion) == 0) {
                                releaseObj = element.getAsJsonObject();
                                break;
                            }
                        }
                    }
                }
            } catch (IllegalArgumentException e) {
                // continue, if no version was found
            }
        }

        if (releaseObj != null && releaseVersion != null) {
            Version installedVersion = config.getInstalledHelperLibVersion();
            Version providedVersion = config.getProvidedHelperLibVersion();
            if (releaseVersion.compareTo(providedVersion) < 0) {
                console.println("Outdated version '" + releaseVersion.toString() + "' not supported");
                releaseVersion = null;
            } else if (installedVersion != null) {
                if (releaseVersion.compareTo(installedVersion) < 0) {
                    // Don't install older version, if 'latest' was requested
                    if ("latest".equals(requestedVersionString)) {
                        console.println("Newer Version '" + installedVersion.toString() + "' already installed");
                        releaseVersion = null;
                    }
                } else if (releaseVersion.equals(installedVersion)) {
                    console.println("Version '" + releaseVersion.toString() + "' already installed");
                    releaseVersion = null;
                }
            }

            if (releaseVersion != null) {
                String zipballUrl = releaseObj.get("zipball_url").getAsString();

                try {
                    PythonScriptEngineHelper.installHelperLib(zipballUrl, releaseVersion, config);
                    console.println("Version '" + releaseVersion.toString() + "' installed successfully");
                } catch (URISyntaxException | IOException e) {
                    console.println("Fetching release zip '" + zipballUrl + "' file failed. ");
                    throw new IllegalArgumentException(e);
                }
            }
        } else {
            console.println("Version '" + requestedVersionString + "' not found. ");
        }
    }

    private @Nullable JsonElement getReleaseData(String url) {
        try {
            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder().uri(URI.create(url))
                    .header("Accept", "application/vnd.github+json").GET().build();

            HttpResponse<String> response = client.send(request, BodyHandlers.ofString());
            if (response.statusCode() == 200) {
                JsonElement obj = JsonParser.parseString(response.body());
                return obj;
            } else {
                console.println("Fetching releases failed. Status code is " + response.statusCode());
            }

        } catch (IOException | InterruptedException e) {
            console.println("Fetching releases failed. Request interrupted " + e.getLocalizedMessage());
        }
        return null;
    }
}
