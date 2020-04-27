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
package org.openhab.binding.paradoxalarm.internal.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.openhab.binding.paradoxalarm.internal.communication.IDataUpdateListener;
import org.openhab.binding.paradoxalarm.internal.communication.IParadoxCommunicator;
import org.openhab.binding.paradoxalarm.internal.exceptions.ParadoxRuntimeException;
import org.openhab.binding.paradoxalarm.internal.parsers.EvoParser;
import org.openhab.binding.paradoxalarm.internal.parsers.IParadoxParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link ParadoxPanel} Composition class which contains all Paradox entities.
 *
 * @author Konstantin Polihronov - Initial contribution
 */
public class ParadoxPanel implements IDataUpdateListener {

    private final Logger logger = LoggerFactory.getLogger(ParadoxPanel.class);

    private static ParadoxPanel paradoxPanel = new ParadoxPanel();

    private ParadoxInformation panelInformation;
    private List<Partition> partitions;
    private List<Zone> zones;
    private IParadoxParser parser;
    private IParadoxCommunicator communicator;

    private ParadoxPanel() {
        this.parser = new EvoParser();
    }

    public void createModelEntities() {
        byte[] panelInfoBytes = communicator.getPanelInfoBytes();
        panelInformation = new ParadoxInformation(panelInfoBytes, parser);

        if (isPanelSupported()) {
            logger.info("Paradox system is supported. Panel data retrieved={} ", panelInformation);
            createPartitions();
            createZones();
            updateEntitiesStates();
        } else {
            throw new ParadoxRuntimeException(
                    "Unsupported panel type. Type: " + panelInformation.getPanelType().name());
        }
    }

    public static ParadoxPanel getInstance() {
        return paradoxPanel;
    }

    public boolean isPanelSupported() {
        PanelType panelType = panelInformation.getPanelType();
        return panelType == PanelType.EVO48 || panelType == PanelType.EVO192 || panelType == PanelType.EVOHD;
    }

    public void updateEntitiesStates() {
        if (!isOnline()) {
            logger.debug("Not online. Unable to update entities states. ");
            return;
        }

        List<byte[]> currentPartitionFlags = communicator.getPartitionFlags();
        for (int i = 0; i < partitions.size(); i++) {
            Partition partition = partitions.get(i);
            partition.setState(parser.calculatePartitionState(currentPartitionFlags.get(i)));
        }

        ZoneStateFlags zoneStateFlags = communicator.getZoneStateFlags();
        for (int i = 0; i < zones.size(); i++) {
            Zone zone = zones.get(i);
            zone.setZoneState(parser.calculateZoneState(zone.getId(), zoneStateFlags));
        }
    }

    private List<Zone> createZones() {
        zones = new ArrayList<>();
        Map<Integer, String> zoneLabels = communicator.getZoneLabels();
        for (int i = 0; i < zoneLabels.size(); i++) {
            Zone zone = new Zone(i + 1, zoneLabels.get(i));
            zones.add(zone);
        }
        return zones;
    }

    private List<Partition> createPartitions() {
        partitions = new ArrayList<>();
        Map<Integer, String> partitionLabels = communicator.getPartitionLabels();
        for (int i = 0; i < partitionLabels.size(); i++) {
            Partition partition = new Partition(i + 1, partitionLabels.get(i));
            partitions.add(partition);
            logger.debug("Partition {}:\t{}", i + 1, partition.getState().getMainState());
        }
        return partitions;
    }

    public ParadoxInformation getPanelInformation() {
        return panelInformation;
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

    public boolean isOnline() {
        return communicator.isOnline();
    }

    @Override
    public void update() {
        if (panelInformation == null || partitions == null || zones == null) {
            createModelEntities();
        } else {
            updateEntitiesStates();
        }
    }

    public void setCommunicator(IParadoxCommunicator communicator) {
        this.communicator = communicator;
    }
}
