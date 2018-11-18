/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.paradoxalarm.internal.communication;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.openhab.binding.paradoxalarm.internal.model.Partition;
import org.openhab.binding.paradoxalarm.internal.model.Zone;
import org.openhab.binding.paradoxalarm.internal.model.ZoneStateFlags;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link Main} - used for testing purposes only.
 *
 * @author Konstantin_Polihronov - Initial contribution
 */
public class Main {

    private static Logger logger = LoggerFactory.getLogger(Main.class);

    private static final String IP_ADDRESS = "192.168.254.231";
    private static final int PORT = 10000;

    // PASSWORD is your IP150 password
    private static String ip150Password;

    // PC Password is the value of section 3012, i.e. if value is 0987, PC Password is two bytes 0x09, 0x87
    private static final byte[] PC_PASSWORD = { (byte) 0x09, (byte) 0x87 };

    public static void main(String[] args) {
        handleArguments(args);

        try {
            IParadoxCommunicator paradoxSystem = new Evo192Communicator(IP_ADDRESS, PORT, ip150Password, PC_PASSWORD);

            List<Partition> partitions = initializePartitions(paradoxSystem);

            List<Zone> zones = initializeZones(paradoxSystem);

            while (true) {
                infiniteLoop(paradoxSystem, partitions, zones);
            }
            // paradoxSystem.logoutSequence();
            // paradoxSystem.close();
        } catch (Exception e) {
            logger.error("Exception: {}", e.getMessage(), e);
            System.exit(0);
        }
    }

    private static List<Zone> initializeZones(IParadoxCommunicator paradoxSystem) {
        List<String> zoneLabels = paradoxSystem.readZoneLabels();
        List<Zone> zones = new ArrayList<Zone>();
        for (int i = 0; i < 40; i++) {
            Zone zone = new Zone(i + 1, zoneLabels.get(i));

            zones.add(zone);
        }
        return zones;
    }

    private static List<Partition> initializePartitions(IParadoxCommunicator paradoxSystem) {
        List<String> partitionLabels = paradoxSystem.readPartitionLabels();
        List<Partition> partitions = new ArrayList<Partition>();
        for (int i = 0; i < partitionLabels.size(); i++) {
            Partition partition = new Partition(i + 1, partitionLabels.get(i));
            partitions.add(partition);
            logger.debug("Partition {}:\t{}", i + 1, partition.getState().calculatedState());
        }
        return partitions;
    }

    private static void handleArguments(String[] args) {
        if (args == null || args.length < 2 || !"--password".equals(args[0]) || args[1] == null || args[1].isEmpty()) {
            logger.error("Usage: application --password <YOUR_PASSWORD_FOR_IP150>");
            System.exit(0);
        } else {
            logger.info("Password retrieved successfully from CLI.");
        }
    }

    private static void infiniteLoop(IParadoxCommunicator paradoxSystem, List<Partition> partitions, List<Zone> zones)
            throws Exception, InterruptedException {
        paradoxSystem.refreshMemoryMap();
        List<byte[]> currentPartitionFlags = paradoxSystem.readPartitionFlags();
        for (int i = 0; i < partitions.size(); i++) {
            Partition partition = partitions.get(i);
            partition.setState(currentPartitionFlags.get(i));
            logger.debug("Partition {}:\t{}", partition.getLabel(), partition.getState().calculatedState());
        }

        ZoneStateFlags zoneStateFlags = paradoxSystem.readZoneStateFlags();
        for (int i = 0; i < zones.size(); i++) {
            Zone zone = zones.get(i);
            zone.setFlags(zoneStateFlags);
            logger.debug("Zone {}:\tOpened: {}, Tampered: {}, LowBattery: {}",
                    new Object[] { zone.getLabel(), zone.isOpened(), zone.isTampered(), zone.hasLowBattery() });
        }
        logger.debug("############################################################################");
        Thread.sleep(5000);
    }

    private static String retrievePassword(String file) {
        try {
            byte[] bytes = Files.readAllBytes(Paths.get(file));
            if (bytes != null && bytes.length > 0) {
                String result = new String(bytes);
                logger.debug("Password: {}", result);
                return result;
            }
        } catch (IOException e) {
            logger.debug("Exception during reading password from file", e);
        }
        return "";
    }
}
