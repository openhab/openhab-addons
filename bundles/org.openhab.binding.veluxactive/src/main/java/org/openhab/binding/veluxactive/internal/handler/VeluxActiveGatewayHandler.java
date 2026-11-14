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
package org.openhab.binding.veluxactive.internal.handler;

import static org.openhab.binding.veluxactive.internal.VeluxActiveBindingConstants.*;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.time.DateTimeException;
import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.veluxactive.internal.ModuleRequestContext;
import org.openhab.binding.veluxactive.internal.action.VeluxActivePairingActions;
import org.openhab.binding.veluxactive.internal.api.ModuleInfo;
import org.openhab.binding.veluxactive.internal.api.dto.HomeStatus;
import org.openhab.binding.veluxactive.internal.api.dto.Module;
import org.openhab.binding.veluxactive.internal.api.dto.ModuleError;
import org.openhab.binding.veluxactive.internal.config.VeluxActiveModuleConfiguration;
import org.openhab.binding.veluxactive.internal.crypto.NetcomPairing;
import org.openhab.binding.veluxactive.internal.crypto.SigningKey;
import org.openhab.binding.veluxactive.internal.crypto.SigningKeyStore;
import org.openhab.binding.veluxactive.internal.discovery.VeluxActiveGatewayDiscoveryService;
import org.openhab.binding.veluxactive.internal.exception.ApiException;
import org.openhab.core.common.ThreadPoolManager;
import org.openhab.core.library.types.DateTimeType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.ThingStatusInfo;
import org.openhab.core.thing.binding.BridgeHandler;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.thing.binding.ThingHandlerService;
import org.openhab.core.thing.binding.builder.BridgeBuilder;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.openhab.core.types.UnDefType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Publishes gateway state and manages pairing and departure commands.
 *
 * @author Jacob Laursen - Initial contribution
 */
@NonNullByDefault
public class VeluxActiveGatewayHandler extends VeluxActiveModuleHandler implements BridgeHandler {

    private final static int MINIMUM_FIRMWARE_VERSION_REQUIRED_FOR_SIGNING = 27;
    private final static int MINIMUM_FIRMWARE_VERSION_REQUIRED_FOR_SIGNING_WITH_NONCE = 60;

    private final Logger logger = LoggerFactory.getLogger(VeluxActiveGatewayHandler.class);
    private final SigningKeyStore signingKeys;

    private volatile @Nullable Integer firmwareVersion;
    private @Nullable NetcomPairing pairing;
    private @Nullable ScheduledFuture<?> pairingTask;
    private @Nullable ScheduledFuture<?> pairingTimeout;
    private String pairingResult = "IDLE";
    private boolean lockPending;

    public VeluxActiveGatewayHandler(Bridge thing, SigningKeyStore signingKeys) {
        super(thing);
        this.signingKeys = signingKeys;
    }

    @Override
    public void initialize() {
        synchronized (this) {
            finishPairing("CANCELLED");
            firmwareVersion = null;
            lockPending = false;
            pairingResult = "IDLE";
        }
        super.initialize();
    }

    public String pair(String host) {
        ModuleRequestContext requestContext = captureRequestContext();
        VeluxActiveAccountHandler account = account();
        NetcomPairing attempt;
        synchronized (this) {
            if (pairing != null) {
                return "IN_PROGRESS";
            }
            if (requestContext == null || account == null || requestContext.homeId().isBlank()
                    || !isRequestContextCurrent(requestContext) || getThing().getStatus() != ThingStatus.ONLINE) {
                return "UNAVAILABLE: Gateway must be online with a resolved home.";
            }
            Integer firmware = firmwareVersion;
            if (firmware == null || firmware < MINIMUM_FIRMWARE_VERSION_REQUIRED_FOR_SIGNING) {
                return "UNAVAILABLE: Signing firmware support is unknown or unavailable.";
            }
            try {
                attempt = new NetcomPairing(host.trim());
            } catch (IOException e) {
                return "INVALID_ADDRESS: Supply the gateway's LAN IP address.";
            }
            pairing = attempt;
            pairingResult = "STARTING";
            pairingTimeout = scheduler.schedule(() -> {
                synchronized (this) {
                    if (attempt.equals(pairing)) {
                        finishPairing("TIMED_OUT: Press the gateway button and try again.");
                    }
                }
            }, 60, TimeUnit.SECONDS);
        }
        account.retrieveKey(requestContext, () -> {
            synchronized (this) {
                if (attempt.equals(pairing) && isRequestContextCurrent(requestContext)) {
                    pairingResult = "WAITING_FOR_BUTTON";
                    schedulePairing(requestContext, attempt, 0);
                }
            }
        }, () -> {
            synchronized (this) {
                if (attempt.equals(pairing)) {
                    finishPairing("FAILED: Cloud key retrieval was not accepted.");
                }
            }
        });
        return "STARTED: Press the gateway button, then check pairingStatus().";
    }

