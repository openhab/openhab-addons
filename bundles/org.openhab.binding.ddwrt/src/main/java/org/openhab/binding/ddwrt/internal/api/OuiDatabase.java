/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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
package org.openhab.binding.ddwrt.internal.api;

import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * Lightweight OUI (Organizationally Unique Identifier) database for generating
 * synthetic hostnames from MAC address vendor prefixes.
 *
 * Contains the most common consumer device manufacturers found in home networks.
 * The first 3 bytes of a MAC address identify the vendor (OUI prefix).
 *
 * @author Lee Ballard - Initial contribution
 */
@NonNullByDefault
public final class OuiDatabase {

    private OuiDatabase() {
        // utility class
    }

    // OUI prefix (first 3 bytes, lowercase, colon-separated) -> short vendor name
    private static final Map<String, String> OUI_MAP = Map.ofEntries(
            // Amazon
            Map.entry("00:fc:8b", "Amazon"), Map.entry("10:2c:6b", "Amazon"), Map.entry("14:91:82", "Amazon"),
            Map.entry("18:74:2e", "Amazon"), Map.entry("34:d2:70", "Amazon"), Map.entry("38:f7:3d", "Amazon"),
            Map.entry("40:a2:db", "Amazon"), Map.entry("44:65:0d", "Amazon"), Map.entry("4c:ef:c0", "Amazon"),
            Map.entry("50:dc:e7", "Amazon"), Map.entry("68:37:e9", "Amazon"), Map.entry("68:54:fd", "Amazon"),
            Map.entry("74:c2:46", "Amazon"), Map.entry("84:d6:d0", "Amazon"), Map.entry("a4:08:ea", "Amazon"),
            Map.entry("b4:7c:9c", "Amazon"), Map.entry("f0:f0:a4", "Amazon"), Map.entry("fc:65:de", "Amazon"),
            Map.entry("0c:47:c9", "Amazon"),
            // Apple
            Map.entry("00:17:f2", "Apple"), Map.entry("00:1c:b3", "Apple"), Map.entry("00:25:bc", "Apple"),
            Map.entry("04:0c:ce", "Apple"), Map.entry("08:66:98", "Apple"), Map.entry("10:dd:b1", "Apple"),
            Map.entry("14:99:d3", "Apple"), Map.entry("18:af:61", "Apple"), Map.entry("1c:36:bb", "Apple"),
            Map.entry("20:78:f0", "Apple"), Map.entry("28:cf:e9", "Apple"), Map.entry("2c:be:eb", "Apple"),
            Map.entry("34:08:bc", "Apple"), Map.entry("38:53:9c", "Apple"), Map.entry("3c:06:30", "Apple"),
            Map.entry("40:b3:95", "Apple"), Map.entry("44:2a:60", "Apple"), Map.entry("48:a9:1c", "Apple"),
            Map.entry("4c:57:ca", "Apple"), Map.entry("50:ed:3c", "Apple"), Map.entry("54:4e:90", "Apple"),
            Map.entry("58:b0:35", "Apple"), Map.entry("5c:f7:e6", "Apple"), Map.entry("60:03:08", "Apple"),
            Map.entry("64:70:33", "Apple"), Map.entry("68:5b:35", "Apple"), Map.entry("6c:4d:73", "Apple"),
            Map.entry("70:56:81", "Apple"), Map.entry("74:8d:08", "Apple"), Map.entry("78:7e:61", "Apple"),
            Map.entry("7c:d1:c3", "Apple"), Map.entry("80:be:05", "Apple"), Map.entry("84:fc:fe", "Apple"),
            Map.entry("88:e9:fe", "Apple"), Map.entry("8c:85:90", "Apple"), Map.entry("90:8d:6c", "Apple"),
            Map.entry("94:e9:6a", "Apple"), Map.entry("98:01:a7", "Apple"), Map.entry("9c:20:7b", "Apple"),
            Map.entry("a0:99:9b", "Apple"), Map.entry("a4:83:e7", "Apple"), Map.entry("a8:5c:2c", "Apple"),
            Map.entry("ac:bc:32", "Apple"), Map.entry("b0:19:c6", "Apple"), Map.entry("b4:18:d1", "Apple"),
            Map.entry("b8:c1:11", "Apple"), Map.entry("bc:52:b7", "Apple"), Map.entry("c0:b6:58", "Apple"),
            Map.entry("c4:b3:01", "Apple"), Map.entry("c8:69:cd", "Apple"), Map.entry("cc:08:e0", "Apple"),
            Map.entry("d0:03:4b", "Apple"), Map.entry("d4:61:9d", "Apple"), Map.entry("d8:1c:79", "Apple"),
            Map.entry("dc:a4:ca", "Apple"), Map.entry("e0:5f:45", "Apple"), Map.entry("e4:c6:3d", "Apple"),
            Map.entry("e8:06:88", "Apple"), Map.entry("f0:18:98", "Apple"), Map.entry("f4:37:b7", "Apple"),
            Map.entry("f8:ff:c2", "Apple"),
            // Google / Nest
            Map.entry("08:9e:08", "Google"), Map.entry("18:d6:c7", "Google"), Map.entry("1c:f2:9a", "Google"),
            Map.entry("20:df:b9", "Google"), Map.entry("3c:5a:b4", "Google"), Map.entry("48:d6:d5", "Google"),
            Map.entry("54:60:09", "Google"), Map.entry("64:16:66", "Google"), Map.entry("94:eb:2c", "Google"),
            Map.entry("a4:77:33", "Google"), Map.entry("f4:f5:d8", "Google"), Map.entry("f4:f5:e8", "Google"),
            Map.entry("f8:0f:f9", "Google"), Map.entry("d8:eb:46", "Google"),
            // Samsung
            Map.entry("00:07:ab", "Samsung"), Map.entry("00:12:47", "Samsung"), Map.entry("00:16:32", "Samsung"),
            Map.entry("00:1b:98", "Samsung"), Map.entry("08:37:3d", "Samsung"), Map.entry("0c:14:20", "Samsung"),
            Map.entry("10:1d:c0", "Samsung"), Map.entry("14:49:e0", "Samsung"), Map.entry("18:3a:2d", "Samsung"),
            Map.entry("1c:62:b8", "Samsung"), Map.entry("24:18:1d", "Samsung"), Map.entry("28:cc:01", "Samsung"),
            Map.entry("2c:ae:2b", "Samsung"), Map.entry("30:96:fb", "Samsung"), Map.entry("34:23:ba", "Samsung"),
            Map.entry("38:01:46", "Samsung"), Map.entry("3c:a6:2f", "Samsung"), Map.entry("40:4e:36", "Samsung"),
            Map.entry("44:78:3e", "Samsung"), Map.entry("48:21:0b", "Samsung"), Map.entry("4c:3c:16", "Samsung"),
            Map.entry("50:01:bb", "Samsung"), Map.entry("54:40:ad", "Samsung"), Map.entry("58:c3:8b", "Samsung"),
            Map.entry("5c:49:7d", "Samsung"), Map.entry("60:6b:bd", "Samsung"), Map.entry("64:b5:c6", "Samsung"),
            Map.entry("68:27:37", "Samsung"), Map.entry("6c:f3:73", "Samsung"), Map.entry("70:1a:b8", "Samsung"),
            Map.entry("74:45:ce", "Samsung"), Map.entry("78:47:1d", "Samsung"), Map.entry("7c:0a:3f", "Samsung"),
            Map.entry("80:65:6d", "Samsung"), Map.entry("84:25:19", "Samsung"), Map.entry("84:38:38", "Samsung"),
            Map.entry("88:ad:d2", "Samsung"), Map.entry("8c:71:f8", "Samsung"), Map.entry("90:18:7c", "Samsung"),
            Map.entry("94:01:c2", "Samsung"), Map.entry("98:52:3d", "Samsung"), Map.entry("9c:3a:af", "Samsung"),
            Map.entry("a0:82:1f", "Samsung"), Map.entry("a8:7c:01", "Samsung"), Map.entry("ac:5f:3e", "Samsung"),
            Map.entry("b4:79:a7", "Samsung"), Map.entry("bc:14:ef", "Samsung"), Map.entry("c0:97:27", "Samsung"),
            Map.entry("c4:73:1e", "Samsung"), Map.entry("cc:07:ab", "Samsung"), Map.entry("d0:17:6a", "Samsung"),
            Map.entry("d8:90:e8", "Samsung"), Map.entry("e4:7d:bd", "Samsung"), Map.entry("f0:25:b7", "Samsung"),
            Map.entry("f4:7b:09", "Samsung"), Map.entry("fc:a8:9a", "Samsung"),
            // TP-Link / Kasa / Tapo
            Map.entry("14:eb:b6", "TPLink"), Map.entry("18:a6:f7", "TPLink"), Map.entry("1c:3b:f3", "TPLink"),
            Map.entry("30:de:4b", "TPLink"), Map.entry("50:c7:bf", "TPLink"), Map.entry("54:af:97", "TPLink"),
            Map.entry("60:a4:b7", "TPLink"), Map.entry("60:32:b1", "TPLink"), Map.entry("68:ff:7b", "TPLink"),
            Map.entry("6c:5a:b0", "TPLink"), Map.entry("78:8c:b5", "TPLink"), Map.entry("84:d8:1b", "TPLink"),
            Map.entry("98:da:c4", "TPLink"), Map.entry("a0:92:08", "TPLink"), Map.entry("ac:84:c6", "TPLink"),
            Map.entry("b0:4e:26", "TPLink"), Map.entry("b0:95:75", "TPLink"), Map.entry("c0:06:c3", "TPLink"),
            Map.entry("c0:e3:fb", "TPLink"), Map.entry("d8:07:b6", "TPLink"), Map.entry("d8:47:32", "TPLink"),
            Map.entry("e8:48:b8", "TPLink"), Map.entry("e8:fc:af", "TPLink"), Map.entry("f0:2f:9e", "TPLink"),
            Map.entry("f0:a7:31", "TPLink"), Map.entry("b0:19:21", "TPLink"),
            // Roku
            Map.entry("b0:a7:37", "Roku"), Map.entry("b8:3e:59", "Roku"), Map.entry("c8:3a:6b", "Roku"),
            Map.entry("d0:4d:c6", "Roku"), Map.entry("dc:3a:5e", "Roku"), Map.entry("84:ea:ed", "Roku"),
            Map.entry("ac:3a:7a", "Roku"),
            // LG Electronics
            Map.entry("00:1c:62", "LG"), Map.entry("00:1e:75", "LG"), Map.entry("00:aa:70", "LG"),
            Map.entry("10:f1:f2", "LG"), Map.entry("20:3d:bd", "LG"), Map.entry("2c:54:cf", "LG"),
            Map.entry("34:4d:f7", "LG"), Map.entry("38:8c:50", "LG"), Map.entry("58:a2:b5", "LG"),
            Map.entry("64:89:9a", "LG"), Map.entry("74:40:be", "LG"), Map.entry("78:f2:9e", "LG"),
            Map.entry("88:c9:d0", "LG"), Map.entry("a8:23:fe", "LG"), Map.entry("b4:b5:b6", "LG"),
            Map.entry("c4:36:6c", "LG"), Map.entry("cc:2d:8c", "LG"), Map.entry("e8:f2:e2", "LG"),
            // Sony
            Map.entry("00:04:1f", "Sony"), Map.entry("00:13:a9", "Sony"), Map.entry("00:1d:0d", "Sony"),
            Map.entry("00:24:8d", "Sony"), Map.entry("04:5d:4b", "Sony"), Map.entry("24:21:ab", "Sony"),
            Map.entry("2c:33:7a", "Sony"), Map.entry("40:b8:9a", "Sony"), Map.entry("70:2a:d5", "Sony"),
            Map.entry("78:84:3c", "Sony"), Map.entry("a8:e3:ee", "Sony"), Map.entry("ac:9b:0a", "Sony"),
            Map.entry("b8:f9:34", "Sony"), Map.entry("fc:0f:e6", "Sony"),
            // Microsoft / Xbox
            Map.entry("28:18:78", "Xbox"), Map.entry("7c:ed:8d", "Xbox"), Map.entry("98:5f:d3", "Xbox"),
            Map.entry("c8:3d:d4", "Xbox"), Map.entry("60:45:bd", "Xbox"),
            // Nintendo
            Map.entry("00:1b:ea", "Nintendo"), Map.entry("00:1e:35", "Nintendo"), Map.entry("00:22:d7", "Nintendo"),
            Map.entry("00:24:f3", "Nintendo"), Map.entry("00:25:a0", "Nintendo"), Map.entry("04:03:d6", "Nintendo"),
            Map.entry("10:1f:74", "Nintendo"), Map.entry("2c:10:c1", "Nintendo"), Map.entry("34:af:2c", "Nintendo"),
            Map.entry("40:d2:8a", "Nintendo"), Map.entry("58:2f:40", "Nintendo"), Map.entry("7c:bb:8a", "Nintendo"),
            Map.entry("8c:cd:e8", "Nintendo"), Map.entry("98:41:5c", "Nintendo"), Map.entry("98:b6:e9", "Nintendo"),
            Map.entry("d8:6b:f7", "Nintendo"),
            // Sonos
            Map.entry("00:0e:58", "Sonos"), Map.entry("34:7e:5c", "Sonos"), Map.entry("48:a6:b8", "Sonos"),
            Map.entry("5c:aa:fd", "Sonos"), Map.entry("78:28:ca", "Sonos"), Map.entry("94:9f:3e", "Sonos"),
            Map.entry("b8:e9:37", "Sonos"),
            // Wyze
            Map.entry("2c:aa:8e", "Wyze"), Map.entry("d0:3f:27", "Wyze"),
            // Ring
            Map.entry("44:3d:54", "Ring"), Map.entry("34:3e:a4", "Ring"),
            // Ecobee
            Map.entry("44:61:32", "Ecobee"),
            // Chamberlain / MyQ
            Map.entry("00:17:c9", "Chamberlain"),
            // Philips Hue
            Map.entry("00:17:88", "Hue"), Map.entry("ec:b5:fa", "Hue"),
            // LIFX
            Map.entry("d0:73:d5", "LIFX"),
            // Tuya / Smart Life
            Map.entry("10:d5:61", "Tuya"), Map.entry("7c:f6:66", "Tuya"),
            // Espressif (ESP8266/ESP32)
            Map.entry("18:fe:34", "Espressif"), Map.entry("24:0a:c4", "Espressif"), Map.entry("24:62:ab", "Espressif"),
            Map.entry("2c:3a:e8", "Espressif"), Map.entry("30:ae:a4", "Espressif"), Map.entry("3c:61:05", "Espressif"),
            Map.entry("3c:71:bf", "Espressif"), Map.entry("4c:11:ae", "Espressif"), Map.entry("5c:cf:7f", "Espressif"),
            Map.entry("60:01:94", "Espressif"), Map.entry("68:c6:3a", "Espressif"), Map.entry("80:7d:3a", "Espressif"),
            Map.entry("84:0d:8e", "Espressif"), Map.entry("84:cc:a8", "Espressif"), Map.entry("a0:20:a6", "Espressif"),
            Map.entry("a4:cf:12", "Espressif"), Map.entry("a8:03:2a", "Espressif"), Map.entry("ac:67:b2", "Espressif"),
            Map.entry("b4:e6:2d", "Espressif"), Map.entry("bc:dd:c2", "Espressif"), Map.entry("c4:4f:33", "Espressif"),
            Map.entry("c8:c9:a3", "Espressif"), Map.entry("cc:50:e3", "Espressif"), Map.entry("d8:bf:c0", "Espressif"),
            Map.entry("dc:4f:22", "Espressif"), Map.entry("e0:98:06", "Espressif"), Map.entry("e8:db:84", "Espressif"),
            Map.entry("ec:fa:bc", "Espressif"), Map.entry("f0:08:d1", "Espressif"), Map.entry("f4:cf:a2", "Espressif"),
            // Raspberry Pi
            Map.entry("b8:27:eb", "RaspberryPi"), Map.entry("dc:a6:32", "RaspberryPi"),
            Map.entry("e4:5f:01", "RaspberryPi"), Map.entry("d8:3a:dd", "RaspberryPi"),
            // Intel
            Map.entry("00:1e:67", "Intel"), Map.entry("3c:97:0e", "Intel"), Map.entry("68:05:ca", "Intel"),
            Map.entry("8c:8c:aa", "Intel"), Map.entry("a4:34:d9", "Intel"), Map.entry("b4:96:91", "Intel"),
            Map.entry("f8:63:3f", "Intel"),
            // HP / Hewlett-Packard
            Map.entry("00:14:38", "HP"), Map.entry("00:1b:78", "HP"), Map.entry("00:21:5a", "HP"),
            Map.entry("10:60:4b", "HP"), Map.entry("2c:41:38", "HP"), Map.entry("30:e1:71", "HP"),
            Map.entry("3c:d9:2b", "HP"), Map.entry("c8:b5:ad", "HP"),
            // Dell
            Map.entry("00:06:5b", "Dell"), Map.entry("00:14:22", "Dell"), Map.entry("00:1a:a0", "Dell"),
            Map.entry("14:18:77", "Dell"), Map.entry("18:03:73", "Dell"), Map.entry("24:b6:fd", "Dell"),
            Map.entry("34:17:eb", "Dell"), Map.entry("f0:1f:af", "Dell"), Map.entry("b0:8b:a8", "Dell"),
            // Lenovo
            Map.entry("00:06:1b", "Lenovo"), Map.entry("28:d2:44", "Lenovo"), Map.entry("50:7b:9d", "Lenovo"),
            Map.entry("54:e1:ad", "Lenovo"), Map.entry("70:5a:0f", "Lenovo"), Map.entry("98:fa:9b", "Lenovo"),
            Map.entry("c8:21:58", "Lenovo"),
            // Netgear
            Map.entry("00:14:6c", "Netgear"), Map.entry("00:1b:2f", "Netgear"), Map.entry("00:1e:2a", "Netgear"),
            Map.entry("20:0c:c8", "Netgear"), Map.entry("28:c6:8e", "Netgear"), Map.entry("30:46:9a", "Netgear"),
            Map.entry("4c:60:de", "Netgear"), Map.entry("6c:b0:ce", "Netgear"), Map.entry("84:1b:5e", "Netgear"),
            Map.entry("a4:2b:8c", "Netgear"), Map.entry("c0:ff:d4", "Netgear"), Map.entry("e0:91:f5", "Netgear"),
            // Asus
            Map.entry("00:1a:92", "Asus"), Map.entry("04:92:26", "Asus"), Map.entry("10:c3:7b", "Asus"),
            Map.entry("1c:87:2c", "Asus"), Map.entry("2c:4d:54", "Asus"), Map.entry("2c:fd:a1", "Asus"),
            Map.entry("30:5a:3a", "Asus"), Map.entry("3c:7c:3f", "Asus"), Map.entry("50:46:5d", "Asus"),
            Map.entry("60:45:cb", "Asus"), Map.entry("74:d0:2b", "Asus"), Map.entry("90:e6:ba", "Asus"),
            Map.entry("ac:9e:17", "Asus"), Map.entry("f0:79:59", "Asus"),
            // Linksys
            Map.entry("00:06:25", "Linksys"), Map.entry("00:12:17", "Linksys"), Map.entry("00:14:bf", "Linksys"),
            Map.entry("00:18:f8", "Linksys"), Map.entry("20:aa:4b", "Linksys"), Map.entry("c0:56:27", "Linksys"),
            // D-Link
            Map.entry("00:05:5d", "DLink"), Map.entry("00:17:9a", "DLink"), Map.entry("00:1c:f0", "DLink"),
            Map.entry("00:22:b0", "DLink"), Map.entry("1c:7e:e5", "DLink"), Map.entry("28:10:7b", "DLink"),
            Map.entry("34:08:04", "DLink"), Map.entry("60:63:4c", "DLink"), Map.entry("b8:a3:86", "DLink"),
            Map.entry("f0:b4:d2", "DLink"),
            // Ubiquiti
            Map.entry("04:18:d6", "Ubiquiti"), Map.entry("18:e8:29", "Ubiquiti"), Map.entry("24:5a:4c", "Ubiquiti"),
            Map.entry("44:d9:e7", "Ubiquiti"), Map.entry("68:72:51", "Ubiquiti"), Map.entry("74:83:c2", "Ubiquiti"),
            Map.entry("78:8a:20", "Ubiquiti"), Map.entry("80:2a:a8", "Ubiquiti"), Map.entry("b4:fb:e4", "Ubiquiti"),
            Map.entry("f0:9f:c2", "Ubiquiti"), Map.entry("fc:ec:da", "Ubiquiti"),
            // Liteon (often WiFi modules in laptops)
            Map.entry("30:52:cb", "Liteon"), Map.entry("00:26:18", "Liteon"),
            // Qualcomm Atheros (WiFi chipset)
            Map.entry("3c:6a:d2", "Atheros"),
            // Broadcom
            Map.entry("00:10:18", "Broadcom"), Map.entry("d8:b1:22", "Broadcom"),
            // Realtek
            Map.entry("00:e0:4c", "Realtek"), Map.entry("52:54:00", "Realtek"),
            // MediaTek
            Map.entry("00:0c:e7", "MediaTek"),
            // Xiaomi
            Map.entry("04:cf:8c", "Xiaomi"), Map.entry("0c:1d:af", "Xiaomi"), Map.entry("28:6c:07", "Xiaomi"),
            Map.entry("34:ce:00", "Xiaomi"), Map.entry("50:64:2b", "Xiaomi"), Map.entry("58:44:98", "Xiaomi"),
            Map.entry("64:cc:2e", "Xiaomi"), Map.entry("7c:49:eb", "Xiaomi"), Map.entry("8c:de:f9", "Xiaomi"),
            Map.entry("9c:9d:7e", "Xiaomi"), Map.entry("ac:c1:ee", "Xiaomi"), Map.entry("b0:e2:35", "Xiaomi"),
            Map.entry("f0:b4:29", "Xiaomi"), Map.entry("fc:64:ba", "Xiaomi"),
            // Huawei
            Map.entry("00:1e:10", "Huawei"), Map.entry("00:25:68", "Huawei"), Map.entry("04:c0:6f", "Huawei"),
            Map.entry("10:44:00", "Huawei"), Map.entry("20:a6:cd", "Huawei"), Map.entry("24:09:95", "Huawei"),
            Map.entry("28:3c:e4", "Huawei"), Map.entry("34:00:a3", "Huawei"), Map.entry("40:4d:8e", "Huawei"),
            Map.entry("4c:b1:6c", "Huawei"), Map.entry("58:60:5f", "Huawei"), Map.entry("60:de:44", "Huawei"),
            Map.entry("70:72:3c", "Huawei"), Map.entry("80:b6:86", "Huawei"), Map.entry("88:28:b3", "Huawei"),
            Map.entry("c0:70:09", "Huawei"), Map.entry("cc:a2:23", "Huawei"),
            // OnePlus / BBK
            Map.entry("40:ed:00", "OnePlus"), Map.entry("94:65:2d", "OnePlus"),
            // Motorola
            Map.entry("5c:a6:e6", "Motorola"), Map.entry("00:0b:06", "Motorola"), Map.entry("10:68:3f", "Motorola"),
            Map.entry("e4:90:7e", "Motorola"),
            // iRobot (Roomba)
            Map.entry("50:14:79", "iRobot"),
            // Honeywell / Resideo
            Map.entry("00:d0:2d", "Honeywell"), Map.entry("08:3a:f2", "Honeywell"),
            // Belkin / Wemo
            Map.entry("08:86:3b", "Wemo"), Map.entry("94:10:3e", "Wemo"), Map.entry("b4:75:0e", "Wemo"),
            Map.entry("c4:12:f5", "Wemo"), Map.entry("ec:1a:59", "Wemo"),
            // Bose
            Map.entry("04:52:c7", "Bose"), Map.entry("08:df:1f", "Bose"), Map.entry("2c:41:a1", "Bose"),
            Map.entry("4c:87:5d", "Bose"),
            // TCL
            Map.entry("70:70:aa", "TCL"), Map.entry("d4:6a:6a", "TCL"),
            // Vizio
            Map.entry("d4:36:39", "Vizio"),
            // Hisense
            Map.entry("00:1a:e8", "Hisense"), Map.entry("74:4a:a4", "Hisense"),
            // Canon
            Map.entry("00:1e:8f", "Canon"), Map.entry("18:0c:ac", "Canon"),
            // Brother
            Map.entry("00:1b:a9", "Brother"), Map.entry("00:80:77", "Brother"),
            // Epson
            Map.entry("00:00:48", "Epson"), Map.entry("00:26:ab", "Epson"),
            // Wistron (laptops/ODM for HP, Dell, etc.)
            Map.entry("74:ab:93", "Wistron"),
            // Pegatron (ODM for many PC brands)
            Map.entry("74:d4:23", "Pegatron"),
            // Murata (WiFi module vendor)
            Map.entry("44:a7:cf", "Murata"), Map.entry("60:f1:89", "Murata"),
            // Texas Instruments (TI) - embedded/IoT
            Map.entry("00:17:e9", "TI"), Map.entry("04:a3:16", "TI"), Map.entry("78:a5:04", "TI"),
            Map.entry("d0:39:72", "TI"),
            // Shenzhen Bilian (smart plugs, IoT)
            Map.entry("e8:cf:83", "Bilian"),
            // AzureWave (WiFi modules, often in cameras/IoT)
            Map.entry("74:da:88", "AzureWave"), Map.entry("b0:ee:45", "AzureWave"), Map.entry("84:28:59", "AzureWave"),
            // Eero
            Map.entry("50:01:d9", "Eero"), Map.entry("f8:bb:bf", "Eero"),
            // Arris / Motorola Surfboard
            Map.entry("00:00:ca", "Arris"), Map.entry("00:15:96", "Arris"), Map.entry("00:1d:cd", "Arris"),
            Map.entry("20:3d:66", "Arris"),
            // Meross
            Map.entry("48:e1:e9", "Meross"),
            // Govee
            Map.entry("bc:07:1d", "Govee"),
            // Eufy / Anker
            Map.entry("8c:85:80", "Eufy"), Map.entry("98:8f:e0", "Eufy"));

