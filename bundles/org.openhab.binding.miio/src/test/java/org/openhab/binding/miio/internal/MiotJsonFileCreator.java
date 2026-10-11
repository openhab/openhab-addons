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
package org.openhab.binding.miio.internal;

import java.io.File;
import java.net.URI;
import java.net.http.HttpClient.Version;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse.BodyHandlers;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map.Entry;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.junit.jupiter.api.Disabled;
import org.openhab.binding.miio.internal.basic.CommandParameterType;
import org.openhab.binding.miio.internal.basic.MiIoBasicChannel;
import org.openhab.binding.miio.internal.basic.MiIoBasicDevice;
import org.openhab.binding.miio.internal.basic.MiIoDeviceAction;
import org.openhab.binding.miio.internal.basic.MiIoDeviceActionCondition;
import org.openhab.binding.miio.internal.basic.OptionsValueListDTO;
import org.openhab.binding.miio.internal.basic.StateDescriptionDTO;
import org.openhab.binding.miio.internal.miot.MiotParseException;
import org.openhab.binding.miio.internal.miot.MiotParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

/**
 * Supporting tool for creation of the json database files for miot devices
 * *
 * Run in IDE with 'run as java application' or run in command line as:
 * mvn exec:java -Dexec.mainClass="org.openhab.binding.miio.internal.MiotJsonFileCreator" -Dexec.classpathScope="test"
 * -Dexec.args="zhimi.humidifier.ca4"
 *
 * The argument is the model string to create the database file for.
 * If the digit at the end of the model is omitted, it will try a range of devices.
 * Without a version the first released spec version is used. Append a spec version to select another one, e.g.
 * {@code -Dexec.args="viomi.vacuum.v13:2"} (all spec versions, including debug ones, can be selected this way).
 * <p>
 * Actions with more than one input can not be filled from a single channel value. The parser generates them as
 * actions that are never sent, so they are removed from the generated file and reported in the log. They can be
 * added by hand later (constant inputs plus a single {@code $value$} input) or left out deliberately.
 *
 * @author Marcel Verpaalen - Initial contribution
 *
 */
@NonNullByDefault
public class MiotJsonFileCreator {
    private static final Logger LOGGER = LoggerFactory.getLogger(MiotJsonFileCreator.class);
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    private static final String BASEDIR = "./src/main/resources/database/";
    private static final String BASE_URL = "https://miot-spec.org/miot-spec-v2/";
    private static final String FILENAME_EXTENSION = "-miot.json";
    private static final boolean OVERWRITE_EXISTING_DATABASE_FILE = false;

    @Disabled
    public static void main(String[] args) {
        LinkedHashMap<String, String> checksums = new LinkedHashMap<>();
        LinkedHashSet<String> models = new LinkedHashSet<>();
        if (args.length > 0) {
            models.add(args[0]);
        }

        String m = models.isEmpty() ? "" : (String) models.toArray()[0];
        boolean scan = m.isEmpty() ? false : !Character.isDigit(m.charAt(m.length() - 1));
        if (scan) {
            for (int i = 1; i <= 12; i++) {
                models.add(models.toArray()[0] + String.valueOf(i));
            }
        }

        MiotParser miotParser;
        for (String modelArg : models) {
            String model = modelArg;
            String version = "";
            int versionPos = modelArg.indexOf(':');
            if (versionPos > 0) {
                model = modelArg.substring(0, versionPos);
                version = modelArg.substring(versionPos + 1);
            }
            LOGGER.info("Processing: {}{}", model, version.isEmpty() ? "" : " (spec version " + version + ")");
            HttpClient httpClient = null;
            try {
                httpClient = new HttpClient(new SslContextFactory.Client());
                httpClient.setFollowRedirects(false);
                httpClient.start();
                MiIoBasicDevice device;
                if (version.isEmpty()) {
                    miotParser = MiotParser.parse(model, httpClient);
                    LOGGER.info("urn: {}", miotParser.getUrn());
                    LOGGER.info("{}", miotParser.getUrnData());
                    device = miotParser.getDevice();
                } else {
                    miotParser = new MiotParser(model);
                    device = miotParser.getDevice(getUrnData(model, version));
                }
                if (device != null) {
                    removeUnsupportedActions(model, device);
                    LOGGER.info("Device: {}", device);
                    String fileName = String.format("%s%s%s", BASEDIR, model, FILENAME_EXTENSION);
                    if (!OVERWRITE_EXISTING_DATABASE_FILE) {
                        int counter = 0;
                        while (new File(fileName).isFile()) {
                            fileName = String.format("%s%s-%d%s", BASEDIR, model, counter, FILENAME_EXTENSION);
                            counter++;
                        }
                    }
                    miotParser.writeDevice(fileName, device);
                    String channelsJson = GSON.toJson(device.getDevice().getChannels()).toString();
                    checksums.put(model, checksumMD5(channelsJson));
                }
                LOGGER.info("Finished");
            } catch (MiotParseException e) {
                LOGGER.info("Error processing model {}: {}", model, e.getMessage());
            } catch (Exception e) {
                LOGGER.info("Failed to initiate http Client: {}", e.getMessage());
            } finally {
                try {
                    if (httpClient != null && httpClient.isRunning()) {
                        httpClient.stop();
                    }
                } catch (Exception e) {
                    // ignore
                }
            }
        }
        StringBuilder sb = new StringBuilder();
        for (Entry<String, String> ch : checksums.entrySet()) {
            sb.append(ch.getValue());
            sb.append(" --> ");
            sb.append(ch.getKey());
            sb.append("\r\n");
        }
        LOGGER.info("Checksums for device comparisons\r\n{}", sb);
    }

