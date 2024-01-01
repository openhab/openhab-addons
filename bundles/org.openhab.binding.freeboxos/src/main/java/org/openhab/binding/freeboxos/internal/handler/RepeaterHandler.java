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
package org.openhab.binding.freeboxos.internal.handler;

import static org.openhab.binding.freeboxos.internal.FreeboxOsBindingConstants.*;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.freeboxos.internal.action.RepeaterActions;
import org.openhab.binding.freeboxos.internal.api.FreeboxException;
import org.openhab.binding.freeboxos.internal.api.rest.LanBrowserManager.LanHost;
import org.openhab.binding.freeboxos.internal.api.rest.RepeaterManager;
import org.openhab.binding.freeboxos.internal.api.rest.RepeaterManager.Repeater;
import org.openhab.core.library.unit.Units;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
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
public class RepeaterHandler extends HostHandler implements FreeDeviceIntf {
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
        properties.put(Thing.PROPERTY_SERIAL_NUMBER, repeater.sn());
        properties.put(Thing.PROPERTY_FIRMWARE_VERSION, repeater.firmwareVersion());
        properties.put(Thing.PROPERTY_MODEL_ID, repeater.model().name());
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
        updateChannelOnOff(REPEATER_MISC, LED, repeater.ledActivated());
        updateChannelString(REPEATER_MISC, CONNECTION_STATUS, repeater.connection());

        List<LanHost> hosts = repeaterManager.getRepeaterHosts(getClientId());
        updateChannelDecimal(REPEATER_MISC, HOST_COUNT, hosts.size());

        uptime = checkUptimeAndFirmware(repeater.getUptimeVal(), uptime, repeater.firmwareVersion());
        updateChannelQuantity(REPEATER_MISC, UPTIME, uptime, Units.SECOND);
    }

    @Override
    protected boolean internalHandleCommand(String channelId, Command command) throws FreeboxException {
        if (ON_OFF_CLASSES.contains(command.getClass()) && LED.equals(channelId)) {
            getManager(RepeaterManager.class).led(getClientId(), TRUE_COMMANDS.contains(command))
                    .ifPresent(repeater -> updateChannelOnOff(REPEATER_MISC, LED, repeater.ledActivated()));
        }
        return super.internalHandleCommand(channelId, command);
    }

    public void reboot() {
        processReboot(() -> {
            try {
                getManager(RepeaterManager.class).reboot(getClientId());
            } catch (FreeboxException e) {
                logger.warn("Error rebooting: {}", e.getMessage());
            }
        });
    }

    @Override
    public Collection<Class<? extends ThingHandlerService>> getServices() {
        return Set.of(RepeaterActions.class);
    }

    @Override
    public ChannelUID getEventChannelUID() {
        return eventChannelUID;
    }

    @Override
    public void triggerChannel(ChannelUID channelUID, String event) {
        super.triggerChannel(channelUID, event);
    }
}
