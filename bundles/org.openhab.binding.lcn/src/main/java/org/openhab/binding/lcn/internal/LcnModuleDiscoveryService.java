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
package org.openhab.binding.lcn.internal;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.config.discovery.AbstractDiscoveryService;
import org.eclipse.smarthome.config.discovery.DiscoveryResultBuilder;
import org.eclipse.smarthome.config.discovery.DiscoveryService;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.eclipse.smarthome.core.thing.binding.ThingHandlerService;
import org.openhab.binding.lcn.internal.common.LcnAddrMod;
import org.openhab.binding.lcn.internal.common.NullScheduledFuture;
import org.openhab.binding.lcn.internal.connection.Connection;
import org.openhab.binding.lcn.internal.subhandler.LcnModuleMetaAckSubHandler;
import org.openhab.binding.lcn.internal.subhandler.LcnModuleMetaFirmwareSubHandler;
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
    private static final int MODULE_NAME_PART_COUNT = 2;
    private static final int DISCOVERY_TIMEOUT_SEC = 90;
    private static final int ACK_TIMEOUT_MS = 1000;
    private static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Collections
            .unmodifiableSet(Stream.of(LcnBindingConstants.THING_TYPE_MODULE).collect(Collectors.toSet()));
    private @Nullable PckGatewayHandler bridgeHandler;
    private Map<LcnAddrMod, Map<Integer, String>> moduleNames = new HashMap<>();
    private Map<LcnAddrMod, DiscoveryResultBuilder> discoveryResultBuilders = new HashMap<>();
    private List<LcnAddrMod> successfullyDiscovered = new LinkedList<>();
    private LinkedList<@Nullable LcnAddrMod> serialNumberRequestQueue = new LinkedList<>();
    private LinkedList<@Nullable LcnAddrMod> moduleNameRequestQueue = new LinkedList<>();
    private volatile ScheduledFuture<?> queueProcessor = NullScheduledFuture.getInstance();
    private ScheduledFuture<?> builderTask = NullScheduledFuture.getInstance();

    public LcnModuleDiscoveryService() {
        super(SUPPORTED_THING_TYPES_UIDS, DISCOVERY_TIMEOUT_SEC, false);
    }

    @Override
    public void setThingHandler(@Nullable ThingHandler handler) {
        if (handler instanceof PckGatewayHandler) {
            this.bridgeHandler = (PckGatewayHandler) handler;
        }
    }

    @Override
    public @Nullable ThingHandler getThingHandler() {
        return bridgeHandler;
    }

    @Override
    public void deactivate() {
        super.deactivate();
        stopScan();
    }

    @SuppressWarnings({ "unused", "null" })
    @Override
    protected void startScan() {
        synchronized (this) {
            if (bridgeHandler == null) {
                logger.warn("Bridge handler not set");
                return;
            }

            if (bridgeHandler.getConnection() == null) {
                builderTask.cancel(true);
            }

            bridgeHandler.registerPckListener(data -> {
                Matcher matcher;

                if ((matcher = LcnModuleMetaAckSubHandler.PATTERN_POS.matcher(data)).matches()
                        || (matcher = LcnModuleMetaFirmwareSubHandler.PATTERN.matcher(data)).matches()
                        || (matcher = NAME_PATTERN.matcher(data)).matches()) {
                    synchronized (LcnModuleDiscoveryService.this) {
                        Connection connection = bridgeHandler.getConnection();

                        if (connection == null) {
                            return;
                        }

                        LcnAddrMod addr = new LcnAddrMod(
                                bridgeHandler.toLogicalSegmentId(Integer.parseInt(matcher.group("segId"))),
                                Integer.parseInt(matcher.group("modId")));

                        if (matcher.pattern() == LcnModuleMetaAckSubHandler.PATTERN_POS) {
                            // the module could send an Ack with a response to another command. So, ignore the Ack, when
                            // we received our data already.
                            if (!discoveryResultBuilders.containsKey(addr)) {
                                serialNumberRequestQueue.add(addr);
                                rescheduleQueueProcessor(); // delay request of serial until all modules finished ACKing
                            }

                            if (!moduleNames.containsKey(addr)
                                    || moduleNames.get(addr).size() != MODULE_NAME_PART_COUNT) {
                                moduleNameRequestQueue.add(addr);
                                rescheduleQueueProcessor(); // delay request of names until all modules finished ACKing
                            }
                        } else if (matcher.pattern() == LcnModuleMetaFirmwareSubHandler.PATTERN) {
                            Map<String, Object> properties = new HashMap<>(5);
                            properties.put("segmentId", addr.getSegmentId());
                            properties.put("moduleId", addr.getModuleId());

                            ThingUID bridgeUid = bridgeHandler.getThing().getUID();
                            String thingId = matcher.group("sn");
                            ThingUID thingUid = new ThingUID(LcnBindingConstants.THING_TYPE_MODULE, bridgeUid, thingId);

                            DiscoveryResultBuilder discoveryResult = DiscoveryResultBuilder.create(thingUid)
                                    .withProperties(properties).withBridge(bridgeUid);

                            discoveryResultBuilders.put(addr, discoveryResult);
                        } else if (matcher.pattern() == NAME_PATTERN) {
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
                    discoveryResultBuilders.entrySet().stream().filter(e -> moduleNames.containsKey(e.getKey()))
                            .filter(e -> moduleNames.get(e.getKey()).size() == MODULE_NAME_PART_COUNT)
                            .filter(e -> !successfullyDiscovered.contains(e.getKey())).forEach(e -> {
                                // collect() to remove while iterating
                                StringBuilder thingName = new StringBuilder();
                                if (e.getKey().getSegmentId() != 0) {
                                    thingName.append("Segment " + e.getKey().getSegmentId() + " ");
                                }

                                thingName.append("Module " + e.getKey().getModuleId() + ": ");
                                thingName.append(moduleNames.get(e.getKey()).get(0));
                                thingName.append(moduleNames.get(e.getKey()).get(1));

                                thingDiscovered(e.getValue().withLabel(thingName.toString()).build());
                                successfullyDiscovered.add(e.getKey());
                            });
                }
            }, 500, 500, TimeUnit.MILLISECONDS);

            bridgeHandler.sendModuleDiscoveryCommand();
        }
    }

    private synchronized void rescheduleQueueProcessor() {
        // delay serial number and module name requests to not clog the bus
        queueProcessor.cancel(true);
        queueProcessor = scheduler.scheduleWithFixedDelay(() -> {
            PckGatewayHandler localBridgeHandler = bridgeHandler;
            if (localBridgeHandler != null) {
                synchronized (serialNumberRequestQueue) {
                    synchronized (moduleNameRequestQueue) {
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
                }
            }
        }, ACK_TIMEOUT_MS, ACK_TIMEOUT_MS, TimeUnit.MILLISECONDS);
    }

    @Override
    public synchronized void stopScan() {
        builderTask.cancel(true);
        queueProcessor.cancel(true);
        PckGatewayHandler localBridgeHandler = bridgeHandler;
        if (localBridgeHandler != null) {
            localBridgeHandler.removeAllPckListeners();
        }
        successfullyDiscovered.clear();
        moduleNames.clear();

        super.stopScan();
    }
}
