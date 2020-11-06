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
package org.openhab.binding.systeminfo.internal.handler;

import static org.openhab.binding.systeminfo.internal.SysteminfoBindingConstants.*;

import java.math.BigDecimal;
import java.util.Objects;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.systeminfo.internal.model.SysteminfoInterface;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.library.unit.SmartHomeUnits;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.openhab.core.types.UnDefType;
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

    private final Logger logger = LoggerFactory.getLogger(ProcessinfoHandler.class);

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

        try {
            switch (channelUID.getId()) {
                case CHANNEL_NAME: {
                    String state = systeminfo.getProcessName(pid);
                    updateState(channelUID, new StringType(state));
                    break;
                }
                case CHANNEL_PROCESS_LOAD: {
                    BigDecimal state = systeminfo.getProcessCpuUsage(pid);
                    updateState(channelUID, new QuantityType<>(state, SmartHomeUnits.PERCENT));
                    break;
                }
                case CHANNEL_PROCESS_RESIDENT_MEMORY: {
                    BigDecimal state = systeminfo.getProcessResidentMemory(pid);
                    updateState(channelUID, new QuantityType<>(state, SmartHomeUnits.BYTE));
                    break;
                }
                case CHANNEL_PROCESS_VIRTUAL_MEMORY: {
                    BigDecimal state = systeminfo.getProcessVirtualMemory(pid);
                    updateState(channelUID, new QuantityType<>(state, SmartHomeUnits.BYTE));
                    break;
                }
                case CHANNEL_PROCESS_USER: {
                    String state = systeminfo.getProcessUser(pid);
                    updateState(channelUID, new StringType(state));
                    break;
                }
                case CHANNEL_PROCESS_PATH: {
                    String state = systeminfo.getProcessPath(pid);
                    updateState(channelUID, new StringType(state));
                    break;
                }
                case CHANNEL_PROCESS_THREADS: {
                    BigDecimal state = systeminfo.getProcessThreads(pid);
                    updateState(channelUID, new DecimalType(state));
                    break;
                }
                default: {
                    logger.debug("Channel with unknown ID: {}.", channelUID);
                    break;
                }
            }
        } catch (IllegalArgumentException exception) {
            if (isLinked(channelUID)) {
                logger.warn("No information for channel {} with process id {}.", channelUID, pid);
            }
            updateState(channelUID, UnDefType.UNDEF);
        } catch (Exception exception) {
            String message = exception.getMessage();
            logger.debug("Unexpected error occurred while getting system information: {}.", message);
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, message);
        }
    }

    @Override
    public void initialize() {
        logger.debug("Start initializing.");

        SysteminfoHandler handler = null;
        final Thing thing = getThing();
        final Bridge bridge = getBridge();
        if (updateConfiguration(thing) && (bridge != null)) {
            handler = (SysteminfoHandler) bridge.getHandler();
        } else {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_UNINITIALIZED, "Thing cannot be initialized.");
        }
        if (handler != null) {
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
        SysteminfoHandler handler = null;
        final Bridge bridge = getBridge();
        if (bridge != null) {
            handler = (SysteminfoHandler) bridge.getHandler();
        }
        if (handler != null) {
            for (Channel channel : getThing().getChannels()) {
                handler.changeChannelPriority(channel.getUID(), null);
            }
        }
        super.dispose();
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
