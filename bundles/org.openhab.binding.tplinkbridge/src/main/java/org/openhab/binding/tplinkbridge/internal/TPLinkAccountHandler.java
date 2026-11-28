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
package org.openhab.binding.tplinkbridge.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.tplinkbridge.TPLinkAccount;
import org.openhab.binding.tplinkbridge.TPLinkCredentials;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.BaseBridgeHandler;
import org.openhab.core.types.Command;

/**
 * Holds the credentials of a TP-Link account for the Kasa and Tapo Things attached to it.
 *
 * <p>
 * The bridge goes ONLINE once both credentials are set. It does not contact the TP-Link cloud, so the
 * credentials are only checked when a device accepts or rejects them.
 *
 * @author Christopher Smith - Initial contribution
 */
@NonNullByDefault
public class TPLinkAccountHandler extends BaseBridgeHandler implements TPLinkAccount {

    private volatile TPLinkCredentials credentials = new TPLinkCredentials("", "");

    public TPLinkAccountHandler(Bridge bridge) {
        super(bridge);
    }

    @Override
    public void initialize() {
        TPLinkAccountConfiguration config = getConfigAs(TPLinkAccountConfiguration.class);
        credentials = new TPLinkCredentials(config.username.trim(), config.password);
        if (credentials.areSet()) {
            updateStatus(ThingStatus.ONLINE);
        } else {
            updateStatus(ThingStatusDetail.CONFIGURATION_ERROR, "@text/offline.conf-error-credentials");
        }
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        // the account bridge has no channels
    }

    @Override
    public TPLinkCredentials getCredentials() {
        return credentials;
    }
}
