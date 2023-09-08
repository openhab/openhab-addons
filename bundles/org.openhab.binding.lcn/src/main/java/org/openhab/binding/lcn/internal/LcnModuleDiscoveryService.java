/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
package org.openhab.binding.lcn.internal;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.lcn.internal.common.LcnAddrMod;
import org.openhab.binding.lcn.internal.connection.Connection;
import org.openhab.binding.lcn.internal.subhandler.LcnModuleMetaAckSubHandler;
import org.openhab.binding.lcn.internal.subhandler.LcnModuleMetaFirmwareSubHandler;
import org.openhab.core.config.discovery.AbstractDiscoveryService;
import org.openhab.core.config.discovery.DiscoveryResultBuilder;
import org.openhab.core.config.discovery.DiscoveryService;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.thing.binding.ThingHandlerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Scans all LCN segments for LCN modules.
 *
 * Scan approach:
 * 1. Send "Leerkomando" to the broadcast address with request for Ack set
 * 2. For every received Ack, send the following requests to the module:
 * - serial number request (SN)
 * - module's name first part request (NM1)
 * - module's name second part request (NM2)
 * 3. When all three messages have been received, fire thingDiscovered()
 *
 * @author Fabian Wolter - Initial Contribution
 */
@NonNullByDefault
public class LcnModuleDiscoveryService extends AbstractDiscoveryService
        implements DiscoveryService, ThingHandlerService {
    private final Logger logger = LoggerFactory.getLogger(LcnModuleDiscoveryService.class);
    private static final Pattern NAME_PATTERN = Pattern
            .compile("=M(?<segId>\\d{3})(?<modId>\\d{3}).N(?<part>[1-2]{1})(?<name>.*)");
    private static final String SEGMENT_ID = "segmentId";
    private static final String MODULE_ID = "moduleId";
    private static final int MODULE_NAME_PART_COUNT = 2;
    private static final int DISCOVERY_TIMEOUT_SEC = 90;
    private static final int ACK_TIMEOUT_MS = 1000;
    private static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Set.of(LcnBindingConstants.THING_TYPE_MODULE);
    private @Nullable PckGatewayHandler bridgeHandler;
    private final Map<LcnAddrMod, @Nullable Map<Integer, String>> moduleNames = new HashMap<>();
    private final Map<LcnAddrMod, DiscoveryResultBuilder> discoveryResultBuilders = new ConcurrentHashMap<>();
    private final List<LcnAddrMod> successfullyDiscovered = new LinkedList<>();
    private final Queue<@Nullable LcnAddrMod> serialNumberRequestQueue = new ConcurrentLinkedQueue<>();
    private final Queue<@Nullable LcnAddrMod> moduleNameRequestQueue = new ConcurrentLinkedQueue<>();
    private @Nullable volatile ScheduledFuture<?> queueProcessor;
    private @Nullable ScheduledFuture<?> builderTask;

    public LcnModuleDiscoveryService() {
        super(SUPPORTED_THING_TYPES_UIDS, DISCOVERY_TIMEOUT_SEC, false);
    }

    @Override
    public void setThingHandler(@Nullable ThingHandler handler) {
        if (handler instanceof PckGatewayHandler gatewayHandler) {
            this.bridgeHandler = gatewayHandler;
        }
    }

    @Override
    public @Nullable ThingHandler getThingHandler() {
        return bridgeHandler;
    }

    @Override
    public void deactivate() {
        stopScan();
        super.deactivate();
    }

    @Override
    @SuppressWarnings("PMD.CompareObjectsWithEquals")
    protected void startScan() {
        synchronized (this) {
            PckGatewayHandler localBridgeHandler = bridgeHandler;
            if (localBridgeHandler == null) {
                logger.warn("Bridge handler not set");
                return;
            }

            ScheduledFuture<?> localBuilderTask = builderTask;
            if (localBridgeHandler.getConnection() == null && localBuilderTask != null) {
                localBuilderTask.cancel(true);
            }

            localBridgeHandler.registerPckListener(data -> {
                Matcher matcher;

                if ((matcher = LcnModuleMetaAckSubHandler.PATTERN_POS.matcher(data)).matches()
                        || (matcher = LcnModuleMetaFirmwareSubHandler.PATTERN.matcher(data)).matches()
                        || (matcher = NAME_PATTERN.matcher(data)).matches()) {
                    synchronized (LcnModuleDiscoveryService.this) {
                        Connection connection = localBridgeHandler.getConnection();

                        if (connection == null) {
                            return;
                        }

                        LcnAddrMod addr = new LcnAddrMod(
                                localBridgeHandler.toLogicalSegmentId(Integer.parseInt(matcher.group("segId"))),
                                Integer.parseInt(matcher.group("modId")));

                        if (matcher.pattern() == LcnModuleMetaAckSubHandler.PATTERN_POS) {
                            // Received an ACK frame

                            // The module could send an Ack with a response to another command. So, ignore the Ack, when
                            // we received our data already.
                            if (!discoveryResultBuilders.containsKey(addr)) {
                                serialNumberRequestQueue.add(addr);
                                rescheduleQueueProcessor(); // delay request of serial until all modules finished ACKing
                            }

                            Map<Integer, String> localNameParts = moduleNames.get(addr);
                            if (localNameParts == null || localNameParts.size() != MODULE_NAME_PART_COUNT) {
                                moduleNameRequestQueue.add(addr);
                                rescheduleQueueProcessor(); // delay request of names until all modules finished ACKing
                            }
                        } else if (matcher.pattern() == LcnModuleMetaFirmwareSubHandler.PATTERN) {
                            // Received a firmware version info frame

                            ThingUID bridgeUid = localBridgeHandler.getThing().getUID();
                            String serialNumber = matcher.group("sn");

                            String thingID = String.format("S%03dM%03d", addr.getSegmentId(), addr.getModuleId());

                            ThingUID thingUid = new ThingUID(LcnBindingConstants.THING_TYPE_MODULE, bridgeUid, thingID);

                            Map<String, Object> properties = new HashMap<>(3);
                            properties.put(SEGMENT_ID, addr.getSegmentId());
                            properties.put(MODULE_ID, addr.getModuleId());
                            properties.put(Thing.PROPERTY_SERIAL_NUMBER, serialNumber);

                            DiscoveryResultBuilder discoveryResult = DiscoveryResultBuilder.create(thingUid)
                                    .withProperties(properties).withRepresentationProperty(Thing.PROPERTY_SERIAL_NUMBER)
                                    .withBridge(bridgeUid);

                            discoveryResultBuilders.put(addr, discoveryResult);
                        } else if (matcher.pattern() == NAME_PATTERN) {
                            // Received part of a module's name frame

                            final int part = Integer.parseInt(matcher.group("part")) - 1;
                            final String name = matcher.group("name");

                            moduleNames.compute(addr, (partNumber, namePart) -> {
                                Map<Integer, String> namePartMapping = namePart;
                                if (namePartMapping == null) {
                                    namePartMapping = new HashMap<>();
                                }

                                namePartMapping.put(part, name);

                                return namePartMapping;
                            });
                        }
                    }
                }
            });

            builderTask = scheduler.scheduleWithFixedDelay(() -> {
                synchronized (LcnModuleDiscoveryService.this) {
                    discoveryResultBuilders.entrySet().stream().filter(e -> {
                        Map<Integer, String> localNameParts = moduleNames.get(e.getKey());
                        return localNameParts != null && localNameParts.size() == MODULE_NAME_PART_COUNT;
                    }).filter(e -> !successfullyDiscovered.contains(e.getKey())).forEach(e -> {
                        StringBuilder thingName = new StringBuilder();
                        if (e.getKey().getSegmentId() != 0) {
                            thingName.append("Segment " + e.getKey().getSegmentId() + " ");
                        }

                        thingName.append("Module " + e.getKey().getModuleId() + ": ");
                        Map<Integer, String> localNameParts = moduleNames.get(e.getKey());
                        if (localNameParts != null) {
                            thingName.append(localNameParts.get(0));
                            thingName.append(localNameParts.get(1));

                            thingDiscovered(e.getValue().withLabel(thingName.toString()).build());
                            successfullyDiscovered.add(e.getKey());
                        }
                    });
                }
            }, 500, 500, TimeUnit.MILLISECONDS);

            localBridgeHandler.sendModuleDiscoveryCommand();
        }
    }

    private synchronized void rescheduleQueueProcessor() {
        // delay serial number and module name requests to not clog the bus
        ScheduledFuture<?> localQueueProcessor = queueProcessor;
        if (localQueueProcessor != null) {
            localQueueProcessor.cancel(true);
        }
        queueProcessor = scheduler.scheduleWithFixedDelay(() -> {
            PckGatewayHandler localBridgeHandler = bridgeHandler;
            if (localBridgeHandler != null) {
                LcnAddrMod serial = serialNumberRequestQueue.poll();
                if (serial != null) {
                    localBridgeHandler.sendSerialNumberRequest(serial);
                }

                LcnAddrMod name = moduleNameRequestQueue.poll();
                if (name != null) {
                    localBridgeHandler.sendModuleNameRequest(name);
                }

                // stop scan when all LCN modules have been requested
                if (serial == null && name == null) {
                    scheduler.schedule(this::stopScan, ACK_TIMEOUT_MS, TimeUnit.MILLISECONDS);
                }
            }
        }, ACK_TIMEOUT_MS, ACK_TIMEOUT_MS, TimeUnit.MILLISECONDS);
    }

    @Override
    public synchronized void stopScan() {
        ScheduledFuture<?> localBuilderTask = builderTask;
        if (localBuilderTask != null) {
            localBuilderTask.cancel(true);
        }
        ScheduledFuture<?> localQueueProcessor = queueProcessor;
        if (localQueueProcessor != null) {
            localQueueProcessor.cancel(true);
        }
        PckGatewayHandler localBridgeHandler = bridgeHandler;
        if (localBridgeHandler != null) {
            localBridgeHandler.removeAllPckListeners();
        }
        successfullyDiscovered.clear();
        moduleNames.clear();

        super.stopScan();
    }
}