    /**
     * Look up the vendor name for a MAC address using the OUI prefix (first 3 bytes).
     *
     * @param mac full MAC address (e.g., "e8:fc:af:a3:a0:12")
     * @return vendor name or null if not found
     */
    public static @Nullable String lookupVendor(String mac) {
        if (mac.length() < 8) {
            return null;
        }
        String prefix = mac.substring(0, 8).toLowerCase();
        return OUI_MAP.get(prefix);
    }

    /**
     * Generate a synthetic hostname for a device based on its MAC OUI prefix.
     * Format: "Vendor-XXYYZZ" where XXYYZZ are the last 3 bytes of the MAC.
     *
     * @param mac full MAC address (e.g., "e8:fc:af:a3:a0:12")
     * @return generated hostname (e.g., "TPLink-a3a012") or empty string if vendor unknown
     */
    public static String generateHostname(String mac) {
        String vendor = lookupVendor(mac);
        if (vendor == null) {
            return "";
        }
        // Use last 3 bytes of MAC as suffix (e.g., "a3:a0:12" -> "a3a012")
        String suffix = mac.length() >= 17 ? mac.substring(9).replace(":", "") : mac.replace(":", "");
        return vendor + "-" + suffix;
    }

    /**
     * Check if a MAC address has the locally-administered bit set,
     * indicating it is a randomized/private MAC address.
     * OUI lookup is not meaningful for randomized MACs.
     *
     * @param mac full MAC address
     * @return true if the MAC is locally administered (randomized)
     */
    public static boolean isRandomizedMac(String mac) {
        if (mac.length() < 2) {
            return false;
        }
        try {
            int firstByte = Integer.parseInt(mac.substring(0, 2), 16);
            return (firstByte & 0x02) != 0;
        } catch (NumberFormatException e) {
            return false;
        }
    }
}
