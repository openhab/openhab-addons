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
package org.openhab.binding.twilio.internal.discovery;

import static org.openhab.binding.twilio.internal.TwilioBindingConstants.*;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.twilio.internal.api.TwilioApiClient;
import org.openhab.binding.twilio.internal.api.TwilioApiException;
import org.openhab.binding.twilio.internal.api.TwilioPhoneNumberInfo;
import org.openhab.binding.twilio.internal.handler.TwilioAccountHandler;
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
 * The {@link TwilioPhoneDiscoveryService} discovers Twilio phone numbers
 * associated with the account and adds them to the inbox.
 *
 * @author Dan Cunningham - Initial contribution
 */
@Component(scope = ServiceScope.PROTOTYPE, service = TwilioPhoneDiscoveryService.class)
@NonNullByDefault
public class TwilioPhoneDiscoveryService extends AbstractThingHandlerDiscoveryService<TwilioAccountHandler> {

    private static final int TIMEOUT_SECONDS = 10;

    private final Logger logger = LoggerFactory.getLogger(TwilioPhoneDiscoveryService.class);

    public TwilioPhoneDiscoveryService() {
        super(TwilioAccountHandler.class, Set.of(THING_TYPE_PHONE), TIMEOUT_SECONDS, false);
    }

    @Override
    public Set<ThingTypeUID> getSupportedThingTypes() {
        return Set.of(THING_TYPE_PHONE);
    }

    @Override
    protected void startScan() {
        TwilioApiClient client = thingHandler.getApiClient();
        if (client == null) {
            logger.debug("Cannot discover phone numbers: API client not available");
            return;
        }

        try {
            List<TwilioPhoneNumberInfo> phoneNumbers = client.listPhoneNumbers();
            ThingUID bridgeUID = thingHandler.getThing().getUID();

            for (TwilioPhoneNumberInfo info : phoneNumbers) {
                // Sanitize phone number for use as thing ID (remove + and other non-alphanumeric chars)
                String thingId = info.phoneNumber.replaceAll("[^a-zA-Z0-9]", "");
                ThingUID thingUID = new ThingUID(THING_TYPE_PHONE, bridgeUID, thingId);

                String label = "Twilio: " + (info.friendlyName.isEmpty() ? info.phoneNumber : info.friendlyName);

                DiscoveryResult result = DiscoveryResultBuilder.create(thingUID) //
                        .withBridge(bridgeUID) //
                        .withProperties(Map.of("phoneNumber", info.phoneNumber)) //
                        .withRepresentationProperty("phoneNumber") //
                        .withLabel(label) //
                        .build();

                thingDiscovered(result);
            }

            logger.debug("Discovered {} Twilio phone numbers", phoneNumbers.size());
        } catch (TwilioApiException e) {
            logger.debug("Failed to discover phone numbers: {}", e.getMessage());
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
