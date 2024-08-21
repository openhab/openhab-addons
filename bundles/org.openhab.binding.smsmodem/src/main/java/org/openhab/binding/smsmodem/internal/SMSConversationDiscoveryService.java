/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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
package org.openhab.binding.smsmodem.internal;

import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.smsmodem.internal.handler.SMSConversationHandler;
import org.openhab.binding.smsmodem.internal.handler.SMSModemBridgeHandler;
import org.openhab.core.config.discovery.AbstractThingHandlerDiscoveryService;
import org.openhab.core.config.discovery.DiscoveryResult;
import org.openhab.core.config.discovery.DiscoveryResultBuilder;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.ThingUID;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ServiceScope;

/**
 * This class implements a discovery service for SMSConversation
 *
 * @author Gwendal ROULLEAU - Initial contribution
 */
@Component(scope = ServiceScope.PROTOTYPE, service = SMSConversationDiscoveryService.class)
@NonNullByDefault
public class SMSConversationDiscoveryService extends AbstractThingHandlerDiscoveryService<SMSModemBridgeHandler> {

    private @NonNullByDefault({}) ThingUID bridgeUid;

    public SMSConversationDiscoveryService() {
        this(0);
    }

    public SMSConversationDiscoveryService(int timeout) throws IllegalArgumentException {
        super(SMSModemBridgeHandler.class, timeout);
    }

    @Override
    protected void startScan() {
        for (String msisdn : thingHandler.getAllSender()) {
            buildDiscovery(msisdn);
        }
    }

    public void buildDiscovery(String sender) {
        String senderSanitized = sender.replaceAll("[^a-zA-Z0-9+]", "_");

        ThingUID thingUID = new ThingUID(SMSModemBindingConstants.SMSCONVERSATION_THING_TYPE, senderSanitized,
                bridgeUid.getId());

        DiscoveryResult result = DiscoveryResultBuilder.create(thingUID)
                .withProperty(SMSModemBindingConstants.SMSCONVERSATION_PARAMETER_RECIPIENT, senderSanitized)
                .withLabel("Conversation with " + sender).withBridge(bridgeUid)
                .withThingType(SMSModemBindingConstants.SMSCONVERSATION_THING_TYPE)
                .withRepresentationProperty(SMSModemBindingConstants.SMSCONVERSATION_PARAMETER_RECIPIENT).build();
        thingDiscovered(result);
    }

    public void buildByAutoDiscovery(String sender) {
        if (isBackgroundDiscoveryEnabled()) {
            buildDiscovery(sender);
        }
    }

    @Override
    public Set<ThingTypeUID> getSupportedThingTypes() {
        return Set.of(SMSConversationHandler.SUPPORTED_THING_TYPES_UIDS);
    }

    @Override
    public void initialize() {
        bridgeUid = thingHandler.getThing().getUID();
        thingHandler.setDiscoveryService(this);
        super.initialize();
    }
}
