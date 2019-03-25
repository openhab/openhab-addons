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

import javax.measure.quantity.Temperature;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.QuantityType;
import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.library.unit.SIUnits;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.openhab.binding.verisure.internal.model.VerisureClimateBaseJSON;
import org.openhab.binding.verisure.internal.model.VerisureThingJSON;

/**
 * Handler for all Climate Device thing types that Verisure provides.
 *
 * @author Jan Gustafsson - Initial contribution
 *
 */
public class VerisureClimateDeviceThingHandler extends VerisureThingHandler {

    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPES = new HashSet<ThingTypeUID>();
    static {
        SUPPORTED_THING_TYPES.add(THING_TYPE_SMOKEDETECTOR);
        SUPPORTED_THING_TYPES.add(THING_TYPE_WATERDETETOR);
        SUPPORTED_THING_TYPES.add(THING_TYPE_SIREN);
        SUPPORTED_THING_TYPES.add(THING_TYPE_NIGHT_CONTROL);
    }

    public VerisureClimateDeviceThingHandler(@NonNull Thing thing) {
        super(thing);
    }

    @Override
    public synchronized void update(@Nullable VerisureThingJSON thing) {
        logger.debug("update on thing: {}", thing);
        updateStatus(ThingStatus.ONLINE);
        if (getThing().getThingTypeUID().equals(THING_TYPE_SMOKEDETECTOR)) {
            VerisureClimateBaseJSON obj = (VerisureClimateBaseJSON) thing;
            if (obj != null) {
                updateClimateDeviceState(obj);
            }
        } else if (getThing().getThingTypeUID().equals(THING_TYPE_WATERDETETOR)) {
            VerisureClimateBaseJSON obj = (VerisureClimateBaseJSON) thing;
            if (obj != null) {
                updateClimateDeviceState(obj);
            }
        } else if (getThing().getThingTypeUID().equals(THING_TYPE_NIGHT_CONTROL)) {
            VerisureClimateBaseJSON obj = (VerisureClimateBaseJSON) thing;
            if (obj != null) {
                updateClimateDeviceState(obj);
            }
        } else if (getThing().getThingTypeUID().equals(THING_TYPE_SIREN)) {
            VerisureClimateBaseJSON obj = (VerisureClimateBaseJSON) thing;
            if (obj != null) {
                updateClimateDeviceState(obj);
            }
        } else {
            logger.warn("Can't handle this thing typeuid: {}", getThing().getThingTypeUID());
        }
    }

    private void updateClimateDeviceState(VerisureClimateBaseJSON status) {
        ChannelUID cuid = new ChannelUID(getThing().getUID(), CHANNEL_TEMPERATURE);
        BigDecimal value = null;
        String temperature = status.getTemperature();
        if (temperature != null && temperature.length() > 1) {
            // Verisure temperature string contains HTML entity #176; for degree sign
            value = new BigDecimal(temperature.replace("&#176;", ""));
            updateState(cuid, new QuantityType<Temperature>(value, SIUnits.CELSIUS));
        }
        cuid = new ChannelUID(getThing().getUID(), CHANNEL_HUMIDITY);
        String humidity = status.getHumidity();
        if (humidity != null && humidity.length() > 1) {
            value = new BigDecimal(humidity.replace("%", ""));
            updateState(cuid, new DecimalType(value));
        }
        cuid = new ChannelUID(getThing().getUID(), CHANNEL_LASTUPDATE);
        updateState(cuid, new StringType(status.getTimestamp()));
        cuid = new ChannelUID(getThing().getUID(), CHANNEL_LOCATION);
        updateState(cuid, new StringType(status.getLocation()));
        cuid = new ChannelUID(getThing().getUID(), CHANNEL_SITE_INSTALLATION_ID);
        BigDecimal siteId = status.getSiteId();
        if (siteId != null) {
            updateState(cuid, new DecimalType(siteId.longValue()));
        }
        cuid = new ChannelUID(getThing().getUID(), CHANNEL_SITE_INSTALLATION_NAME);
        updateState(cuid, new StringType(status.getSiteName()));
    }
}
