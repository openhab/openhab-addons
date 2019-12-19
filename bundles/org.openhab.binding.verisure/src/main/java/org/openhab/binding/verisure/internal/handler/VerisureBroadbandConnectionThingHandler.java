/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
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
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.openhab.binding.verisure.internal.model.VerisureBroadbandConnectionsJSON;
import org.openhab.binding.verisure.internal.model.VerisureThingJSON;

/**
 * Handler for the Broadband COnnection thing type that Verisure provides.
 *
 * @author Jan Gustafsson - Initial contribution
 *
 */
@NonNullByDefault
public class VerisureBroadbandConnectionThingHandler extends VerisureThingHandler {

    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPES = Collections
            .singleton(THING_TYPE_BROADBAND_CONNECTION);

    public VerisureBroadbandConnectionThingHandler(Thing thing) {
        super(thing);
    }

    @Override
    public synchronized void update(@Nullable VerisureThingJSON thing) {
        logger.debug("update on thing: {}", thing);
        updateStatus(ThingStatus.ONLINE);
        if (getThing().getThingTypeUID().equals(THING_TYPE_BROADBAND_CONNECTION)) {
            VerisureBroadbandConnectionsJSON obj = (VerisureBroadbandConnectionsJSON) thing;
            if (obj != null) {
                updateBroadbandConnection(obj);
            }
        } else {
            logger.warn("Can't handle this thing typeuid: {}", getThing().getThingTypeUID());
        }
    }

    private void updateBroadbandConnection(VerisureBroadbandConnectionsJSON vbcJSON) {
        updateTimeStamp(vbcJSON.getData().getInstallation().getBroadband().getTestDate());
        ChannelUID cuid = new ChannelUID(getThing().getUID(), CHANNEL_CONNECTED);
        updateState(cuid,
                new StringType(vbcJSON.getData().getInstallation().getBroadband().isBroadbandConnected().toString()));
        super.update(vbcJSON);
    }

}
