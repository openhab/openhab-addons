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
package org.openhab.binding.dsmr.internal.discovery;

import java.util.Collection;
import java.util.List;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.dsmr.internal.device.connector.DSMRErrorStatus;
import org.openhab.binding.dsmr.internal.device.cosem.CosemObject;
import org.openhab.binding.dsmr.internal.device.cosem.CosemObjectType;
import org.openhab.binding.dsmr.internal.device.p1telegram.P1Telegram;
import org.openhab.binding.dsmr.internal.device.p1telegram.P1TelegramListener;
import org.openhab.binding.dsmr.internal.handler.DSMRBridgeHandler;
import org.openhab.binding.dsmr.internal.handler.DSMRMeterHandler;
import org.openhab.binding.dsmr.internal.meter.DSMRMeterDescriptor;
import org.openhab.binding.dsmr.internal.meter.DSMRMeterType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.thing.binding.ThingHandlerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This implements the discovery service for new DSMR Meters on an active DSMR bridge.
 *
 * @author M. Volaart - Initial contribution
 * @author Hilbrand Bouwkamp - Refactored code to detect meters during actual discovery phase.
 */
@NonNullByDefault
public class DSMRMeterDiscoveryService extends DSMRDiscoveryService implements P1TelegramListener, ThingHandlerService {

    private final Logger logger = LoggerFactory.getLogger(DSMRMeterDiscoveryService.class);

    /**
     * The {@link DSMRBridgeHandler} instance
     */
    private @NonNullByDefault({}) DSMRBridgeHandler dsmrBridgeHandler;

    /**
     * Constructs a new {@link DSMRMeterDiscoveryService} attached to the give bridge handler.
     *
     * @param dsmrBridgeHandler The bridge handler this discovery service is attached to
     */
    public DSMRMeterDiscoveryService() {
        super();
        this.i18nProvider = DSMRI18nProviderTracker.i18nProvider;
        this.localeProvider = DSMRI18nProviderTracker.localeProvider;
    }

    @Override
    public void deactivate() {
        super.deactivate();
    }

    @Override
    public @Nullable ThingHandler getThingHandler() {
        return dsmrBridgeHandler;
    }

    @Override
    public void setThingHandler(final ThingHandler handler) {
        if (handler instanceof DSMRBridgeHandler) {
            dsmrBridgeHandler = (DSMRBridgeHandler) handler;
        }
    }

    @Override
    protected void startScan() {
        logger.debug("Start discovery on existing DSMR bridge.");
        dsmrBridgeHandler.setLenientMode(true);
        dsmrBridgeHandler.registerDSMRMeterListener(this);
    }

    @Override
    protected synchronized void stopScan() {
        logger.debug("Stop discovery on existing DSMR bridge.");
        dsmrBridgeHandler.setLenientMode(false);
        super.stopScan();
        dsmrBridgeHandler.unregisterDSMRMeterListener(this);
    }

    @Override
    public void telegramReceived(final P1Telegram telegram) {
        if (logger.isDebugEnabled()) {
            logger.debug("Detect meters from #{} objects", telegram.getCosemObjects().size());
        }
        final Entry<Collection<DSMRMeterDescriptor>, List<CosemObject>> detectedMeters = meterDetector
                .detectMeters(telegram);
        verifyUnregisteredCosemObjects(telegram, detectedMeters.getValue());
        validateConfiguredMeters(dsmrBridgeHandler.getThing().getThings(),
                detectedMeters.getKey().stream().map(md -> md.getMeterType()).collect(Collectors.toSet()));
        detectedMeters.getKey().forEach(m -> meterDiscovered(m, dsmrBridgeHandler.getThing().getUID()));
    }

    @Override
    public void onError(final DSMRErrorStatus state, final String message) {
        logger.info("Telegram could not be parsed correctly, failed with state: {}, {}", state, message);
    }

