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
package org.openhab.binding.plivo.internal.discovery;

import static org.openhab.binding.plivo.internal.PlivoBindingConstants.*;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.plivo.internal.api.PlivoApiClient;
import org.openhab.binding.plivo.internal.api.PlivoApiException;
import org.openhab.binding.plivo.internal.api.PlivoPhoneNumberInfo;
import org.openhab.binding.plivo.internal.handler.PlivoAccountHandler;
import org.openhab.core.config.discovery.AbstractThingHandlerDiscoveryService;
import org.openhab.core.config.discovery.DiscoveryResult;
import org.openhab.core.config.discovery.DiscoveryResultBuilder;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.ThingUID;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ServiceScope;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link PlivoPhoneDiscoveryService} discovers Plivo phone numbers
 * associated with the account and adds them to the inbox.
 *
 * @author Sarvesh Patil - Initial contribution
 */
@Component(scope = ServiceScope.PROTOTYPE, service = PlivoPhoneDiscoveryService.class)
@NonNullByDefault
public class PlivoPhoneDiscoveryService extends AbstractThingHandlerDiscoveryService<PlivoAccountHandler> {

    private static final int TIMEOUT_SECONDS = 10;

    private final Logger logger = LoggerFactory.getLogger(PlivoPhoneDiscoveryService.class);

    public PlivoPhoneDiscoveryService() {
        super(PlivoAccountHandler.class, Set.of(THING_TYPE_PHONE), TIMEOUT_SECONDS, true);
    }

    @Override
    public Set<ThingTypeUID> getSupportedThingTypes() {
        return Set.of(THING_TYPE_PHONE);
    }

    @Override
    protected void startBackgroundDiscovery() {
        startScan();
    }

    @Override
    protected void startScan() {
        PlivoApiClient client = thingHandler.getApiClient();
        if (client == null) {
            logger.debug("Cannot discover phone numbers: API client not available");
            return;
        }

        try {
            List<PlivoPhoneNumberInfo> phoneNumbers = client.listPhoneNumbers();
            ThingUID bridgeUID = thingHandler.getThing().getUID();

            for (PlivoPhoneNumberInfo info : phoneNumbers) {
                String thingId = info.number.replaceAll("[^a-zA-Z0-9]", "");
                ThingUID thingUID = new ThingUID(THING_TYPE_PHONE, bridgeUID, thingId);

                String label = "Plivo: " + (info.alias.isEmpty() ? info.number : info.alias);

                DiscoveryResult result = DiscoveryResultBuilder.create(thingUID) //
                        .withBridge(bridgeUID) //
                        .withProperties(Map.of("phoneNumber", "+" + info.number)) //
                        .withRepresentationProperty("phoneNumber") //
                        .withLabel(label) //
                        .build();

                thingDiscovered(result);
            }

            logger.debug("Discovered {} Plivo phone numbers", phoneNumbers.size());
        } catch (PlivoApiException e) {
            logger.warn("Could not discover Plivo phone numbers: {}", e.getMessage());
        } catch (RuntimeException e) {
            logger.warn("Unexpected error discovering Plivo phone numbers", e);
        }
    }

    @Override
    protected synchronized void stopScan() {
        super.stopScan();
        removeOlderResults(getTimestampOfLastScan());
    }

    @Override
    public void dispose() {
        super.dispose();
        removeOlderResults(Instant.now());
    }
}
