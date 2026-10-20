/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
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
package org.openhab.binding.e3dc.internal.discovery;

import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.BiFunction;
import java.util.function.Predicate;
import java.util.stream.IntStream;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.e3dc.internal.E3DCBindingConstants;
import org.openhab.binding.e3dc.internal.E3DCContainerRequests;
import org.openhab.binding.e3dc.internal.handler.E3DCBridgeHandler;
import org.openhab.binding.e3dc.internal.rscp.RscpException;
import org.openhab.binding.e3dc.internal.rscp.RscpTag;
import org.openhab.binding.e3dc.internal.rscp.RscpValue;
import org.openhab.core.config.discovery.AbstractThingHandlerDiscoveryService;
import org.openhab.core.config.discovery.DiscoveryResult;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.thing.binding.ThingHandlerService;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ServiceScope;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Proposes the batteries and the battery cell modules of the device as things.
 *
 * <p>
 * The power plant is always proposed. The device is asked which batteries it has, and for each battery which
 * cell modules it reports. For power meters and wallboxes the device gives no count, so the indexes are asked
 * for one after the other and each one that answers with values is proposed.
 *
 * <p>
 * Every answer is logged at DEBUG level, those of indexes that are not proposed included, so that the
 * behaviour of a device can be analysed from the log.
 *
 * @author Bernd Weymann - Initial contribution
 */
@Component(scope = ServiceScope.PROTOTYPE, service = ThingHandlerService.class)
@NonNullByDefault
public class E3DCDiscoveryService extends AbstractThingHandlerDiscoveryService<E3DCBridgeHandler> {

    private static final int TIMEOUT_SECONDS = 30;
    private static final int FIRST_SCAN_DELAY_SECONDS = 30;
    private static final int SCAN_INTERVAL_SECONDS = 3600;
    /** Gives the first poll after the connection came up a moment, before the discovery asks as well. */
    private static final int ONLINE_SCAN_DELAY_SECONDS = 5;

    private final Logger logger = LoggerFactory.getLogger(E3DCDiscoveryService.class);

    private @Nullable ScheduledFuture<?> backgroundScan;
    private final Runnable onlineScan = () -> scheduler.schedule(this::scan, ONLINE_SCAN_DELAY_SECONDS,
            TimeUnit.SECONDS);

    public E3DCDiscoveryService() {
        super(E3DCBridgeHandler.class,
                Set.of(E3DCBindingConstants.THING_TYPE_POWER_PLANT, E3DCBindingConstants.THING_TYPE_BATTERY,
                        E3DCBindingConstants.THING_TYPE_BATTERY_MODULE, E3DCBindingConstants.THING_TYPE_POWER_METER,
                        E3DCBindingConstants.THING_TYPE_WALLBOX),
                TIMEOUT_SECONDS, false);
    }

    @Override
    protected void startScan() {
        scan();
    }

    @Override
    protected synchronized void startBackgroundDiscovery() {
        thingHandler.addOnlineListener(onlineScan);
        ScheduledFuture<?> running = backgroundScan;
        if (running == null || running.isCancelled()) {
            backgroundScan = scheduler.scheduleWithFixedDelay(this::scan, FIRST_SCAN_DELAY_SECONDS,
                    SCAN_INTERVAL_SECONDS, TimeUnit.SECONDS);
        }
    }

    @Override
    protected synchronized void stopBackgroundDiscovery() {
        thingHandler.removeOnlineListener(onlineScan);
        ScheduledFuture<?> running = backgroundScan;
        if (running != null) {
            running.cancel(true);
            backgroundScan = null;
        }
    }

    private synchronized void scan() {
        E3DCBridgeHandler bridge = thingHandler;
        if (!bridge.isConnected()) {
            logger.debug("The device is not connected, so there is nothing to discover yet");
            return;
        }
        ThingUID bridgeUid = bridge.getThing().getUID();
        long started = System.nanoTime();
        logger.debug("Discovery scan of {} started", bridgeUid);
        thingDiscovered(E3DCDiscovery.powerPlant(bridgeUid));
        int batteries = scanBatteries(bridge, bridgeUid);
        int meters = probe(bridge, bridgeUid, E3DCBindingConstants.GROUP_METER, E3DCDiscovery.MAX_METERS,
                E3DCDiscovery::isMeterAnswered, E3DCDiscovery::meter);
        int wallboxes = probe(bridge, bridgeUid, E3DCBindingConstants.GROUP_WALLBOX, E3DCDiscovery.MAX_WALLBOXES,
                E3DCDiscovery::isWallboxAnswered, E3DCDiscovery::wallbox);
        logger.debug(
                "Discovery scan of {} finished after {} ms: 1 power plant, {} batteries, {} power meters, {} wallboxes",
                bridgeUid, (System.nanoTime() - started) / 1_000_000, batteries, meters, wallboxes);
    }

