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
package org.openhab.binding.irobot.internal.dto;

import java.io.IOException;
import java.io.StringReader;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.nio.charset.StandardCharsets;

import com.google.gson.Gson;
import com.google.gson.JsonParseException;
import com.google.gson.stream.JsonReader;

/**
 * iRobot discovery and identification protocol
 *
 * @author Pavel Fedin - Initial contribution
 *
 */
public class IdentProtocol {
    private static final String UDP_PACKET_CONTENTS = "irobotmcs";
    private static final int REMOTE_UDP_PORT = 5678;
    private static final Gson gson = new Gson();

    public static DatagramSocket sendRequest(InetAddress host) throws IOException {
        DatagramSocket socket = new DatagramSocket();

        socket.setBroadcast(true);
        socket.setReuseAddress(true);

        byte[] packetContents = UDP_PACKET_CONTENTS.getBytes(StandardCharsets.UTF_8);
        DatagramPacket packet = new DatagramPacket(packetContents, packetContents.length, host, REMOTE_UDP_PORT);

        socket.send(packet);
        return socket;
    }

    public static DatagramPacket receiveResponse(DatagramSocket socket) throws IOException {
        byte[] buffer = new byte[1024];
        DatagramPacket incomingPacket = new DatagramPacket(buffer, buffer.length);

        socket.setSoTimeout(1000 /* one second */);
        socket.receive(incomingPacket);

        return incomingPacket;
    }

    public static IdentData decodeResponse(DatagramPacket packet) throws JsonParseException {
        return decodeResponse(new String(packet.getData(), StandardCharsets.UTF_8));
    }

    public static IdentData decodeResponse(String reply) throws JsonParseException {
        /*
         * packet is a JSON of the following contents (addresses are undisclosed):
         * @formatter:off
         * {
         *   "ver":"3",
         *   "hostname":"Roomba-3168820480607740",
         *   "robotname":"Roomba",
         *   "ip":"XXX.XXX.XXX.XXX",
         *   "mac":"XX:XX:XX:XX:XX:XX",
         *   "sw":"v2.4.6-3",
         *   "sku":"R981040",
         *   "nc":0,
         *   "proto":"mqtt",
         *   "cap":{
         *     "pose":1,
         *     "ota":2,
         *     "multiPass":2,
         *     "carpetBoost":1,
         *     "pp":1,
         *     "binFullDetect":1,
         *     "langOta":1,
         *     "maps":1,
         *     "edge":1,
         *     "eco":1,
         *     "svcConf":1
         *   }
         * }
         * @formatter:on
         */
        // We are not consuming all the fields, so we have to create the reader explicitly
        // If we use fromJson(String) or fromJson(java.util.reader), it will throw
        // "JSON not fully consumed" exception, because not all the reader's content has been
        // used up. We want to avoid that for compatibility reasons because newer iRobot versions
        // may add fields.
        JsonReader jsonReader = new JsonReader(new StringReader(reply));
        IdentData data = gson.fromJson(jsonReader, IdentData.class);

        data.postParse();
        return data;
    }

    public static class IdentData {
        public static int MIN_SUPPORTED_VERSION = 2;
        public static String PRODUCT_ROOMBA = "Roomba";

        public int ver;
        private String hostname;
        public String robotname;

        // These two fields are synthetic, they are not contained in JSON
        public String product;
        public String blid;

        public void postParse() {
            // Synthesize missing properties.
            String[] hostparts = hostname.split("-");

            // This also comes from Roomba980-Python. Comments there say that "iRobot"
            // prefix is used by i7. We assume for other robots it would be product
            // name, e. g. "Scooba"
            if (hostparts[0].equals("iRobot")) {
                product = "Roomba";
            } else {
                product = hostparts[0];
            }

            blid = hostparts[1];
        }
    }
}
