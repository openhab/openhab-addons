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
package org.openhab.binding.sony.internal.scalarweb.protocols;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.types.Command;
import org.openhab.binding.sony.internal.ThingCallback;
import org.openhab.binding.sony.internal.scalarweb.ScalarWebChannel;
import org.openhab.binding.sony.internal.scalarweb.ScalarWebChannelDescriptor;
import org.openhab.binding.sony.internal.scalarweb.ScalarWebContext;
import org.openhab.binding.sony.internal.scalarweb.models.ScalarWebMethod;
import org.openhab.binding.sony.internal.scalarweb.models.ScalarWebService;
import org.openhab.binding.sony.internal.scalarweb.models.api.Enabled;
import org.openhab.binding.sony.internal.scalarweb.models.api.PowerSyncMode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The implementation of the protocol handles the CEC service
 *
 * @author Tim Roberts - Initial contribution
 * @param <T> the generic type for the callback
 */
@NonNullByDefault
class ScalarWebCecProtocol<T extends ThingCallback<String>> extends AbstractScalarWebProtocol<T> {

    /** The logger */
    private final Logger logger = LoggerFactory.getLogger(ScalarWebCecProtocol.class);

    // Constants used by the protocol
    private static final String CONTROLMODE = "controlmode";
    private static final String MHLAUTOINPUTCHANGEMODE = "mhlautoinputchangemode";
    private static final String MHLPOWERFEEDMODE = "mhlpowerfeedmode";
    private static final String POWEROFFSYNCMODE = "poweroffsyncmode";
    private static final String POWERONSYNCMODE = "poweronsyncmode";

    /**
     * Instantiates a new scalar web cec protocol.
     *
     * @param factory the non-null factory
     * @param context the non-null context
     * @param service the non-null service
     * @param scheduler the non-null scheduler
     * @param callback the non-null callback
     */
    ScalarWebCecProtocol(final ScalarWebProtocolFactory<T> factory, final ScalarWebContext context,
            final ScalarWebService service, final T callback) {
        super(factory, context, service, callback);
    }

    @Override
    public Collection<ScalarWebChannelDescriptor> getChannelDescriptors(final boolean dynamicOnly) {
        final List<ScalarWebChannelDescriptor> descriptors = new ArrayList<ScalarWebChannelDescriptor>();

        // no dynamic channels
        if (dynamicOnly) {
            return descriptors;
        }

        if (service.hasMethod(ScalarWebMethod.SETCECCONTROLMODE)) {
            descriptors.add(createDescriptor(createChannel(CONTROLMODE), "Switch", "scalarceccontrolmode"));
        }

        if (service.hasMethod(ScalarWebMethod.SETMHLAUTOINPUTCHANGEMODE)) {
            descriptors.add(createDescriptor(createChannel(MHLAUTOINPUTCHANGEMODE), "Switch",
                    "scalarcecmhlautoinputchangemode"));
        }

        if (service.hasMethod(ScalarWebMethod.SETMHLPOWERFEEDMODE)) {
            descriptors.add(createDescriptor(createChannel(MHLPOWERFEEDMODE), "Switch", "scalarcecmhlpowerfeedmode"));
        }

        if (service.hasMethod(ScalarWebMethod.SETPOWERSYNCMODE)) {
            descriptors.add(createDescriptor(createChannel(POWEROFFSYNCMODE), "Switch", "scalarcecpoweroffsyncmode"));
            descriptors.add(createDescriptor(createChannel(POWERONSYNCMODE), "Switch", "scalarcecpoweronsyncmode"));
        }

        return descriptors;
    }

    @Override
    public void refreshState(boolean initial) {
    }

    @Override
    public void refreshChannel(final ScalarWebChannel channel) {
    }

    @Override
    public void setChannel(final ScalarWebChannel channel, final Command command) {
        Objects.requireNonNull(channel, "channel cannot be null");
        Objects.requireNonNull(command, "command cannot be null");

        switch (channel.getCategory()) {
            case CONTROLMODE:
                if (command instanceof OnOffType) {
                    setControlMode(command == OnOffType.ON);
                } else {
                    logger.debug("Control mode command not an OnOffType: {}", command);
                }

                break;

            case MHLAUTOINPUTCHANGEMODE:
                if (command instanceof OnOffType) {
                    setMhlAutoInputChangeMode(command == OnOffType.ON);
                } else {
                    logger.debug("MHLAUTOINPUTCHANGEMODE command not an OnOffType: {}", command);
                }

                break;

            case MHLPOWERFEEDMODE:
                if (command instanceof OnOffType) {
                    setMhlPowerFeedMode(command == OnOffType.ON);
                } else {
                    logger.debug("MHLPOWERFEEDMODE command not an OnOffType: {}", command);
                }

                break;

            case POWEROFFSYNCMODE:
                if (command instanceof OnOffType) {
                    setPowerOffSyncMode(command == OnOffType.ON);
                } else {
                    logger.debug("POWEROFFSYNCMODE command not an OnOffType: {}", command);
                }

                break;

            case POWERONSYNCMODE:
                if (command instanceof OnOffType) {
                    setPowerOnSyncMode(command == OnOffType.ON);
                } else {
                    logger.debug("POWERONSYNCMODE command not an OnOffType: {}", command);
                }

                break;

            default:
                logger.debug("Unhandled channel command: {} - {}", channel, command);
                break;
        }
    }

    /**
     * Sets the control mode
     *
     * @param on true if on, false otherwise
     */
    private void setControlMode(final boolean on) {
        handleExecute(ScalarWebMethod.SETCECCONTROLMODE, new Enabled(on));
    }

    /**
     * Sets the mhl auto input change mode
     *
     * @param on true if on, false otherwise
     */
    private void setMhlAutoInputChangeMode(final boolean on) {
        handleExecute(ScalarWebMethod.SETMHLAUTOINPUTCHANGEMODE, new Enabled(on));
    }

    /**
     * Sets the mhl power feed mode
     *
     * @param on true if on, false otherwise
     */
    private void setMhlPowerFeedMode(final boolean on) {
        handleExecute(ScalarWebMethod.SETMHLPOWERFEEDMODE, new Enabled(on));
    }

    /**
     * Sets the power off sync mode
     *
     * @param on true if on, false otherwise
     */
    private void setPowerOffSyncMode(final boolean on) {
        handleExecute(ScalarWebMethod.SETPOWERSYNCMODE, new PowerSyncMode(on, null));
    }

    /**
     * Sets the power on sync mode
     *
     * @param on true if on, false otherwise
     */
    private void setPowerOnSyncMode(final boolean on) {
        handleExecute(ScalarWebMethod.SETPOWERSYNCMODE, new PowerSyncMode(null, on));
    }
}
