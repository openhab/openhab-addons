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
package main;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.openhab.binding.paradoxalarm.internal.communication.ICommunicatorBuilder;
import org.openhab.binding.paradoxalarm.internal.communication.IParadoxCommunicator;
import org.openhab.binding.paradoxalarm.internal.communication.ParadoxBuilderFactory;
import org.openhab.binding.paradoxalarm.internal.exceptions.ParadoxException;
import org.openhab.binding.paradoxalarm.internal.model.PanelType;
import org.openhab.binding.paradoxalarm.internal.model.ParadoxPanel;
import org.openhab.binding.paradoxalarm.internal.model.Partition;
import org.openhab.binding.paradoxalarm.internal.model.Zone;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link Main} - used for testing purposes of low-level stuff.
 *
 * @author Konstantin Polihronov - Initial contribution
 */
public class Main {

    private static Logger logger = LoggerFactory.getLogger(Main.class);

    private static String ipAddress;
    private static int port;

    // PASSWORD is your IP150 password
    private static String ip150Password;

    // PC Password is the value of section 3012, i.e. if value is 0987, PC Password is two bytes 0x09, 0x87
    private static String pcPassword;

    private static ScheduledExecutorService scheduler;

    private static final String LOG_LEVEL = "DEBUG";

    private static IParadoxCommunicator communicator;

    public static void main(String[] args) {
        System.setProperty(org.slf4j.impl.SimpleLogger.DEFAULT_LOG_LEVEL_KEY, LOG_LEVEL);

        readArguments(args);

        try {
            scheduler = Executors.newScheduledThreadPool(Runtime.getRuntime().availableProcessors());

            ParadoxBuilderFactory factory = new ParadoxBuilderFactory();
            ICommunicatorBuilder builder = factory.createBuilder(PanelType.EVO192);
            communicator = builder.withIp150Password(ip150Password).withPcPassword(pcPassword).withIpAddress(ipAddress)
                    .withTcpPort(port).withMaxPartitions(4).withMaxZones(60).withScheduler(scheduler).build();

            ParadoxPanel panel = ParadoxPanel.getInstance();
            panel.setCommunicator(communicator);

            communicator.setListeners(Arrays.asList(panel));

            panel.setCommunicator(communicator);

            // scheduler.schedule(communicator::initializeEpromData, 2, TimeUnit.SECONDS);
            // scheduler.schedule(communicator::refreshMemoryMap, 2, TimeUnit.SECONDS);

            scheduler.scheduleWithFixedDelay(() -> {
                updateDataCache(communicator, false);
            }, 7, 5, TimeUnit.SECONDS);
        } catch (Exception e) {
            logger.error("Exception: ", e);
            System.exit(0);
        }
    }

    private static void updateDataCache(IParadoxCommunicator communicator, boolean withEpromValues) {
        logger.debug("Refreshing memory map");
        communicator.refreshMemoryMap();
    }

    private static List<Zone> initializeZones(IParadoxCommunicator paradoxSystem)
            throws IOException, InterruptedException, ParadoxException {
        Map<Integer, String> zoneLabels = paradoxSystem.getZoneLabels();
        List<Zone> zones = new ArrayList<>();
        for (int i = 0; i < 40; i++) {
            Zone zone = new Zone(i + 1, zoneLabels.get(i));
            zones.add(zone);
        }
        return zones;
    }

    private static List<Partition> initializePartitions(IParadoxCommunicator paradoxSystem)
            throws IOException, InterruptedException, ParadoxException {
        Map<Integer, String> partitionLabels = paradoxSystem.getPartitionLabels();
        List<Partition> partitions = new ArrayList<>();
        for (int i = 0; i < partitionLabels.size(); i++) {
            Partition partition = new Partition(i + 1, partitionLabels.get(i));
            partitions.add(partition);
            logger.debug("Partition {}:\t{}", i + 1, partition.getState().getMainState());
        }
        return partitions;
    }

    private static void readArguments(String[] args) {
        if (args == null || args.length < 8 || !"--password".equals(args[0]) || args[1] == null || args[1].isEmpty()
                || args[2] == null || !"--pc_password".equals(args[2]) || args[3] == null || args[3].isEmpty()
                || !"--ip_address".equals(args[4]) || args[5] == null || args[5].isEmpty() || !"--port".equals(args[6])
                || args[7] == null || args[7].isEmpty()) {
            logger.error(
                    "Usage: application --password <YOUR_PASSWORD_FOR_IP150> --pc_password <your PC_password> --ip_address <address of IP150> --port <port of Paradox>\n (pc password default is 0000, can be obtained by checking section 3012), default port is 10000");
            System.exit(0);
        } else {
            logger.info("Arguments retrieved successfully from CLI.");

            ip150Password = args[1];
            logger.info("IP150 Password: {}", ip150Password);

            pcPassword = args[3];
            logger.info("PC Password: {}", pcPassword);

            ipAddress = args[5];
            logger.info("IP150 IP Address: {}", ipAddress);

            port = new Integer(args[7]);
            logger.info("IP150 port: {}", port);
        }
    }
}
