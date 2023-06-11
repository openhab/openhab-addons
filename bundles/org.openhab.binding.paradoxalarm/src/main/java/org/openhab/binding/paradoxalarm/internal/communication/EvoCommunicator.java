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
package org.openhab.binding.paradoxalarm.internal.communication;

import java.io.IOException;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.openhab.binding.paradoxalarm.internal.communication.messages.EpromRequestPayload;
import org.openhab.binding.paradoxalarm.internal.communication.messages.HeaderMessageType;
import org.openhab.binding.paradoxalarm.internal.communication.messages.IPayload;
import org.openhab.binding.paradoxalarm.internal.communication.messages.ParadoxIPPacket;
import org.openhab.binding.paradoxalarm.internal.communication.messages.RamRequestPayload;
import org.openhab.binding.paradoxalarm.internal.exceptions.ParadoxException;
import org.openhab.binding.paradoxalarm.internal.exceptions.ParadoxRuntimeException;
import org.openhab.binding.paradoxalarm.internal.model.EntityType;
import org.openhab.binding.paradoxalarm.internal.model.PanelType;
import org.openhab.binding.paradoxalarm.internal.model.ZoneStateFlags;
import org.openhab.binding.paradoxalarm.internal.util.ParadoxUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link EvoCommunicator} is responsible for handling communication to Evo192 alarm system via IP150 interface.
 *
 * @author Konstantin Polihronov - Initial contribution
 */
public class EvoCommunicator extends GenericCommunicator implements IParadoxCommunicator {

    private static final byte RAM_BLOCK_SIZE = (byte) 64;

    private final Logger logger = LoggerFactory.getLogger(EvoCommunicator.class);

    private MemoryMap memoryMap;

    private Map<EntityType, Map<Integer, String>> entityLabelsMap = new HashMap<>();

    private PanelType panelType = PanelType.UNKNOWN;
    private Integer maxPartitions;
    private Integer maxZones;

    private EvoCommunicator(String ipAddress, int tcpPort, String ip150Password, String pcPassword,
            ScheduledExecutorService scheduler, PanelType panelType, Integer maxPartitions, Integer maxZones,
            boolean useEncryption) throws UnknownHostException, IOException {
        super(ipAddress, tcpPort, ip150Password, pcPassword, scheduler, useEncryption);
        this.panelType = panelType;
        this.maxPartitions = maxPartitions;
        this.maxZones = maxZones;
        logger.debug("PanelType={}, Max Partitions={}, Max Zones={}", panelType, maxPartitions, maxZones);
        initializeMemoryMap();
    }

    @Override
    protected void receiveEpromResponse(IResponse response) {
        byte[] payload = response.getPayload();
        if (payload != null) {
            EpromRequest request = (EpromRequest) response.getRequest();
            int entityId = request.getEntityId();
            EntityType entityType = request.getEntityType();
            updateEntityLabel(entityType, entityId, payload);
        } else {
            logger.debug("Wrong parsed result. Probably wrong data received in response. Response={}", response);
            return;
        }
    }

    @Override
    protected void receiveRamResponse(IResponse response) {
        byte[] payload = response.getPayload();
        if (payload != null && payload.length >= RAM_BLOCK_SIZE) {
            RamRequest request = (RamRequest) response.getRequest();
            int ramBlockNumber = request.getRamBlockNumber();
            memoryMap.updateElement(ramBlockNumber, payload);
            if (logger.isTraceEnabled()) {
                logger.trace("Result for ramBlock={} is [{}]", ramBlockNumber, ParadoxUtil.byteArrayToString(payload));
            }

            // Trigger listeners update when last memory page update is received
            if (ramBlockNumber == panelType.getRamPagesNumber()) {
                updateListeners();
            }
        } else {
            logger.debug("Wrong parsed result. Probably wrong data received in response");
            return;
        }
    }

    private void updateEntityLabel(EntityType entityType, int entityId, byte[] payload) {
        String label = createString(payload);
        logger.debug("{} label updated to: {}", entityType, label);

        entityLabelsMap.get(entityType).put(entityId, label);
    }

    private void retrievePartitionLabel(int partitionNo) {
        logger.debug("Submitting request for partition label: {}", partitionNo);
        int address = 0x3A6B + (partitionNo) * 107;
        byte labelLength = 16;

        try {
            IPayload payload = new EpromRequestPayload(address, labelLength);
            ParadoxIPPacket readEpromIPPacket = createSerialPassthroughPacket(payload);

            IRequest epromRequest = new EpromRequest(partitionNo, EntityType.PARTITION, readEpromIPPacket, this);
            submitRequest(epromRequest);
        } catch (ParadoxException e) {
            logger.debug("Error creating request for with number={}, Exception={} ", partitionNo, e.getMessage());
        }
    }

