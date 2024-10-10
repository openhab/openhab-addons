/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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

import static org.openhab.binding.miio.internal.MiIoBindingConstants.*;

import java.io.File;
import java.io.FileFilter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.junit.jupiter.api.Disabled;
import org.openhab.binding.miio.internal.basic.MiIoBasicChannel;
import org.openhab.binding.miio.internal.basic.MiIoBasicDevice;
import org.openhab.binding.miio.internal.basic.OptionsValueListDTO;
import org.openhab.binding.miio.internal.basic.StateDescriptionDTO;
import org.openhab.core.thing.ThingTypeUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import com.google.gson.stream.JsonReader;

/**
 * Support creation of the miio readme doc
 *
 * Run after adding devices or changing database entries of basic devices
 *
 * Run in IDE with 'run as java application'
 * or run in command line as:
 * mvn exec:java -Dexec.mainClass="org.openhab.binding.miio.internal.ReadmeHelper" -Dexec.classpathScope="test"
 *
 * @author Marcel Verpaalen - Initial contribution
 */
@NonNullByDefault
public class ReadmeHelper {
    private static final Logger LOGGER = LoggerFactory.getLogger(ReadmeHelper.class);
    private static final String BASEFILE = "./README.base.md";
    private static final String OUTPUTFILE = "./README.md";
    private static final String DEVICE_NAMES_FILE = "./src/main/resources/misc/device_names.json";
    private static final String I18N_CHANNEL_FILE = "./src/main/resources/OH-INF/i18n/basic.properties";
    private static final boolean UPDATE_OPTION_MAPPING_README_COMMENTS = true;

    public static final Set<ThingTypeUID> DATABASE_THING_TYPES = Collections
            .unmodifiableSet(Stream.of(MiIoBindingConstants.THING_TYPE_BASIC, MiIoBindingConstants.THING_TYPE_LUMI,
                    MiIoBindingConstants.THING_TYPE_GATEWAY).collect(Collectors.toSet()));

    @Disabled
    public static void main(String[] args) {
        ReadmeHelper rm = new ReadmeHelper();
        LOGGER.info("## Creating device list");
        StringBuilder deviceList = rm.deviceList();
        rm.checkDatabaseEntrys();
        LOGGER.info("## Creating channel list for json database driven devices");
        StringBuilder channelList = rm.channelList();
        LOGGER.info("## Creating Item Files for json database driven devices");
        StringBuilder itemFileExamples = rm.itemFileExamples();
        try {
            String baseDoc = new String(Files.readAllBytes(Paths.get(BASEFILE)), StandardCharsets.UTF_8);
            String newDoc = baseDoc.replaceAll("!!!devices", deviceList.toString())
                    .replaceAll("!!!channelList", channelList.toString())
                    .replaceAll("!!!itemFileExamples", itemFileExamples.toString());
            Files.write(Paths.get(OUTPUTFILE), newDoc.getBytes(StandardCharsets.UTF_8));
        } catch (IOException e) {
            LOGGER.warn("IO exception writing readme", e);
        }

        LOGGER.info("## Creating i18n entries for devices and miio:basic channels");
        StringBuilder sb = new StringBuilder();
        sb.append("# Automatic created list by miio readme maker for miio devices & database channels\n\n");
        sb.append("# Devices\n\n");
        for (MiIoDevices d : Arrays.asList(MiIoDevices.values())) {
            sb.append(I18N_THING_PREFIX);
            sb.append(d.getModel());
            sb.append(" = ");
            sb.append(d.getDescription());
            sb.append("\n");
        }
        sb.append("\n# Channels\n\n");
        for (Entry<String, String> e : sortByKeys(rm.createI18nEntries()).entrySet()) {
            sb.append(e.getKey());
            sb.append(" = ");
            sb.append(e.getValue());
            sb.append("\n");
        }
        sb.append("\n");
        try {
            Files.write(Paths.get(I18N_CHANNEL_FILE), sb.toString().getBytes(StandardCharsets.UTF_8));
        } catch (IOException e) {
            LOGGER.warn("IO exception creating i18n file", e);
        }
        LOGGER.info("## Done");
    }

