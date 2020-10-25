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
package org.openhab.binding.serial.internal;

import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.library.types.StringType;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.ThingStatusInfo;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;

/**
 * The {@link SerialDeviceHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Mike Major - Initial contribution
 */
@NonNullByDefault
public class SerialDeviceHandler extends BaseThingHandler {

    private @NonNullByDefault({}) Pattern devicePattern;

    private StringType deviceData;

    public SerialDeviceHandler(Thing thing) {
        super(thing);
        deviceData = new StringType();
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (command instanceof RefreshType) {
            refresh();
        }
    }

    @Override
    public void initialize() {
        SerialDeviceConfiguration config = getConfigAs(SerialDeviceConfiguration.class);

        try {
            devicePattern = Pattern.compile(config.patternMatch);
        } catch (PatternSyntaxException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "Invalid device pattern" + e.getMessage());
            return;
        }

        updateStatus(ThingStatus.UNKNOWN);
        bridgeStatusChanged(getBridgeStatus());
    }

    public void refresh() {
        if (isLinked(SerialBindingConstants.DEVICE_CHANNEL)) {
            updateState(SerialBindingConstants.DEVICE_CHANNEL, deviceData);
        }
    }

    public void handleData(String data) {
        if (devicePattern.matcher(data).matches()) {
            this.deviceData = StringType.valueOf(data);
            refresh();
        }
    }

    @Override
    public void bridgeStatusChanged(ThingStatusInfo bridgeStatusInfo) {
        if (getThing().getStatusInfo().getStatusDetail() == ThingStatusDetail.CONFIGURATION_ERROR) {
            return;
        }

        if (bridgeStatusInfo.getStatus() == ThingStatus.ONLINE && getThing().getStatus() == ThingStatus.UNKNOWN) {
            updateStatus(ThingStatus.ONLINE, ThingStatusDetail.NONE);
            return;
        }

        super.bridgeStatusChanged(bridgeStatusInfo);
    }

    /**
     * Return the bridge status.
     */
    public ThingStatusInfo getBridgeStatus() {
        Bridge b = getBridge();
        if (b != null) {
            return b.getStatusInfo();
        } else {
            return new ThingStatusInfo(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_OFFLINE, null);
        }
    }
}
