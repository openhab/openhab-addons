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
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Iterator;
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

/**
 * Supporting tool for creation of the json database files for miot devices
 * *
 * Run in IDE with 'run as java application' or run in command line as:
 * mvn exec:java -Dexec.mainClass="org.openhab.binding.miio.internal.MiotJsonFileCreator" -Dexec.classpathScope="test"
 * -Dexec.args="zhimi.humidifier.ca4"
 *
 * The argument is the model string to create the database file for.
 * If the digit at the end of the model is omitted, it will try a range of devices.
 * Without a version the newest spec is used: the highest released version, or the highest preview or else debug
 * version for a model without released spec. Append a spec version to select another one, e.g.
 * {@code -Dexec.args="viomi.vacuum.v13:1"} (all spec versions, including debug ones, can be selected this way).
 * <p>
 * Actions with more than one input can not be filled from a single channel value, and neither can an action with an
 * input that is not a property of its service. The parser generates them as actions that are never sent, so they are
 * removed from the generated file and reported in the log. They can be added by hand later (constant inputs plus a
 * single {@code $value$} input) or left out deliberately.
 *
 * @author Marcel Verpaalen - Initial contribution
 *
 */
@NonNullByDefault
public class MiotJsonFileCreator {
    private static final Logger LOGGER = LoggerFactory.getLogger(MiotJsonFileCreator.class);
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    private static final String BASEDIR = "./src/main/resources/database/";
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
            Integer version = null;
            int versionPos = modelArg.indexOf(':');
            if (versionPos > 0) {
                model = modelArg.substring(0, versionPos);
                try {
                    version = Integer.valueOf(modelArg.substring(versionPos + 1));
                } catch (NumberFormatException e) {
                    LOGGER.info("Skipping {}: the spec version is not a number", modelArg);
                    continue;
                }
            }
            LOGGER.info("Processing: {} (spec version {})", model, version == null ? "newest" : version);
            HttpClient httpClient = null;
            try {
                httpClient = new HttpClient(new SslContextFactory.Client());
                httpClient.setFollowRedirects(false);
                httpClient.start();
                miotParser = MiotParser.parse(model, version, httpClient);
                LOGGER.info("urn: {}", miotParser.getUrn());
                LOGGER.info("{}", miotParser.getUrnData());
                MiIoBasicDevice device = miotParser.getDevice();
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
                LOGGER.info("Error processing model {}", model, e);
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

    /**
     * Removes the actions that the parser could not give a working definition (several inputs, or an input that is not
     * a property of the service) and logs them. A channel that has no action left is removed as well.
     */
    static void removeUnsupportedActions(String model, MiIoBasicDevice device) {
        for (Iterator<MiIoBasicChannel> channels = device.getDevice().getChannels().iterator(); channels.hasNext();) {
            MiIoBasicChannel channel = channels.next();
            List<MiIoDeviceAction> keep = new ArrayList<>();
            List<String> removedValues = new ArrayList<>();
            for (MiIoDeviceAction action : channel.getActions()) {
                if (action.getparameterType() == CommandParameterType.UNKNOWN) {
                    MiIoDeviceActionCondition condition = action.getCondition();
                    String value = condition == null ? ""
                            : condition.getParameters().getAsJsonArray().get(0).getAsJsonObject().get("matchValue")
                                    .getAsString();
                    removedValues.add(value);
                    String reason = action.getParameters().size() > 1 ? "needs several inputs"
                            : "input is not a property of the service";
                    LOGGER.warn("{}: removed action '{}' siid {} aiid {}: {} {}", model, value, action.getSiid(),
                            action.getAiid(), reason, action.getParameters());
                } else {
                    keep.add(action);
                }
            }
            if (removedValues.isEmpty()) {
                continue;
            }
            if (keep.isEmpty()) {
                LOGGER.warn("{}: removed channel '{}', no sendable action left", model, channel.getChannel());
                channels.remove();
                continue;
            }
            channel.setActions(keep);
            StateDescriptionDTO stateDescription = channel.getStateDescription();
            List<OptionsValueListDTO> currentOptions = stateDescription == null ? null : stateDescription.getOptions();
            if (stateDescription != null && currentOptions != null) {
                List<OptionsValueListDTO> options = new ArrayList<>(currentOptions);
                options.removeIf(option -> removedValues.contains(option.getValue()));
                stateDescription.setOptions(options);
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
