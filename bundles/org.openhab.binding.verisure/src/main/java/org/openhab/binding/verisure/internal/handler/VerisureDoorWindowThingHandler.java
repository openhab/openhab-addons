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

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.OpenClosedType;
import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.openhab.binding.verisure.internal.model.VerisureDoorWindowsJSON;
import org.openhab.binding.verisure.internal.model.VerisureThingJSON;

//import com.google.common.collect.Sets;

/**
 * Handler for the Smart Lock Device thing type that Verisure provides.
 *
 * @author Jan Gustafsson - Initial contribution
 *
 */
@NonNullByDefault
public class VerisureDoorWindowThingHandler extends VerisureThingHandler {

    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPES = new HashSet<ThingTypeUID>();
    static {
        SUPPORTED_THING_TYPES.add(THING_TYPE_DOORWINDOW);
    }

    public VerisureDoorWindowThingHandler(Thing thing) {
        super(thing);
    }

    @Override
    public synchronized void update(@Nullable VerisureThingJSON thing) {
        logger.debug("update on thing: {}", thing);
        if (thing != null) {
            updateStatus(ThingStatus.ONLINE);
            if (getThing().getThingTypeUID().equals(THING_TYPE_DOORWINDOW)) {
                VerisureDoorWindowsJSON obj = (VerisureDoorWindowsJSON) thing;
                updateDoorWindowState(obj);
            } else {
                logger.warn("Can't handle this thing typeuid: {}", getThing().getThingTypeUID());
            }
        } else {
            logger.warn("Thing JSON is null: {}", getThing().getThingTypeUID());
        }
    }

    private void updateDoorWindowState(VerisureDoorWindowsJSON doorWindowJSON) {
        ChannelUID cuid = new ChannelUID(getThing().getUID(), CHANNEL_STATE);
        if ("OPEN".equals(doorWindowJSON.getData().getInstallation().getDoorWindows().get(0).getState())) {
            updateState(cuid, OpenClosedType.OPEN);
        } else {
            updateState(cuid, OpenClosedType.CLOSED);
        }
        updateTimeStamp(doorWindowJSON.getData().getInstallation().getDoorWindows().get(0).getReportTime());
        cuid = new ChannelUID(getThing().getUID(), CHANNEL_LOCATION);
        updateState(cuid, new StringType(
                doorWindowJSON.getData().getInstallation().getDoorWindows().get(0).getDevice().getArea()));
        cuid = new ChannelUID(getThing().getUID(), CHANNEL_INSTALLATION_ID);
        BigDecimal siteId = doorWindowJSON.getSiteId();
        if (siteId != null) {
            updateState(cuid, new DecimalType(siteId.longValue()));
        }
        cuid = new ChannelUID(getThing().getUID(), CHANNEL_INSTALLATION_NAME);
        StringType instName = new StringType(doorWindowJSON.getSiteName());
        updateState(cuid, instName);
    }

}