    public synchronized String pairingStatus() {
        ModuleRequestContext requestContext = captureRequestContext();
        if ("IDLE".equals(pairingResult) && requestContext != null && signingKeys.contains(requestContext.moduleId())) {
            return "PAIRED";
        }
        return pairingResult;
    }

    private void schedulePairing(ModuleRequestContext requestContext, NetcomPairing attempt, int delay) {
        pairingTask = ThreadPoolManager.getScheduledPool(BINDING_ID + "-pairing").schedule(() -> {
            try {
                SigningKey key = attempt.attempt();
                synchronized (this) {
                    if (!attempt.equals(pairing)) {
                        return;
                    }
                    if (!isRequestContextCurrent(requestContext) || getThing().getStatus() != ThingStatus.ONLINE) {
                        finishPairing("CANCELLED");
                        return;
                    }
                    if (key != null) {
                        signingKeys.put(requestContext.moduleId(), key);
                        finishPairing("PAIRED");
                    } else if (attempt.expired()) {
                        finishPairing("TIMED_OUT");
                    } else {
                        schedulePairing(requestContext, attempt, 1);
                    }
                }
            } catch (IOException | GeneralSecurityException e) {
                synchronized (this) {
                    if (attempt.equals(pairing)) {
                        finishPairing(
                                "FAILED: Local gateway pairing failed. Check the address and button, then try again.");
                    }
                }
            } catch (RuntimeException e) {
                synchronized (this) {
                    if (attempt.equals(pairing)) {
                        finishPairing("FAILED: Could not retain gateway credentials.");
                    }
                }
                logger.debug("Gateway pairing failed unexpectedly ({})", e.getClass().getSimpleName());
            }
        }, delay, TimeUnit.SECONDS);
    }

    private void finishPairing(String result) {
        NetcomPairing attempt = pairing;
        pairing = null;
        ScheduledFuture<?> task = pairingTask;
        pairingTask = null;
        if (task != null) {
            task.cancel(false);
        }
        ScheduledFuture<?> timeout = pairingTimeout;
        pairingTimeout = null;
        if (timeout != null) {
            timeout.cancel(false);
        }
        if (attempt != null) {
            attempt.close();
            pairingResult = result;
        }
    }

    boolean canSign() {
        ModuleRequestContext requestContext = captureRequestContext();
        Integer firmware = firmwareVersion;
        return requestContext != null && getThing().getStatus() == ThingStatus.ONLINE && firmware != null
                && firmware >= MINIMUM_FIRMWARE_VERSION_REQUIRED_FOR_SIGNING
                && signingKeys.contains(requestContext.moduleId());
    }

    Map<String, Object> sign(ModuleRequestContext requestContext, String moduleId, String field, Object value,
            long serverTime) throws ApiException {
        Integer firmware = firmwareVersion;
        if (!isRequestContextCurrent(requestContext) || firmware == null
                || firmware < MINIMUM_FIRMWARE_VERSION_REQUIRED_FOR_SIGNING) {
            throw new ApiException("Gateway signing support is unavailable.");
        }
        return signingKeys.sign(requestContext.moduleId(), moduleId, field, value, serverTime,
                firmware >= MINIMUM_FIRMWARE_VERSION_REQUIRED_FOR_SIGNING_WITH_NONCE);
    }

    @Override
    public void bridgeStatusChanged(ThingStatusInfo status) {
        synchronized (this) {
            lockPending = false;
            finishPairing("CANCELLED");
        }
        super.bridgeStatusChanged(status);
    }

    @Override
    public synchronized void dispose() {
        finishPairing("CANCELLED");
        super.dispose();
    }

    @Override
    public synchronized void handleRemoval() {
        finishPairing("CANCELLED");
        String moduleId = getConfigAs(VeluxActiveModuleConfiguration.class).moduleId;
        if (!moduleId.isBlank()) {
            signingKeys.removeIfUnowned(moduleId, getThing().getUID());
        }
        super.handleRemoval();
    }

    @Override
    public Bridge getThing() {
        return (Bridge) super.getThing();
    }

    @Override
    protected BridgeBuilder editThing() {
        return BridgeBuilder.create(thing.getThingTypeUID(), thing.getUID()).withBridge(thing.getBridgeUID())
                .withChannels(thing.getChannels()).withConfiguration(thing.getConfiguration())
                .withLabel(thing.getLabel()).withLocation(thing.getLocation()).withProperties(thing.getProperties())
                .withSemanticEquipmentTag(thing.getSemanticEquipmentTag());
    }

    @Override
    public Collection<Class<? extends ThingHandlerService>> getServices() {
        return Set.of(VeluxActiveGatewayDiscoveryService.class, VeluxActivePairingActions.class);
    }