    private ParadoxIPPacket createSerialPassthroughPacket(IPayload payload) {
        ParadoxIPPacket packet = new ParadoxIPPacket(payload.getBytes());
        packet.setMessageType(HeaderMessageType.SERIAL_PASSTHRU_REQUEST).setUnknown0((byte) 0x14);
        return packet;
    }

    private void retrieveZoneLabel(int zoneNumber) {
        logger.debug("Submitting request for zone label: {}", zoneNumber);
        final byte labelLength = 16;

        try {
            int address;
            if (zoneNumber < 96) {
                address = 0x430 + (zoneNumber) * 16;
            } else {
                address = 0x62F7 + (zoneNumber - 96) * 16;
            }

            IPayload payload = new EpromRequestPayload(address, labelLength);
            ParadoxIPPacket readEpromIPPacket = createSerialPassthroughPacket(payload);

            IRequest epromRequest = new EpromRequest(zoneNumber, EntityType.ZONE, readEpromIPPacket, this);
            submitRequest(epromRequest);
        } catch (ParadoxException e) {
            logger.debug("Error creating request with number={}, Exception={} ", zoneNumber, e.getMessage());
        }
    }

    @Override
    public List<byte[]> getPartitionFlags() {
        List<byte[]> result = new ArrayList<>();

        byte[] element = memoryMap.getElement(2);
        byte[] firstBlock = Arrays.copyOfRange(element, 32, 64);

        element = memoryMap.getElement(3);
        byte[] secondBlock = Arrays.copyOfRange(element, 0, 16);
        byte[] mergeByteArrays = ParadoxUtil.mergeByteArrays(firstBlock, secondBlock);
        for (int i = 0; i < mergeByteArrays.length; i += 6) {
            result.add(Arrays.copyOfRange(mergeByteArrays, i, i + 6));
        }

        return result;
    }

    @Override
    public ZoneStateFlags getZoneStateFlags() {
        ZoneStateFlags result = new ZoneStateFlags();

        byte[] firstPage = memoryMap.getElement(0);
        byte[] secondPage = memoryMap.getElement(8);

        int pageOffset = panelType == PanelType.EVO48 ? 34 : 40;
        byte[] firstBlock = Arrays.copyOfRange(firstPage, 28, pageOffset);
        if (panelType != PanelType.EVO192) {
            result.setZonesOpened(firstBlock);
        } else {
            byte[] secondBlock = Arrays.copyOfRange(secondPage, 0, 12);
            byte[] zonesOpened = ParadoxUtil.mergeByteArrays(firstBlock, secondBlock);
            result.setZonesOpened(zonesOpened);
        }

        pageOffset = panelType == PanelType.EVO48 ? 46 : 52;
        firstBlock = Arrays.copyOfRange(firstPage, 40, pageOffset);
        if (panelType != PanelType.EVO192) {
            result.setZonesTampered(firstBlock);
        } else {
            byte[] secondBlock = Arrays.copyOfRange(secondPage, 12, 24);
            byte[] zonesTampered = ParadoxUtil.mergeByteArrays(firstBlock, secondBlock);
            result.setZonesTampered(zonesTampered);
        }

        pageOffset = panelType == PanelType.EVO48 ? 58 : 64;
        firstBlock = Arrays.copyOfRange(firstPage, 52, pageOffset);
        if (panelType != PanelType.EVO192) {
            result.setZonesTampered(firstBlock);
        } else {
            byte[] secondBlock = Arrays.copyOfRange(secondPage, 24, 36);
            byte[] zonesLowBattery = ParadoxUtil.mergeByteArrays(firstBlock, secondBlock);
            result.setZonesLowBattery(zonesLowBattery);
        }

        return result;
    }

    public void initializeMemoryMap() {
        for (EntityType type : EntityType.values()) {
            entityLabelsMap.put(type, new HashMap<>());
        }

        List<byte[]> ramCache = new ArrayList<>(panelType.getRamPagesNumber() + 1);
        for (int i = 0; i <= panelType.getRamPagesNumber(); i++) {
            ramCache.add(new byte[0]);
        }
        memoryMap = new MemoryMap(ramCache);
    }

    @Override
    public void refreshMemoryMap() {
        if (!isOnline()) {
            logger.debug("Attempt to refresh memory map was made, but communicator is not online. Skipping update.");
            return;
        }

        SyncQueue queue = SyncQueue.getInstance();
        synchronized (queue) {
            for (int i = 1; i <= panelType.getRamPagesNumber(); i++) {
                submitRamRequest(i);
            }
        }
    }

    private void submitRamRequest(int blockNo) {
        try {
            logger.trace("Creating RAM page {} read request", blockNo);
            IPayload payload = new RamRequestPayload(blockNo, RAM_BLOCK_SIZE);
            ParadoxIPPacket ipPacket = createSerialPassthroughPacket(payload);
            IRequest ramRequest = new RamRequest(blockNo, ipPacket, this);
            submitRequest(ramRequest);
        } catch (ParadoxException e) {
            logger.debug(
                    "Unable to create request payload from provided bytes to read. blockNo={}, bytes to read={}. Exception={}",
                    blockNo, RAM_BLOCK_SIZE, e.getMessage());
        }
    }

