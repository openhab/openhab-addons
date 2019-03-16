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
package org.openhab.binding.systeminfo.internal.handler;

import static org.openhab.binding.systeminfo.internal.SysteminfoBindingConstants.*;

import java.math.BigDecimal;
import java.util.Objects;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.config.core.Configuration;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.Channel;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.RefreshType;
import org.eclipse.smarthome.core.types.State;
import org.eclipse.smarthome.core.types.UnDefType;
import org.openhab.binding.systeminfo.internal.model.SysteminfoInterface;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link ProcessinfoHandler} is responsible for providing real time information about the process
 *
 * @author Alexander Falkenstern - Initial contribution
 */
@NonNullByDefault
public class ProcessinfoHandler extends BaseThingHandler {

    private BigDecimal pid = new BigDecimal(-1);

    private @NonNullByDefault({}) SysteminfoInterface systeminfo;

    private Logger logger = LoggerFactory.getLogger(ProcessinfoHandler.class);

    public ProcessinfoHandler(Thing thing, @Nullable SysteminfoInterface systeminfo) {
        super(thing);

        Objects.requireNonNull(systeminfo, "Systeminfo may not be null");
        this.systeminfo = systeminfo;
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        logger.debug("Handle command {} on channel {}", command, channelUID);

        Thing thing = getThing();
        if (ThingStatus.ONLINE != thing.getStatus()) {
            logger.debug("Cannot handle command. Thing is not ONLINE.");
            return;
        }

        if (!(command instanceof RefreshType)) {
            logger.debug("Unsupported command {}. Supported commands: REFRESH.", command);
            return;
        }

        if (isLinked(channelUID)) {
            State state = UnDefType.UNDEF;
            try {
                switch (channelUID.getId()) {
                    case CHANNEL_NAME: {
                        state = systeminfo.getProcessName(pid.intValue());
                        break;
                    }
                    case CHANNEL_PROCESS_LOAD: {
                        state = systeminfo.getProcessCpuUsage(pid.intValue());
                        break;
                    }
                    case CHANNEL_PROCESS_RESIDENT_MEMORY: {
                        state = systeminfo.getProcessResidentMemory(pid.intValue());
                        break;
                    }
                    case CHANNEL_PROCESS_VIRTUAL_MEMORY: {
                        state = systeminfo.getProcessVirtualMemory(pid.intValue());
                        break;
                    }
                    case CHANNEL_PROCESS_USER: {
                        state = systeminfo.getProcessUser(pid.intValue());
                        break;
                    }
                    case CHANNEL_PROCESS_PATH: {
                        state = systeminfo.getProcessPath(pid.intValue());
                        break;
                    }
                    case CHANNEL_PROCESS_THREADS: {
                        state = systeminfo.getProcessThreads(pid.intValue());
                        break;
                    }
                    default: {
                        logger.debug("Channel with unknown ID: {}.", channelUID);
                        break;
                    }
                }
            } catch (IllegalArgumentException exception) {
                logger.warn("No information for channel {} with process id {}.", channelUID, pid);
            } catch (Exception exception) {
                logger.debug("Unexpected error occurred while getting system information: {}.", exception);
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, exception.getMessage());
            }
            updateState(channelUID, state);
        }
    }

    @Override
    public void initialize() {
        logger.debug("Start initializing.");

        final Thing thing = getThing();
        final Bridge bridge = getBridge();
        if (updateConfiguration(thing) && (bridge != null)) {
            SysteminfoHandler handler = (SysteminfoHandler) bridge.getHandler();
            for (Channel channel : thing.getChannels()) {
                Configuration properties = channel.getConfiguration();
                handler.changeChannelPriority(channel.getUID(), (String) properties.get(PARAMETER_PRIOIRITY));
            }

            logger.debug("Thing is successfully initialized.");
            updateStatus(ThingStatus.ONLINE);
        } else {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.HANDLER_INITIALIZING_ERROR,
                    "Thing cannot be initialized.");
        }
    }

    @Override
    public void dispose() {
        super.dispose();
        Bridge bridge = getBridge();
        if (bridge != null) {
            SysteminfoHandler handler = (SysteminfoHandler) bridge.getHandler();
            for (Channel channel : getThing().getChannels()) {
                handler.changeChannelPriority(channel.getUID(), null);
            }
        }
    }

    @Override
    public void thingUpdated(Thing thing) {
        if (updateConfiguration(thing)) {
            super.thingUpdated(thing);
        }
    }

    private boolean updateConfiguration(Thing thing) {
        boolean result = false;
        try {
            final Configuration config = thing.getConfiguration();
            synchronized (pid) {
                BigDecimal value = (BigDecimal) config.get(PROCESS_ID);
                if (value.intValue() < 0) {
                    throw new IllegalArgumentException("Process id must be positive number.");
                }
                pid = value;
            }
            logger.debug("Process id set to {}.", pid);
            result = true;
        } catch (ClassCastException exception) {
            logger.debug("Channel configuration cannot be read.");
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, exception.getMessage());
        } catch (IllegalArgumentException exception) {
            logger.warn("Process id is invalid. Please change the thing configuration.");
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, exception.getMessage());
        }
        return result;
    }

}
