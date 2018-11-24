/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.paradoxalarm.internal.model;

import java.util.ArrayList;
import java.util.List;

import org.openhab.binding.paradoxalarm.internal.communication.IParadoxCommunicator;
import org.openhab.binding.paradoxalarm.internal.exceptions.ParadoxBindingException;
import org.openhab.binding.paradoxalarm.internal.parsers.EvoParser;
import org.openhab.binding.paradoxalarm.internal.parsers.IParadoxParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link ParadoxSecuritySystem} Composition class which contains all Paradox entities.
 *
 * @author Konstantin_Polihronov - Initial contribution
 */
public class ParadoxSecuritySystem {

    private static Logger logger = LoggerFactory.getLogger(Partition.class);

    private List<Partition> partitions = new ArrayList<Partition>(8);
    private List<Zone> zones = new ArrayList<Zone>(192);
    private IParadoxCommunicator communicator;
    private IParadoxParser parser;
    private ParadoxInformation panelInformation;

    public ParadoxSecuritySystem(IParadoxCommunicator communicator) throws ParadoxBindingException {
        // TODO Maybe factory for creating parsers if more than EVO will be implemented?
        // Maybe need to extract the logon sequence and initial parsing of security type and use factory to create the
        // proper communicator/parsers?
        this.communicator = communicator;
        this.parser = new EvoParser();

        panelInformation = new ParadoxInformation(communicator.getPanelInfoBytes(), parser);

        if (isPanelSupported()) {
            logger.debug("Found supported panel - " + panelInformation);
            // initializePartitions();
            // initializeZones();
        } else {
            throw new ParadoxBindingException(
                    "Unsupported panel type. Type: " + panelInformation.getPanelType().name());
        }
    }

    public void updateEntities() throws Exception {
        List<byte[]> currentPartitionFlags = communicator.readPartitionFlags();
        for (int i = 0; i < partitions.size(); i++) {
            Partition partition = partitions.get(i);
            partition.setState(parser.calculatePartitionState(currentPartitionFlags.get(i)));
            logger.debug("Partition {}:\t{}", partition.getLabel(), partition.getState().getMainState());
        }

        ZoneStateFlags zoneStateFlags = communicator.readZoneStateFlags();
        for (int i = 0; i < zones.size(); i++) {
            Zone zone = zones.get(i);
            zone.setZoneState(parser.calculateZoneState(zone.getId(), zoneStateFlags));
        }
        logger.debug("############################################################################");
        Thread.sleep(5000);
    }

    private List<Zone> initializeZones() {
        List<String> zoneLabels = communicator.readZoneLabels();
        // TODO Use the size of retrieved labels list
        for (int i = 0; i < 40; i++) {
            Zone zone = new Zone(i + 1, zoneLabels.get(i));
            zones.add(zone);
        }
        return zones;
    }

    private List<Partition> initializePartitions() {
        List<String> partitionLabels = communicator.readPartitionLabels();
        // TODO move the range as field in communicator maybe?
        for (int i = 0; i < partitionLabels.size(); i++) {
            Partition partition = new Partition(i + 1, partitionLabels.get(i));
            partitions.add(partition);
            logger.debug("Partition {}:\t{}", i + 1, partition.getState().getMainState());
        }
        return partitions;
    }

    private boolean isPanelSupported() {
        PanelType panelType = panelInformation.getPanelType();
        return panelType == PanelType.EVO48 || panelType == PanelType.EVO192 || panelType == PanelType.EVOHD;
    }

    public List<Partition> getPartitions() {
        return partitions;
    }

    public void setPartitions(List<Partition> partitions) {
        this.partitions = partitions;
    }

    public List<Zone> getZones() {
        return zones;
    }

    public void setZones(List<Zone> zones) {
        this.zones = zones;
    }

    public IParadoxCommunicator getCommunicator() {
        return communicator;
    }
}