    private String createString(byte[] payloadResult) {
        return new String(payloadResult, StandardCharsets.US_ASCII);
    }

    @Override
    public void executeCommand(String command) {
        IP150Command ip150Command = IP150Command.valueOf(command);
        switch (ip150Command) {
            case LOGIN:
                startLoginSequence();
                return;
            case LOGOUT:
                CommunicationState.LOGOUT.runPhase(this);
                return;
            case RESET:
                CommunicationState.LOGOUT.runPhase(this);
                scheduler.schedule(this::startLoginSequence, 5, TimeUnit.SECONDS);
                return;
            default:
                logger.debug("Command {} not implemented.", command);
        }
    }

    @Override
    public MemoryMap getMemoryMap() {
        return memoryMap;
    }

    public Map<EntityType, Map<Integer, String>> getEntityLabelsMap() {
        return entityLabelsMap;
    }

    @Override
    public Map<Integer, String> getPartitionLabels() {
        return entityLabelsMap.get(EntityType.PARTITION);
    }

    @Override
    public Map<Integer, String> getZoneLabels() {
        return entityLabelsMap.get(EntityType.ZONE);
    }

    @Override
    public void initializeData() {
        synchronized (SyncQueue.getInstance()) {
            initializeEpromData();
            refreshMemoryMap();
        }
    }

    private void initializeEpromData() {
        for (int i = 0; i < maxPartitions; i++) {
            retrievePartitionLabel(i);
        }
        for (int i = 0; i < maxZones; i++) {
            retrieveZoneLabel(i);
        }
    }

    public static class EvoCommunicatorBuilder implements ICommunicatorBuilder {

        private final Logger logger = LoggerFactory.getLogger(EvoCommunicatorBuilder.class);

        // Mandatory parameters
        private PanelType panelType;
        private String ipAddress;
        private String ip150Password;
        private ScheduledExecutorService scheduler;

        // Non mandatory or with predefined values
        private Integer maxPartitions;
        private Integer maxZones;
        private int tcpPort = 10000;
        private String pcPassword = "0000";

        private boolean useEncryption;

        EvoCommunicatorBuilder(PanelType panelType) {
            this.panelType = panelType;
        }

        @Override
        public IParadoxCommunicator build() {
            if (ipAddress == null || ipAddress.isEmpty()) {
                final String msg = "IP address cannot be empty !";
                logger.debug(msg);
                throw new ParadoxRuntimeException(msg);
            }

            if (ip150Password == null || ip150Password.isEmpty()) {
                final String msg = "Password for IP150 cannot be empty !";
                logger.debug(msg);
                throw new ParadoxRuntimeException(msg);
            }

            if (scheduler == null) {
                final String msg = "Scheduler is mandatory parameter !";
                logger.debug(msg);
                throw new ParadoxRuntimeException(msg);
            }

            if (maxPartitions == null || maxPartitions < 1) {
                this.maxPartitions = panelType.getPartitions();
            }

            if (maxZones == null || maxZones < 1) {
                this.maxZones = panelType.getZones();
            }

            try {
                return new EvoCommunicator(ipAddress, tcpPort, ip150Password, pcPassword, scheduler, panelType,
                        maxPartitions, maxZones, useEncryption);
            } catch (IOException e) {
                logger.warn("Unable to create communicator for Panel={}. Message={}", panelType, e.getMessage());
                throw new ParadoxRuntimeException(e);
            }
        }

        @Override
        public ICommunicatorBuilder withMaxZones(Integer maxZones) {
            this.maxZones = maxZones;
            return this;
        }

        @Override
        public ICommunicatorBuilder withMaxPartitions(Integer maxPartitions) {
            this.maxPartitions = maxPartitions;
            return this;
        }

        @Override
        public ICommunicatorBuilder withIp150Password(String ip150Password) {
            this.ip150Password = ip150Password;
            return this;
        }

        @Override
        public ICommunicatorBuilder withPcPassword(String pcPassword) {
            this.pcPassword = pcPassword;
            return this;
        }

        @Override
        public ICommunicatorBuilder withIpAddress(String ipAddress) {
            this.ipAddress = ipAddress;
            return this;
        }

        @Override
        public ICommunicatorBuilder withTcpPort(Integer tcpPort) {
            this.tcpPort = tcpPort;
            return this;
        }

        @Override
        public ICommunicatorBuilder withScheduler(ScheduledExecutorService scheduler) {
            this.scheduler = scheduler;
            return this;
        }

        @Override
        public ICommunicatorBuilder withEncryption(boolean useEncryption) {
            this.useEncryption = useEncryption;
            return this;
        }
    }
}