    private StringBuilder deviceList() {
        long items = Arrays.asList(MiIoDevices.values()).stream()
                .filter(device -> !device.getThingType().equals(MiIoBindingConstants.THING_TYPE_UNSUPPORTED)).count();
        String devicesCount = String.format("Currently the miio binding supports more than %d different models.",
                (items / 10) * 10);
        LOGGER.info(devicesCount);
        StringBuilder sw = new StringBuilder();
        sw.append(devicesCount);
        sw.append("\n\n");
        sw.append(
                "| Device                             | ThingType        | Device Model           | Supported    | Remark     |\n");
        sw.append(
                "|------------------------------------|------------------|------------------------|--------------|------------|\n");

        Arrays.asList(MiIoDevices.values()).forEach(device -> {
            if (!"unknown".equals(device.getModel())) {
                String link = device.getThingType().equals(MiIoBindingConstants.THING_TYPE_VACUUM)
                        ? "robo-rock-vacuum-channels"
                        : device.getModel().replace(".", "-");
                boolean isSupported = device.getThingType().equals(MiIoBindingConstants.THING_TYPE_UNSUPPORTED);
                Boolean experimental = false;
                String remark = "";
                if (DATABASE_THING_TYPES.contains(device.getThingType())) {
                    MiIoBasicDevice dev = findDatabaseEntry(device.getModel());
                    if (dev != null) {
                        remark = dev.getDevice().getReadmeComment();
                        final Boolean experimentalDev = dev.getDevice().getExperimental();
                        experimental = experimentalDev != null && experimentalDev.booleanValue();
                        if (experimental) {
                            remark += (remark.isBlank() ? "" : "<br />")
                                    + "Experimental support. Please report back if all channels are functional. Preferably share the debug log of property refresh and command responses";
                        }
                    }
                }
                sw.append("| ");
                sw.append(minLengthString(device.getDescription(), 34));
                sw.append(" | ");
                sw.append(minLengthString(device.getThingType().toString(), 16));
                sw.append(" | ");
                String model = isSupported ? device.getModel() : "[" + device.getModel() + "](#" + link + ")";
                sw.append(minLengthString(model, 22));
                sw.append(" | ");
                sw.append(isSupported ? "No          " : (experimental ? "Experimental" : "Yes         "));
                sw.append(" | ");
                sw.append(minLengthString(remark, 10));
                sw.append(" |\n");
            }
        });
        return sw;
    }

    private StringBuilder channelList() {
        StringBuilder sw = new StringBuilder();
        Arrays.asList(MiIoDevices.values()).forEach(device -> {
            if (DATABASE_THING_TYPES.contains(device.getThingType())) {
                MiIoBasicDevice dev = findDatabaseEntry(device.getModel());
                if (dev != null) {
                    String link = device.getModel().replace(".", "-");
                    sw.append("### " + device.getDescription() + " (" + "<a name=\"" + link + "\">" + device.getModel()
                            + "</a>" + ") Channels\n" + "\n");
                    sw.append(
                            "| Channel                    | Type                 | Description                              | Comment    |\n");
                    sw.append(
                            "|----------------------------|----------------------|------------------------------------------|------------|\n");

                    for (MiIoBasicChannel ch : dev.getDevice().getChannels()) {
                        if (UPDATE_OPTION_MAPPING_README_COMMENTS
                                && ch.getReadmeComment().startsWith("Value mapping")) {
                            ch.setReadmeComment(readmeOptionMapping(ch, device.getModel()));
                        }
                        sw.append("| " + minLengthString(ch.getChannel(), 26) + " | "
                                + minLengthString(ch.getType(), 20) + " | " + minLengthString(ch.getFriendlyName(), 40)
                                + " | " + minLengthString(ch.getReadmeComment(), 10) + " |\n");
                    }
                    sw.append("\n");
                } else {
                    LOGGER.info("Pls check: Device not found in db: {}", device);
                }
            }
        });

        // Remove excess newline
        if (sw.length() > 1) {
            sw.setLength(sw.length() - 2);
        }
        return sw;
    }

