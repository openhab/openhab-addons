/**
 * Copyright (c) 2010-2021 Contributors to the openHAB project
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
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map.Entry;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.junit.jupiter.api.Disabled;
import org.openhab.binding.miio.internal.basic.MiIoBasicDevice;
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
 * If the digit at the end of the model is omitted, it will try a range of devices
 *
 * @author Marcel Verpaalen - Initial contribution
 *
 */
@NonNullByDefault
public class MiotJsonFileCreator {
    private static final Logger LOGGER = LoggerFactory.getLogger(MiotJsonFileCreator.class);
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    private static final String BASEDIR = "./src/main/resources/db/";
    private static final String FILENAME_EXTENSION = "-miot.json";
    private static final boolean overwriteFile = false;

    @Disabled
    public static void main(String[] args) {

        LinkedHashMap<String, String> checksums = new LinkedHashMap<>();
        LinkedHashSet<String> models = new LinkedHashSet<>();
        models.add("dmaker.fan.1c");
        models.add("dmaker.fan.p15");
        models.add("dreame.vacuum.mc1808");
        models.add("dreame.vacuum.p2008");
        models.add("dreame.vacuum.p2009");
        models.add("dreame.vacuum.p2036");
        models.add("dreame.vacuum.p2041o");
        models.add("dreame.vacuum.p2156o");
        models.add("dreame.vacuum.p2157");
        models.add("fimi.camera.c1");
        models.add("fimi.camera.c1b");
        models.add("hannto.printer.basil");
        models.add("lumi.ctrl_ln1.aq1");
        models.add("lumi.ctrl_ln1.v1");
        models.add("lumi.ctrl_ln2.aq1");
        models.add("lumi.ctrl_ln2.v1");
        models.add("lumi.ctrl_neutral1.v1");
        models.add("lumi.ctrl_neutral2.v1");
        models.add("lumi.curtain.v1");
        models.add("lumi.gateway.aqhm02");
        models.add("lumi.gateway.lmuk01");
        models.add("lumi.gateway.mgl03");
        models.add("lumi.gateway.mieu01");
        models.add("lumi.light.aqcn02");
        models.add("lumi.plug.maus01");
        models.add("lumi.relay.c2acn01");
        models.add("lumi.remote.b186acn01");
        models.add("lumi.remote.b1acn01");
        models.add("lumi.remote.b286acn01");
        models.add("lumi.sen_ill.mgl01");
        models.add("lumi.sensor_86sw1.v1");
        models.add("lumi.sensor_86sw2.v1");
        models.add("lumi.sensor_cube.aqgl01");
        models.add("lumi.sensor_ht.v1");
        models.add("lumi.sensor_magnet.aq2");
        models.add("lumi.sensor_magnet.v2");
        models.add("lumi.sensor_motion.aq2");
        models.add("lumi.sensor_motion.v2");
        models.add("lumi.sensor_switch.aq2");
        models.add("lumi.sensor_switch.aq3");
        models.add("lumi.sensor_switch.v2");
        models.add("lumi.sensor_wleak.aq1");
        models.add("lumi.vibration.aq1");
        models.add("mijia.light.group1");
        models.add("mijia.light.group2");
        models.add("mijia.light.group3");
        models.add("mijia.light.group4");
        models.add("mijia.vacuum.v2");
        models.add("mmgg.feeder.snack");
        models.add("mmgg.feeder.spec");
        models.add("mmgg.pet_waterer.s4");
        models.add("viomi.airp.v3");
        models.add("viomi.vacuum.v13");
        models.add("viomi.vacuum.v18");
        models.add("viomi.vacuum.v19");
        models.add("yeelink.light.ceilb");
        models.add("yeelink.light.ceilc");
        models.add("yeelink.light.colorb");
        models.add("yeelink.light.colorc");
        models.add("yeelink.light.cta");
        models.add("yeelink.light.monoa");
        models.add("yeelink.light.monob");
        models.add("yeelink.switch.sw1");
        models.add("zhimi.airpurifier.mb3");
        models.add("zhimi.airpurifier.mb4");
        models.add("zhimi.airpurifier.vb2");
        models.add("zhimi.airpurifier.za1");
        models.add("zhimi.fan.za5");
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
        for (String model : models) {
            LOGGER.info("Processing: {}", model);
            HttpClient httpClient = null;
            try {
                httpClient = new HttpClient(new SslContextFactory.Client());
                httpClient.setFollowRedirects(false);
                httpClient.start();
                miotParser = MiotParser.parse(model, httpClient);
                LOGGER.info("urn: ", miotParser.getUrn());
                LOGGER.info("{}", miotParser.getUrnData());
                MiIoBasicDevice device = miotParser.getDevice();
                if (device != null) {
                    LOGGER.info("Device: {}", device);
                    String fileName = String.format("%s%s%s", BASEDIR, model, FILENAME_EXTENSION);
                    if (!overwriteFile) {
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