    public void discover(Consumer<List<ModuleInfo>> consumer) {
        ModuleRequestContext requestContext = captureRequestContext();
        VeluxActiveAccountHandler account = account();
        if (requestContext != null && account != null) {
            account.discover(modules -> {
                if (isRequestContextCurrent(requestContext)) {
                    consumer.accept(modules.stream().filter(module -> requestContext.homeId().equals(module.homeId())
                            && requestContext.moduleId().equals(module.bridgeId())).toList());
                }
            });
        }
    }

    @Override
    public void childHandlerInitialized(ThingHandler childHandler, Thing childThing) {
        VeluxActiveAccountHandler account = account();
        if (account != null) {
            account.requestPoll();
        }
    }

    @Override
    public void childHandlerDisposed(ThingHandler childHandler, Thing childThing) {
        VeluxActiveAccountHandler account = account();
        if (account != null) {
            account.requestPoll();
        }
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        ModuleRequestContext requestContext = captureRequestContext();
        if (requestContext == null) {
            return;
        }
        VeluxActiveAccountHandler account = account();
        if (account == null) {
            return;
        }
        if (command == RefreshType.REFRESH) {
            account.requestPoll();
        } else if (CHANNEL_LOCKED.equals(channelUID.getId())) {
            if (command == OnOffType.ON) {
                account.lock(requestContext);
            } else if (command == OnOffType.OFF) {
                if (canSign()) {
                    account.unlock(requestContext);
                } else {
                    logger.warn("Pair the gateway before unlocking {}", getThing().getUID());
                }
            }
        }
    }

    synchronized void homeResolved(boolean homeChanged) {
        ModuleRequestContext requestContext = captureRequestContext();
        if (requestContext == null || !isRequestContextCurrent(requestContext)) {
            return;
        }
        if (requestContext.homeId().isBlank()) {
            clearState();
            updateStatus(ThingStatusDetail.OFFLINE.COMMUNICATION_ERROR,
                    "@text/offline.comm-error.gateway.home-unresolved");
        } else if (homeChanged) {
            finishPairing("CANCELLED");
            clearState();
        }
    }

    synchronized void update(ModuleRequestContext requestContext, HomeStatus status) {
        if (!isRequestContextCurrent(requestContext)) {
            return;
        }
        ModuleError error = status.error(requestContext.moduleId());
        Module module = status.module(requestContext.moduleId());
        if (error != null || module != null && Boolean.FALSE.equals(module.reachable)) {
            finishPairing("CANCELLED");
            clearState();
            updateStatus(ThingStatusDetail.OFFLINE.COMMUNICATION_ERROR,
                    error != null && !Integer.valueOf(6).equals(error.code)
                            ? "@text/offline.comm-error.gateway.status-error"
                            : "@text/offline.comm-error.gateway.unreachable");
        } else if (module == null) {
            missingStatus("@text/status.gateway.missing");
        } else {
            Map<String, String> properties = editProperties();
            Integer firmware = module.firmwareRevisionNetatmo;
            Integer hardware = module.hardwareVersion;
            if (firmware != null) {
                properties.put(Thing.PROPERTY_FIRMWARE_VERSION, firmware.toString());
                firmwareVersion = firmware;
            }
            if (hardware != null) {
                properties.put(Thing.PROPERTY_HARDWARE_VERSION, hardware.toString());
            }
            updateProperties(properties);
            Boolean locked = module.locked;
            Boolean locking = module.locking;
            // During locking, the gateway still reports the previous unlocked state.
            if (!(lockPending && Boolean.TRUE.equals(locking) && Boolean.FALSE.equals(locked))) {
                lockPending = false;
                updateState(CHANNEL_LOCKED, locked == null ? UnDefType.UNDEF : OnOffType.from(locked));
            }
            Long lastSeen = module.lastSeen;
            try {
                updateState(CHANNEL_LAST_SEEN,
                        lastSeen == null ? UnDefType.UNDEF : new DateTimeType(Instant.ofEpochSecond(lastSeen)));
            } catch (DateTimeException e) {
                updateState(CHANNEL_LAST_SEEN, UnDefType.UNDEF);
            }
            updateStatus(ThingStatus.ONLINE);
        }
    }

    synchronized void lockAccepted(ModuleRequestContext requestContext) {
        if (isRequestContextCurrent(requestContext)) {
            lockPending = true;
            updateState(CHANNEL_LOCKED, OnOffType.ON);
        }
    }

    @Override
    synchronized void failed(ModuleRequestContext requestContext, @Nullable String message) {
        if (isRequestContextCurrent(requestContext)) {
            finishPairing("CANCELLED");
        }
        super.failed(requestContext, message);
    }

    synchronized void unlockAccepted(ModuleRequestContext requestContext) {
        if (isRequestContextCurrent(requestContext)) {
            lockPending = false;
            updateState(CHANNEL_LOCKED, OnOffType.OFF);
        }
    }

    @Override
    protected void clearState() {
        lockPending = false;
        updateState(CHANNEL_LOCKED, UnDefType.UNDEF);
        updateState(CHANNEL_LAST_SEEN, UnDefType.UNDEF);
    }
}
