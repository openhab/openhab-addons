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
package org.openhab.binding.freeboxos.internal.handler;

import static org.openhab.binding.freeboxos.internal.FreeboxOsBindingConstants.*;

import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.freeboxos.internal.api.FreeboxException;
import org.openhab.binding.freeboxos.internal.api.phone.PhoneConfig;
import org.openhab.binding.freeboxos.internal.api.phone.PhoneManager;
import org.openhab.binding.freeboxos.internal.api.phone.PhoneStatus;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.PercentType;
import org.openhab.core.thing.Thing;
import org.openhab.core.types.Command;

/**
 * The {@link DectHandler} is responsible for handling DECT specifics of the Telephony API
 *
 * @author Gaël L'hopital - Initial contribution
 */
@NonNullByDefault
public class DectHandler extends FxsHandler {

    public DectHandler(Thing thing) {
        super(thing);
    }

    @Override
    void initializeProperties(Map<String, String> properties) throws FreeboxException {
        super.initializeProperties(properties);
        getManager(PhoneManager.class).getOptStatus(getClientId()).ifPresent(status -> {
            String vendor = status.getVendor();
            if (vendor != null) {
                properties.put(Thing.PROPERTY_VENDOR, vendor);
            }
        });
    }

    @Override
    protected void updateConfig(PhoneConfig config) {
        super.updateConfig(config);
        updateChannelOnOff(DECT_ACTIVE, config.isEnabled());
        updateChannelOnOff(ALTERNATE_RING, config.isDectRingOnOff());
    }

    @Override
    protected void updateStatus(PhoneStatus status) {
        super.updateStatus(status);
        updateIfActive(GAIN_RX, new PercentType(status.getGainRx()));
        updateIfActive(GAIN_TX, new PercentType(status.getGainTx()));
    }

    @Override
    protected boolean internalHandleCommand(String channelId, Command command) throws FreeboxException {
        PhoneManager phoneManager = getManager(PhoneManager.class);
        if (command instanceof OnOffType onOffCommand) {
            boolean status = OnOffType.ON.equals(onOffCommand);
            if (RINGING.equals(channelId)) {
                phoneManager.ringDect(status);
                return true;
            } else if (DECT_ACTIVE.equals(channelId)) {
                phoneManager.setStatus(status);
                return true;
            } else if (ALTERNATE_RING.equals(channelId)) {
                phoneManager.alternateRing(status);
                return true;
            }
        }
        if (command instanceof PercentType percentCommand) {
            if (GAIN_RX.equals(channelId)) {
                phoneManager.setGainRx(getClientId(), percentCommand.intValue());
                updateIfActive(GAIN_RX, percentCommand);
                return true;
            } else if (GAIN_TX.equals(channelId)) {
                phoneManager.setGainTx(getClientId(), percentCommand.intValue());
                updateIfActive(GAIN_RX, percentCommand);
                return true;
            }
        }
        return super.internalHandleCommand(channelId, command);
    }

    @Override
    public Configuration getConfig() {
        return super.getConfig();
    }
}
