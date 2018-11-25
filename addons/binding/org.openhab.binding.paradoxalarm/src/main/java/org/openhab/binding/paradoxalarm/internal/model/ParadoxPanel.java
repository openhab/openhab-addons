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
 * The {@link ParadoxPanel} Composition class which contains all Paradox entities.
 *
 * @author Konstantin_Polihronov - Initial contribution
 */
public class ParadoxPanel {

    private static final int NUMBER_OF_ZONES = 48;

    private static Logger logger = LoggerFactory.getLogger(Partition.class);

    private static ParadoxPanel paradoxPanel;

    private ParadoxInformation panelInformation;

    public ParadoxInformation getPanelInformation() {
        return panelInformation;
    }

    private List<Partition> partitions;
    private List<Zone> zones;
    private IParadoxCommunicator communicator;
    private IParadoxParser parser;

    private ParadoxPanel() {
        this.parser = new EvoParser();
    }

    public static ParadoxPanel getInstance() {
        synchronized (ParadoxPanel.class) {
            if (paradoxPanel == null) {
                paradoxPanel = new ParadoxPanel();
            }
            return paradoxPanel;
        }
    }

    // Mandatory to call this method after getting the instance for the first time :(
    public void init(IParadoxCommunicator communicator) throws ParadoxBindingException {
        // TODO Maybe factory for creating parsers if more than EVO will be implemented?
        // Maybe need to extract the logon sequence and initial parsing of security type and use factory to create the
        // proper communicator/parsers?
        this.communicator = communicator;

        panelInformation = new ParadoxInformation(communicator.getPanelInfoBytes(), parser);

        if (isPanelSupported()) {
            logger.debug("Found supported panel - " + panelInformation);
            initializePartitions();
            initializeZones();
        } else {
            throw new ParadoxBindingException(
                    "Unsupported panel type. Type: " + panelInformation.getPanelType().name());
        }
        updateEntitiesStates();
    }

    public boolean isPanelSupported() {
        PanelType panelType = panelInformation.getPanelType();
        return panelType == PanelType.EVO48 || panelType == PanelType.EVO192 || panelType == PanelType.EVOHD;
    }

    public void updateEntitiesStates() {
        List<byte[]> currentPartitionFlags = communicator.readPartitionFlags();
        for (int i = 0; i < partitions.size(); i++) {
            Partition partition = partitions.get(i);
            partition.setState(parser.calculatePartitionState(currentPartitionFlags.get(i)));
        }

        ZoneStateFlags zoneStateFlags = communicator.readZoneStateFlags();
        for (int i = 0; i < zones.size(); i++) {
            Zone zone = zones.get(i);
            zone.setZoneState(parser.calculateZoneState(zone.getId(), zoneStateFlags));
        }
    }

    private List<Zone> initializeZones() {
        zones = new ArrayList<Zone>();
        List<String> zoneLabels = communicator.readZoneLabels();
        // TODO Use the size of retrieved labels list
        for (int i = 0; i < NUMBER_OF_ZONES; i++) {
            Zone zone = new Zone(i + 1, zoneLabels.get(i));
            zones.add(zone);
        }
        return zones;
    }

    private List<Partition> initializePartitions() {
        partitions = new ArrayList<Partition>();
        List<String> partitionLabels = communicator.readPartitionLabels();
        // TODO move the range as field in communicator maybe?
        for (int i = 0; i < partitionLabels.size(); i++) {
            Partition partition = new Partition(i + 1, partitionLabels.get(i));
            partitions.add(partition);
            logger.debug("Partition {}:\t{}", i + 1, partition.getState().getMainState());
        }
        return partitions;
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