    protected void verifyUnregisteredCosemObjects(final P1Telegram telegram, final List<CosemObject> list) {
        if (!list.isEmpty()) {
            if (list.stream()
                    .anyMatch(e -> e.getType() == CosemObjectType.METER_EQUIPMENT_IDENTIFIER
                            && e.getCosemValues().entrySet().stream().anyMatch(
                                    cv -> cv.getValue() instanceof StringType && cv.getValue().toString().isEmpty()))) {
                // Unregistered meter detected. log to the user.
                reportUnregisteredMeters();
            } else {
                reportUnrecognizedCosemObjects(list);
                logger.info("There are unrecognized cosem values in the data received from the meter,"
                        + " which means some meters might not be detected. Please report your raw data as reference: {}",
                        telegram.getRawTelegram());
            }
        }
        if (!telegram.getUnknownCosemObjects().isEmpty()) {
            logger.info("There are unrecognized cosem values in the data received from the meter,"
                    + " which means you have values that can't be read by a channel: {}. Please report them and your raw data as reference: {}",
                    telegram.getUnknownCosemObjects().stream()
                            .map(e -> String.format("obis id:%s, value:%s", e.getKey(), e.getValue()))
                            .collect(Collectors.joining(", ")),
                    telegram.getRawTelegram());
        }
    }

    /**
     * Called when Unrecognized cosem objects where found. This can be a bug or a new meter not yet supported.
     *
     * @param list Map with the unrecognized.
     */
    protected void reportUnrecognizedCosemObjects(final List<CosemObject> list) {
        list.forEach(c -> logger.info("Unrecognized cosem object '{}' found in the data: {}", c.getType(), c));
    }

    /**
     * Called when a meter equipment identifier is found that has an empty value. This
     */
    protected void reportUnregisteredMeters() {
        logger.info(
                "An unregistered meter has been found. Probably a new meter. Retry discovery once the meter is registered with the energy provider.");
    }

    /**
     * Validates if the meters configured by the user match with what is detected in the telegram. Some meters are a
     * subset of other meters and therefore an invalid configured meter does work, but not all available data is
     * available to the user.
     *
     * @param things The list of configured things
     * @param configuredMeterTypes The set of meters detected in the telegram
     */
    private void validateConfiguredMeters(final List<Thing> things, final Set<DSMRMeterType> configuredMeterTypes) {
        // @formatter:off
        final Set<DSMRMeterType> configuredMeters = things.stream()
                .map(Thing::getHandler)
                .filter(DSMRMeterHandler.class::isInstance)
                .map(DSMRMeterHandler.class::cast)
                .map(DSMRMeterHandler::getMeterDescriptor)
                .filter(Objects::nonNull)
                .map(d -> d.getMeterType())
                .collect(Collectors.toSet());
        // @formatter:on
        // Create list of all configured meters that are not in the detected list. If not empty meters might not be
        // correctly configured.
        final List<DSMRMeterType> invalidConfigured = configuredMeters.stream()
                .filter(dm -> !configuredMeterTypes.contains(dm)).collect(Collectors.toList());
        // Create a list of all detected meters not yet configured.
        final List<DSMRMeterType> unconfiguredMeters = configuredMeterTypes.stream()
                .filter(dm -> !configuredMeters.contains(dm)).collect(Collectors.toList());

        if (!invalidConfigured.isEmpty()) {
            reportConfigurationValidationResults(invalidConfigured, unconfiguredMeters);
        }
    }

    /**
     * Called when the validation finds in inconsistency between configured meters.
     *
     * @param invalidConfigured The list of invalid configured meters
     * @param unconfiguredMeters The list of meters that were detected, but not configured
     */
    protected void reportConfigurationValidationResults(final List<DSMRMeterType> invalidConfigured,
            final List<DSMRMeterType> unconfiguredMeters) {
        logger.info(
                "Possible incorrect meters configured. These are configured: {}."
                        + "But the following unconfigured meters are found in the data received from the meter: {}",
                invalidConfigured.stream().map(m -> m.name()).collect(Collectors.joining(", ")),
                unconfiguredMeters.stream().map(m -> m.name()).collect(Collectors.joining(", ")));
    }
}
