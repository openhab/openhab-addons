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

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.freeboxos.internal.action.RepeaterActions;
import org.openhab.binding.freeboxos.internal.api.FreeboxException;
import org.openhab.binding.freeboxos.internal.api.lan.browser.LanHost;
import org.openhab.binding.freeboxos.internal.api.repeater.Repeater;
import org.openhab.binding.freeboxos.internal.api.repeater.RepeaterManager;
import org.openhab.core.library.unit.Units;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.ThingHandlerService;
import org.openhab.core.types.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link RepeaterHandler} is responsible for interface to a freebox
 * pop repeater.
 *
 * @author Gaël L'hopital - Initial contribution
 */
@NonNullByDefault
public class RepeaterHandler extends HostHandler implements FreeClientIntf, FreeDeviceIntf {
    private final Logger logger = LoggerFactory.getLogger(RepeaterHandler.class);
    private long uptime = -1;
    private final ChannelUID eventChannelUID;

    public RepeaterHandler(Thing thing) {
        super(thing);
        eventChannelUID = new ChannelUID(getThing().getUID(), REPEATER_MISC, BOX_EVENT);
    }

    @Override
    void initializeProperties(Map<String, String> properties) throws FreeboxException {
        super.initializeProperties(properties);

        Repeater repeater = getManager(RepeaterManager.class).getDevice(getClientId());
        properties.put(Thing.PROPERTY_SERIAL_NUMBER, repeater.getSn());
        properties.put(Thing.PROPERTY_FIRMWARE_VERSION, repeater.getFirmwareVersion());
        properties.put(Thing.PROPERTY_MODEL_ID, repeater.getModel().name());
    }

    @Override
    protected void internalPoll() throws FreeboxException {
        super.internalPoll();

        if (!thing.getStatus().equals(ThingStatus.ONLINE)) {
            return;
        }

        logger.debug("Polling Repeater status");
        RepeaterManager repeaterManager = getManager(RepeaterManager.class);

        Repeater repeater = repeaterManager.getDevice(getClientId());
        updateChannelOnOff(REPEATER_MISC, LED, repeater.isLedActivated());
        updateChannelString(REPEATER_MISC, CONNECTION_STATUS, repeater.getConnection());

        List<LanHost> hosts = repeaterManager.getRepeaterHosts(getClientId());
        updateChannelDecimal(REPEATER_MISC, HOST_COUNT, hosts.size());

        long newUptime = repeater.getUptimeVal();
        uptime = controlUptimeAndFirmware(newUptime, uptime, repeater.getFirmwareVersion());
        updateChannelQuantity(REPEATER_MISC, UPTIME, uptime, Units.SECOND);
    }

    @Override
    protected boolean internalHandleCommand(String channelId, Command command) throws FreeboxException {
        if (ON_OFF_CLASSES.contains(command.getClass())) {
            boolean enable = TRUE_COMMANDS.contains(command);
            if (LED.equals(channelId)) {
                RepeaterManager repeaterManager = getManager(RepeaterManager.class);
                repeaterManager.led(getClientId(), enable)
                        .ifPresent(repeater -> updateChannelOnOff(REPEATER_MISC, LED, repeater.isLedActivated()));
            }
        }
        return super.internalHandleCommand(channelId, command);
    }

    public void reboot() {
        try {
            getManager(RepeaterManager.class).reboot(getClientId());
            triggerChannel(getEventChannelUID(), "reboot_requested");
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.DUTY_CYCLE, "System rebooting...");
            stopRefreshJob();
            scheduler.schedule(this::initialize, 30, TimeUnit.SECONDS);
        } catch (FreeboxException e) {
            logger.warn("Error rebooting device : {}", e.getMessage());
        }
    }

    @Override
    public Collection<Class<? extends ThingHandlerService>> getServices() {
        return Collections.singleton(RepeaterActions.class);
    }

    @Override
    public ChannelUID getEventChannelUID() {
        return eventChannelUID;
    }

    @Override
    public void triggerChannel(ChannelUID channelUID, String event) {
        super.triggerChannel(channelUID, event);
    }

    @Override
    public Map<String, String> editProperties() {
        return super.editProperties();
    }

    @Override
    public void updateProperties(@Nullable Map<String, String> properties) {
        super.updateProperties(properties);
    }

}
