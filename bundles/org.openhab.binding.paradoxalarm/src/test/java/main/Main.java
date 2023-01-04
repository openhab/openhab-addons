/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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

import java.util.Arrays;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.openhab.binding.paradoxalarm.internal.communication.ICommunicatorBuilder;
import org.openhab.binding.paradoxalarm.internal.communication.IParadoxCommunicator;
import org.openhab.binding.paradoxalarm.internal.communication.ParadoxBuilderFactory;
import org.openhab.binding.paradoxalarm.internal.model.PanelType;
import org.openhab.binding.paradoxalarm.internal.model.ParadoxPanel;
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

    private static IParadoxCommunicator communicator;
    private static ParadoxPanel panel;

    public static void main(String[] args) {
        readArguments(args);

        try {
            scheduler = Executors.newScheduledThreadPool(Runtime.getRuntime().availableProcessors());

            ParadoxBuilderFactory factory = new ParadoxBuilderFactory();
            ICommunicatorBuilder builder = factory.createBuilder(PanelType.EVO192);
            communicator = builder.withIp150Password(ip150Password).withPcPassword(pcPassword).withIpAddress(ipAddress)
                    .withTcpPort(port).withMaxPartitions(4).withMaxZones(20).withScheduler(scheduler)
                    .withEncryption(true).build();

            panel = new ParadoxPanel();
            panel.setCommunicator(communicator);
            communicator.setListeners(Arrays.asList(panel));

            communicator.startLoginSequence();

            scheduler.scheduleWithFixedDelay(() -> {
                refreshMemoryMap(panel, false);
            }, 7, 5, TimeUnit.SECONDS);
        } catch (Exception e) {
            logger.error("Exception: ", e);
            System.exit(0);
        }
    }

    private static void refreshMemoryMap(ParadoxPanel panel, boolean withEpromValues) {
        logger.debug("Refreshing memory map");
        IParadoxCommunicator communicator = panel.getCommunicator();
        communicator.refreshMemoryMap();
        panel.getPartitions().stream().forEach(partition -> logger.debug("Partition={}", partition));
        panel.getZones().stream().filter(zone -> zone.getId() == 19).forEach(zone -> logger.debug("Zone={}", zone));
        logger.debug("PanelTime={}, ACLevel={}, DCLevel={}, BatteryLevel={}", panel.getPanelTime(), panel.getVdcLevel(),
                panel.getDcLevel(), panel.getBatteryLevel());
    }

    private static void readArguments(String[] args) {
        MainMethodArgParser parser = new MainMethodArgParser(args);
        logger.info("Arguments retrieved successfully from CLI.");

        ip150Password = parser.getPassword();
        logger.info("IP150 Password: {}", ip150Password);

        pcPassword = parser.getPcPassword();
        logger.info("PC Password: {}", pcPassword);

        ipAddress = parser.getIpAddress();
        logger.info("IP150 IP Address: {}", ipAddress);

        port = Integer.parseInt(parser.getPort());
        logger.info("IP150 port: {}", port);
    }
}