    private int scanBatteries(E3DCBridgeHandler bridge, ThingUID bridgeUid) {
        RscpValue available = null;
        try {
            available = bridge.sendRequest(List.of(RscpValue.none(RscpTag.BAT_REQ_AVAILABLE_BATTERIES)));
            logAnswer("Answer of the battery query", available);
        } catch (RscpException e) {
            logger.debug("The batteries could not be queried, battery 0 is tried: {}", e.getMessage());
        }
        E3DCContainerRequests.Container request = E3DCContainerRequests.forGroup(E3DCBindingConstants.GROUP_MODULE);
        if (request == null) {
            return 0;
        }
        List<Integer> candidates = E3DCDiscovery.batteryCandidates(available);
        logger.debug("Batteries to ask for: {}", candidates);
        int proposed = 0;
        TreeSet<Integer> modules = new TreeSet<>();
        for (int battery : candidates) {
            try {
                RscpValue answer = bridge.sendRequest(List.of(request.request(battery, Set.of())));
                if (!E3DCDiscovery.isBatteryAnswered(answer)) {
                    logger.debug("The device reports no data for battery {}, it is not proposed", battery);
                    logAnswer("Answer for battery " + battery, answer);
                    continue;
                }
                logAnswer("Answer for battery " + battery, answer);
                thingDiscovered(E3DCDiscovery.battery(bridgeUid, battery));
                proposed++;
                List<Integer> found = modulesOf(bridge, request, battery, answer);
                logger.debug("Battery {} reports {} cell modules: {}", battery, found.size(), found);
                modules.addAll(found);
            } catch (RscpException e) {
                logger.debug("Battery {} could not be queried: {}", battery, e.getMessage());
            }
        }
        for (int module : modules) {
            thingDiscovered(E3DCDiscovery.module(bridgeUid, module));
        }
        return proposed;
    }

    /**
     * The modules of one battery. The device answers a module only when it is asked for by its index, so the
     * count the battery reports decides which modules are requested.
     */
    private List<Integer> modulesOf(E3DCBridgeHandler bridge, E3DCContainerRequests.Container request, int battery,
            RscpValue batteryAnswer) throws RscpException {
        Number count = batteryAnswer.findNumber(RscpTag.BAT_DCB_COUNT);
        // without a count the first module is asked for, so a battery is never proposed without any module
        int modules = count == null || count.intValue() <= 0 ? 1
                : Math.min(count.intValue(), E3DCDiscovery.MAX_MODULES);
        List<Integer> wanted = IntStream.range(0, modules).boxed().toList();
        RscpValue answer = bridge.sendRequest(List.of(request.request(battery, Set.of(), wanted)));
        logAnswer("Answer for the modules of battery " + battery, answer);
        return E3DCDiscovery.moduleIndexes(answer);
    }

    /**
     * Asks for the indexes 0 to {@code max - 1} of one kind of device and proposes those that answer with values.
     * The device reports no count for these, so the answers are the only information. They are logged at DEBUG
     * level for every index, which shows how the device answers for an index it does not have.
     */
    @SuppressWarnings("null") // the JDK functional interfaces carry no null annotations
    private int probe(E3DCBridgeHandler bridge, ThingUID bridgeUid, String groupId, int max,
            Predicate<RscpValue> answered, BiFunction<ThingUID, Integer, DiscoveryResult> result) {
        E3DCContainerRequests.Container request = E3DCContainerRequests.forGroup(groupId);
        if (request == null) {
            return 0;
        }
        int proposed = 0;
        for (int index = 0; index < max; index++) {
            try {
                RscpValue answer = bridge.sendRequest(List.of(request.request(index, Set.of())));
                boolean found = answered.test(answer);
                logger.debug("{} {}: {}", groupId, index,
                        found ? "answers with values, proposed" : "no values, not proposed");
                logAnswer("Answer for " + groupId + " " + index, answer);
                if (found) {
                    thingDiscovered(result.apply(bridgeUid, index));
                    proposed++;
                }
            } catch (RscpException e) {
                logger.debug("{} {} could not be queried, the remaining indexes are skipped: {}", groupId, index,
                        e.getMessage());
                break;
            }
        }
        return proposed;
    }

    /** The tree is only built when the log is read at DEBUG level, because an answer can be long. */
    private void logAnswer(String what, RscpValue answer) {
        if (logger.isDebugEnabled()) {
            logger.debug("{}:{}{}", what, System.lineSeparator(), answer.describeTree());
        }
    }
}