    public static String readmeOptionMapping(MiIoBasicChannel channel, String model) {
        final List<OptionsValueListDTO> options = getChannelOptions(channel);
        if (!options.isEmpty()) {
            StringBuilder mapping = new StringBuilder();
            mapping.append("Value mapping `[");
            options.forEach((option) -> {
                mapping.append(
                        String.format("\"%s\"=\"%s\",", String.valueOf(option.value), String.valueOf(option.label)));
            });
            mapping.deleteCharAt(mapping.length() - 1);
            mapping.append("]`");
            String newComment = mapping.toString();
            if (!channel.getReadmeComment().contentEquals(newComment)) {
                LOGGER.info("Channel {} - {} readme comment updated to '{}'", model, channel.getChannel(), newComment);
            }
            return newComment;
        }
        return channel.getReadmeComment();
    }

    private StringBuilder itemFileExamples() {
        StringBuilder sw = new StringBuilder();
        Arrays.asList(MiIoDevices.values()).forEach(device -> {
            if (DATABASE_THING_TYPES.contains(device.getThingType())) {
                MiIoBasicDevice dev = findDatabaseEntry(device.getModel());
                if (dev != null) {
                    sw.append("### " + device.getDescription() + " (" + device.getModel() + ") item file lines\n\n");
                    String[] ids = device.getModel().split("\\.");
                    String id = ids[ids.length - 2];
                    String gr = "G_" + id;
                    sw.append("note: Autogenerated example. Replace the id (" + id
                            + ") in the channel with your own. Replace `basic` with `generic` in the thing UID depending on how your thing was discovered.\n");
                    sw.append("\n```java\n");
                    sw.append("Group " + gr + " \"" + device.getDescription() + "\" <status>\n");

                    for (MiIoBasicChannel ch : dev.getDevice().getChannels()) {
                        sw.append(ch.getType() + " " + ch.getChannel().replace("-", "_") + " \"" + ch.getFriendlyName()
                                + "\" (" + gr + ") {channel=\"" + device.getThingType().toString() + ":" + id + ":"
                                + ch.getChannel() + "\"}\n");
                    }
                    sw.append("```\n\n");
                }
            }
        });

        // Remove excess newline
        if (sw.length() > 0) {
            sw.setLength(sw.length() - 1);
        }
        return sw;
    }

    private void checkDatabaseEntrys() {
        StringBuilder sb = new StringBuilder();
        StringBuilder commentSb = new StringBuilder("Adding support for the following models:\r\n");
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        HashMap<String, String> names = new HashMap<>();
        try {
            JsonReader reader = new JsonReader(new FileReader(DEVICE_NAMES_FILE));
            names = gson.fromJson(reader, names.getClass());
        } catch (JsonSyntaxException | IOException e) {
            LOGGER.info("Error reading name list {}: ", DEVICE_NAMES_FILE, e.getMessage());
        }

        for (MiIoBasicDevice entry : findDatabaseEntrys()) {
            for (String id : entry.getDevice().getId()) {
                if (!DATABASE_THING_TYPES.contains(MiIoDevices.getType(id).getThingType())) {
                    commentSb.append("* ");
                    commentSb.append(names.get(id));
                    commentSb.append(" (modelId: ");
                    commentSb.append(id);
                    commentSb.append(")\r\n");
                    sb.append(id.toUpperCase().replace(".", "_"));
                    sb.append("(\"");
                    sb.append(id);
                    sb.append("\",\"");
                    if (names.containsKey(id)) {
                        sb.append(names.get(id));
                        LOGGER.info("id: {} not found in MiIoDevices.java.", id);
                    } else {
                        sb.append(id);
                        LOGGER.info(
                                "id: {} not found in MiIoDevices.java and name unavilable in the device names list.",
                                id);
                    }
                    sb.append("\", ");
                    sb.append(id.startsWith("lumi.")
                            ? (id.startsWith("lumi.gateway") ? "THING_TYPE_GATEWAY" : "THING_TYPE_LUMI")
                            : "THING_TYPE_BASIC");
                    sb.append("),\r\n");
                }
            }
        }
        if (sb.length() > 0) {
            LOGGER.info("Model(s) not found. Suggested lines to add to MiIoDevices.java\r\n{}", sb);
            LOGGER.info("Model(s) not found. Suggested lines to add to the change log\r\n{}", commentSb);
        }
    }

