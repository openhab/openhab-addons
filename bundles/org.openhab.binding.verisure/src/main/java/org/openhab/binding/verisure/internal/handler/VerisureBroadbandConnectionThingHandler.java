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
package org.openhab.binding.verisure.internal.handler;

import static org.openhab.binding.verisure.internal.VerisureBindingConstants.*;

import java.util.Collections;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.openhab.binding.verisure.internal.dto.VerisureBroadbandConnectionsDTO;

/**
 * Handler for the Broadband COnnection thing type that Verisure provides.
 *
 * @author Jan Gustafsson - Initial contribution
 *
 */
@NonNullByDefault
public class VerisureBroadbandConnectionThingHandler extends VerisureThingHandler<VerisureBroadbandConnectionsDTO> {

    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPES = Collections
            .singleton(THING_TYPE_BROADBAND_CONNECTION);

    public VerisureBroadbandConnectionThingHandler(Thing thing) {
        super(thing);
    }

    @Override
    public Class<VerisureBroadbandConnectionsDTO> getVerisureThingClass() {
        return VerisureBroadbandConnectionsDTO.class;
    }

    @Override
    public synchronized void update(VerisureBroadbandConnectionsDTO thing) {
        updateBroadbandConnection(thing);
        updateStatus(ThingStatus.ONLINE);
    }

    private void updateBroadbandConnection(VerisureBroadbandConnectionsDTO vbcJSON) {
        String testDate = vbcJSON.getData().getInstallation().getBroadband().getTestDate();
        if (testDate != null) {
            updateTimeStamp(testDate);
            ChannelUID cuid = new ChannelUID(getThing().getUID(), CHANNEL_CONNECTED);
            boolean broadbandConnected = vbcJSON.getData().getInstallation().getBroadband().isBroadbandConnected();
            updateState(cuid, OnOffType.from(broadbandConnected));
            updateInstallationChannels(vbcJSON);
        }
    }

    @Override
    public void updateTriggerChannel(String event) {
    }
}
