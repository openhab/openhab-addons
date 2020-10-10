/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.junit.jupiter.api.Disabled;
import org.openhab.binding.miio.internal.basic.MiIoBasicChannel;
import org.openhab.binding.miio.internal.basic.MiIoBasicDevice;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

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

    @Disabled
    public static void main(String[] args) {
        ReadmeHelper rm = new ReadmeHelper();
        LOGGER.info("## Creating device list");
        StringWriter deviceList = rm.deviceList();
        rm.checkDatabaseEntrys();
        LOGGER.info("## Creating channel list for basic devices");
        StringWriter channelList = rm.channelList();
        LOGGER.info("## Creating Item Files for miio:basic devices");
        StringWriter itemFileExamples = rm.itemFileExamples();
        LOGGER.info("## Done");
        try {
            String baseDoc = new String(Files.readAllBytes(Paths.get(BASEFILE)), StandardCharsets.UTF_8);
            String newDoc = baseDoc.replaceAll("!!!devices", deviceList.toString())
                    .replaceAll("!!!channelList", channelList.toString())
                    .replaceAll("!!!itemFileExamples", itemFileExamples.toString());
            Files.write(Paths.get(OUTPUTFILE), newDoc.getBytes(StandardCharsets.UTF_8));
        } catch (IOException e) {
            LOGGER.warn("IO exception", e);
        }
    }

    private StringWriter deviceList() {
        StringWriter sw = new StringWriter();

        sw.write(
                "| Device                       | ThingType        | Device Model           | Supported | Remark     |\r\n");
        sw.write(
                "|------------------------------|------------------|------------------------|-----------|------------|\r\n");

        Arrays.asList(MiIoDevices.values()).forEach(device -> {
            if (!device.getModel().equals("unknown")) {
                String link = device.getModel().replace(".", "-");
                boolean isSupported = device.getThingType().equals(MiIoBindingConstants.THING_TYPE_UNSUPPORTED);
                String remark = "";
                if (device.getThingType().equals(MiIoBindingConstants.THING_TYPE_BASIC)) {
                    MiIoBasicDevice dev = findDatabaseEntry(device.getModel());
                    if (dev != null) {
                        remark = dev.getDevice().getReadmeComment();
                        final Boolean experimental = dev.getDevice().getExperimental();
                        if (experimental != null && experimental.booleanValue()) {
                            remark += (remark.isBlank() ? "" : " ")
                                    + "Experimental support. Please report back if all channels are functional. Preferably share the debug log of property refresh and command responses";
                        }
                    }
                }
                sw.write("| ");
                sw.write(minLengthString(device.getDescription(), 28));
                sw.write(" | ");
                sw.write(minLengthString(device.getThingType().toString(), 16));
                sw.write(" | ");
                String model = isSupported ? device.getModel() : "[" + device.getModel() + "](#" + link + ")";
                sw.write(minLengthString(model, 22));
                sw.write(" | ");
                sw.write(isSupported ? "No       " : "Yes      ");
                sw.write(" | ");
                sw.write(minLengthString(remark, 10));
                sw.write(" |\r\n");
            }
        });
        return sw;
    }

    private StringWriter channelList() {
        StringWriter sw = new StringWriter();

        Arrays.asList(MiIoDevices.values()).forEach(device -> {
            if (device.getThingType().equals(MiIoBindingConstants.THING_TYPE_BASIC)) {
                MiIoBasicDevice dev = findDatabaseEntry(device.getModel());
                if (dev != null) {
                    String link = device.getModel().replace(".", "-");
                    sw.write("### " + device.getDescription() + " (" + "<a name=\"" + link + "\">" + device.getModel()
                            + "</a>" + ") Channels\r\n" + "\r\n");
                    sw.write("| Channel          | Type    | Description                         | Comment    |\r\n");
                    sw.write("|------------------|---------|-------------------------------------|------------|\r\n");

                    for (MiIoBasicChannel ch : dev.getDevice().getChannels()) {
                        sw.write("| " + minLengthString(ch.getChannel(), 16) + " | " + minLengthString(ch.getType(), 7)
                                + " | " + minLengthString(ch.getFriendlyName(), 35) + " | "
                                + minLengthString(ch.getReadmeComment(), 10) + " |\r\n");
                    }
                    sw.write("\r\n");

                } else {
                    LOGGER.info("Pls check: Device not found in db: {}", device);
                }
            }
        });
        return sw;
    }

    private StringWriter itemFileExamples() {
        StringWriter sw = new StringWriter();
        Arrays.asList(MiIoDevices.values()).forEach(device -> {
            if (device.getThingType().equals(MiIoBindingConstants.THING_TYPE_BASIC)) {
                MiIoBasicDevice dev = findDatabaseEntry(device.getModel());
                if (dev != null) {
                    sw.write("### " + device.getDescription() + " (" + device.getModel() + ") item file lines\r\n\r\n");
                    String[] ids = device.getModel().split("\\.");
                    String id = ids[ids.length - 2];
                    String gr = "G_" + id;
                    sw.write("note: Autogenerated example. Replace the id (" + id
                            + ") in the channel with your own. Replace `basic` with `generic` in the thing UID depending on how your thing was discovered.\r\n");
                    sw.write("\r\n```java\r\n");
                    sw.write("Group " + gr + " \"" + device.getDescription() + "\" <status>\r\n");

                    for (MiIoBasicChannel ch : dev.getDevice().getChannels()) {
                        sw.write(ch.getType() + " " + ch.getChannel() + " \"" + ch.getFriendlyName() + "\" (" + gr
                                + ") {channel=\"miio:basic:" + id + ":" + ch.getChannel() + "\"}\r\n");
                    }
                    sw.write("```\r\n\r\n");
                }
            }
        });
        return sw;
    }

    private void checkDatabaseEntrys() {
        for (MiIoBasicDevice entry : findDatabaseEntrys()) {
            for (String id : entry.getDevice().getId()) {
                if (!MiIoDevices.getType(id).getThingType().equals(MiIoBindingConstants.THING_TYPE_BASIC)) {
                    LOGGER.info("id :" + id + " not found");
                }
            }
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
        File[] filesList = dir.listFiles();
        for (File file : filesList) {
            if (file.isFile()) {
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
        }
        return arrayList;
    }

    private static String minLengthString(String string, int length) {
        return String.format("%-" + length + "s", string);
    }

    JsonObject convertFileToJSON(String fileName) {
        // Read from File to String
        JsonObject jsonObject = new JsonObject();

        try {
            JsonParser parser = new JsonParser();
            JsonElement jsonElement = parser.parse(new FileReader(fileName));
            jsonObject = jsonElement.getAsJsonObject();
        } catch (FileNotFoundException e) {
            //
        }
        return jsonObject;
    }
}
