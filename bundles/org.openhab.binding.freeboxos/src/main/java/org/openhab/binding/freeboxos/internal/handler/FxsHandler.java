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
<<<<<<< Upstream, based on origin/main
import org.openhab.binding.freeboxos.internal.api.rest.PhoneManager;
import org.openhab.binding.freeboxos.internal.api.rest.PhoneManager.Config;
import org.openhab.binding.freeboxos.internal.api.rest.PhoneManager.Status;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.types.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link FxsHandler} is responsible for handling everything associated to the landline associated with the
 * Freebox Server.
 *
 * @author Gaël L'hopital - Initial contribution
 */
@NonNullByDefault
public class FxsHandler extends ApiConsumerHandler {
    private final Logger logger = LoggerFactory.getLogger(FxsHandler.class);

    public FxsHandler(Thing thing) {
        super(thing);
    }

    @Override
    void initializeProperties(Map<String, String> properties) throws FreeboxException {
        getManager(PhoneManager.class).getStatus(getClientId())
                .ifPresent(status -> properties.put(Thing.PROPERTY_VENDOR, status.vendor()));
    }

    @Override
    protected void internalPoll() throws FreeboxException {
        logger.debug("Polling landline status...");

        Config config = getManager(PhoneManager.class).getConfig();
        updateConfigChannels(config);

        getManager(PhoneManager.class).getStatus(getClientId()).ifPresent(this::updateStatusChannels);
    }

    protected void updateConfigChannels(Config config) {
        updateChannelString(TELEPHONY_SERVICE, config.network());
    }

    protected void updateStatusChannels(Status status) {
        updateChannelOnOff(ONHOOK, status.onHook());
        updateChannelOnOff(RINGING, status.isRinging());
        updateChannelString(HARDWARE_STATUS, status.hardwareDefect() ? "KO" : "OK");
        updateStatus(ThingStatus.ONLINE);
    }

    @Override
    protected boolean internalHandleCommand(String channelId, Command command) throws FreeboxException {
        if (RINGING.equals(channelId) && command instanceof OnOffType) {
            getManager(PhoneManager.class).ringFxs(TRUE_COMMANDS.contains(command));
            return true;
        }
        return super.internalHandleCommand(channelId, command);
=======
import org.openhab.binding.freeboxos.internal.api.phone.PhoneConfig;
import org.openhab.binding.freeboxos.internal.api.phone.PhoneManager;
import org.openhab.binding.freeboxos.internal.api.phone.PhoneStatus;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.types.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link FxsHandler} is responsible for handling everything associated to the landline associated with the
 * Freebox Server.
 *
 * @author Gaël L'hopital - Initial contribution
 */
@NonNullByDefault
public class FxsHandler extends ApiConsumerHandler implements FreeClientIntf {
    private final Logger logger = LoggerFactory.getLogger(FxsHandler.class);

    public FxsHandler(Thing thing) {
        super(thing);
    }

    @Override
    void initializeProperties(Map<String, String> properties) throws FreeboxException {
        // nothing to do here
    }

    @Override
    protected void internalPoll() throws FreeboxException {
        logger.debug("Polling landline status...");

        PhoneConfig config = getManager(PhoneManager.class).getConfig();
        updateConfig(config);

        getManager(PhoneManager.class).getOptStatus(getClientId()).ifPresent(this::updateStatus);
    }

    protected void updateConfig(PhoneConfig config) {
        updateChannelString(TELEPHONY_SERVICE, config.getNetwork().name());
    }

    protected void updateStatus(PhoneStatus status) {
        updateChannelOnOff(ONHOOK, status.isOnHook());
        updateChannelOnOff(RINGING, status.isRinging());
        updateChannelString(HARDWARE_STATUS, status.isHardwareDefect() ? "KO" : "OK");
        updateStatus(ThingStatus.ONLINE);
    }

    @Override
    protected boolean internalHandleCommand(String channelId, Command command) throws FreeboxException {
        if (RINGING.equals(channelId)) {
            if (command instanceof OnOffType onOffCommand) {
                getManager(PhoneManager.class).ringFxs(OnOffType.ON.equals(onOffCommand));
                return true;
            }
        }
        return super.internalHandleCommand(channelId, command);
    }

    @Override
    public Configuration getConfig() {
        return super.getConfig();
>>>>>>> 006a813 Saving work before instroduction of ArrayListDeserializer
    }
}