    // the list of all spec versions exceeds the buffer size of the jetty client
    private static JsonElement getUrnData(String model, String version) throws Exception {
        java.net.http.HttpClient client = java.net.http.HttpClient.newBuilder().version(Version.HTTP_1_1).build();
        JsonObject instances = JsonParser.parseString(get(client, BASE_URL + "instances?status=all")).getAsJsonObject();
        for (JsonElement instance : instances.getAsJsonArray("instances")) {
            JsonObject data = instance.getAsJsonObject();
            if (model.equals(data.get("model").getAsString()) && version.equals(data.get("version").getAsString())) {
                String urn = data.get("type").getAsString();
                LOGGER.info("urn: {} (status {})", urn, data.get("status"));
                return JsonParser.parseString(get(client, BASE_URL + "instance?type=" + urn));
            }
        }
        throw new MiotParseException("Model " + model + " with spec version " + version + " not found");
    }

    private static String get(java.net.http.HttpClient client, String url) throws Exception {
        return client.send(HttpRequest.newBuilder(URI.create(url)).GET().build(), BodyHandlers.ofString()).body();
    }

    /**
     * Removes the actions that the parser could not give a working definition (several inputs) from the actions
     * channel and logs them.
     */
    private static void removeUnsupportedActions(String model, MiIoBasicDevice device) {
        for (MiIoBasicChannel channel : device.getDevice().getChannels()) {
            List<MiIoDeviceAction> keep = new ArrayList<>();
            List<String> removedValues = new ArrayList<>();
            for (MiIoDeviceAction action : channel.getActions()) {
                if (action.getparameterType() == CommandParameterType.UNKNOWN) {
                    MiIoDeviceActionCondition condition = action.getCondition();
                    String value = condition == null ? ""
                            : condition.getParameters().getAsJsonArray().get(0).getAsJsonObject().get("matchValue")
                                    .getAsString();
                    removedValues.add(value);
                    LOGGER.warn("{}: removed action '{}' siid {} aiid {}: needs several inputs {}", model, value,
                            action.getSiid(), action.getAiid(), action.getParameters());
                } else {
                    keep.add(action);
                }
            }
            if (!removedValues.isEmpty()) {
                channel.setActions(keep);
                StateDescriptionDTO stateDescription = channel.getStateDescription();
                List<OptionsValueListDTO> currentOptions = stateDescription == null ? null
                        : stateDescription.getOptions();
                if (stateDescription != null && currentOptions != null) {
                    List<OptionsValueListDTO> options = new ArrayList<>(currentOptions);
                    options.removeIf(option -> removedValues.contains(option.getValue()));
                    stateDescription.setOptions(options);
                }
            }
        }
    }

    public static String checksumMD5(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            md.update(input.getBytes());
            return Utils.getHex(md.digest());
        } catch (NoSuchAlgorithmException e) {
            return "No MD5";
        }
    }
}