    @Nullable
    private MiIoBasicDevice findDatabaseEntry(String deviceName) {
        for (MiIoBasicDevice entry : findDatabaseEntrys()) {
            for (String id : entry.getDevice().getId()) {
                if (deviceName.equals(id)) {
                    return entry;
                }
            }
        }
        return null;
    }

    private List<MiIoBasicDevice> findDatabaseEntrys() {
        List<MiIoBasicDevice> arrayList = new ArrayList<>();
        String path = "./src/main/resources/database/";
        File dir = new File(path);
        FileFilter fileFilter = file -> !file.isDirectory() && file.getName().toLowerCase().endsWith(".json");
        File[] filesList = dir.listFiles(fileFilter);
        if (filesList == null) {
            return arrayList;
        }
        for (File file : filesList) {
            try {
                JsonObject deviceMapping = convertFileToJSON(path + file.getName());
                Gson gson = new GsonBuilder().serializeNulls().create();
                @Nullable
                MiIoBasicDevice devdb = gson.fromJson(deviceMapping, MiIoBasicDevice.class);
                if (devdb != null) {
                    arrayList.add(devdb);
                }
            } catch (Exception e) {
                LOGGER.info("Error while searching  in database '{}': {}", file.getName(), e.getMessage());
            }
        }
        return arrayList;
    }

    public static List<OptionsValueListDTO> getChannelOptions(MiIoBasicChannel channel) {
        StateDescriptionDTO state = channel.getStateDescription();
        if (state != null) {
            List<OptionsValueListDTO> options = state.getOptions();
            if (options != null) {
                return options;
            }
        }
        return List.of();
    }

    private Map<String, String> createI18nEntries() {
        Map<String, String> i18nEntries = new HashMap<>();
        String path = "./src/main/resources/database/";
        File dir = new File(path);
        FileFilter fileFilter = file -> !file.isDirectory() && file.getName().toLowerCase().endsWith(".json");
        File[] filesList = dir.listFiles(fileFilter);
        if (filesList == null) {
            return i18nEntries;
        }
        for (File file : filesList) {
            try {
                String key = file.getName().toLowerCase().split("json")[0];
                JsonObject deviceMapping = convertFileToJSON(path + file.getName());
                Gson gson = new GsonBuilder().serializeNulls().create();
                @Nullable
                MiIoBasicDevice devdb = gson.fromJson(deviceMapping, MiIoBasicDevice.class);
                if (devdb == null) {
                    continue;
                }
                for (MiIoBasicChannel channel : devdb.getDevice().getChannels()) {
                    i18nEntries.put(I18N_CHANNEL_PREFIX + key + channel.getChannel(), channel.getFriendlyName());
                    List<OptionsValueListDTO> options = getChannelOptions(channel);
                    for (OptionsValueListDTO channelOption : options) {
                        String optionValue = channelOption.value;
                        String optionLabel = channelOption.label;
                        if (optionValue != null && optionLabel != null) {
                            i18nEntries.put(I18N_OPTION_PREFIX + key + channel.getChannel() + "-" + optionValue,
                                    optionLabel);
                        }
                    }
                }
            } catch (Exception e) {
                LOGGER.info("Error while searching  in database '{}': {}", file.getName(), e.getMessage());
            }
        }
        return i18nEntries;
    }

    public static <K extends Comparable<?>, V> Map<K, V> sortByKeys(Map<K, V> map) {
        return new TreeMap<>(map);
    }

    private static String minLengthString(String string, int length) {
        return String.format("%-" + length + "s", string);
    }

    JsonObject convertFileToJSON(String fileName) {
        // Read from File to String
        JsonObject jsonObject = new JsonObject();
        try {
            JsonElement jsonElement = JsonParser.parseReader(new FileReader(fileName));
            jsonObject = jsonElement.getAsJsonObject();
        } catch (FileNotFoundException e) {
            //
        }
        return jsonObject;
    }
}
