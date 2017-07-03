/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.sony.internal.scalarweb.protocols;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.types.Command;
import org.openhab.binding.sony.internal.ThingCallback;
import org.openhab.binding.sony.internal.scalarweb.ScalarWebChannel;
import org.openhab.binding.sony.internal.scalarweb.ScalarWebChannelDescriptor;
import org.openhab.binding.sony.internal.scalarweb.ScalarWebChannelTracker;
import org.openhab.binding.sony.internal.scalarweb.models.ScalarWebMethod;
import org.openhab.binding.sony.internal.scalarweb.models.ScalarWebService;
import org.openhab.binding.sony.internal.scalarweb.models.ScalarWebState;
import org.openhab.binding.sony.internal.scalarweb.models.api.Enabled;
import org.openhab.binding.sony.internal.scalarweb.models.api.PowerSyncMode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// TODO: Auto-generated Javadoc
/**
 * The Class ScalarWebCecProtocol.
 *
 * @author Tim Roberts - Initial contribution
 * @param <T> the generic type
 */
class ScalarWebCecProtocol<T extends ThingCallback<ScalarWebChannel>> extends AbstractScalarWebProtocol<T> {

    /** The logger. */
    private Logger logger = LoggerFactory.getLogger(ScalarWebSystemProtocol.class);

    /** The Constant CONTROLMODE. */
    private final static String CONTROLMODE = "controlmode";

    /** The Constant MHLAUTOINPUTCHANGEMODE. */
    private final static String MHLAUTOINPUTCHANGEMODE = "mhlautoinputchangemode";

    /** The Constant MHLPOWERFEEDMODE. */
    private final static String MHLPOWERFEEDMODE = "mhlpowerfeedmode";

    /** The Constant POWEROFFSYNCMODE. */
    private final static String POWEROFFSYNCMODE = "poweroffsyncmode";

    /** The Constant POWERONSYNCMODE. */
    private final static String POWERONSYNCMODE = "poweronsyncmode";

    /**
     * Instantiates a new scalar web cec protocol.
     *
     * @param tracker the tracker
     * @param state the state
     * @param service the service
     * @param callback the callback
     */
    ScalarWebCecProtocol(ScalarWebChannelTracker tracker, ScalarWebState state, ScalarWebService service, T callback) {
        super(tracker, state, service, callback);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.openhab.binding.sony.internal.scalarweb.protocols.ScalarWebProtocol#getChannelDescriptors()
     */
    @Override
    public Collection<ScalarWebChannelDescriptor> getChannelDescriptors() {

        final List<ScalarWebChannelDescriptor> descriptors = new ArrayList<ScalarWebChannelDescriptor>();

        if (service.getMethod(ScalarWebMethod.SetCecControlMode) != null) {
            descriptors.add(createDescriptor(createChannel(CONTROLMODE), "Switch", "scalarceccontrolmode"));
        }

        if (service.getMethod(ScalarWebMethod.SetMhlAutoInputChangeMode) != null) {
            descriptors.add(createDescriptor(createChannel(MHLAUTOINPUTCHANGEMODE), "Switch",
                    "scalarcecmhlautoinputchangemode"));
        }

        if (service.getMethod(ScalarWebMethod.SetMhlPowerFeedMode) != null) {
            descriptors.add(createDescriptor(createChannel(MHLPOWERFEEDMODE), "Switch", "scalarcecmhlpowerfeedmode"));
        }

        if (service.getMethod(ScalarWebMethod.SetPowerSyncMode) != null) {
            descriptors.add(createDescriptor(createChannel(POWEROFFSYNCMODE), "Switch", "scalarcecpoweroffsyncmode"));
            descriptors.add(createDescriptor(createChannel(POWERONSYNCMODE), "Switch", "scalarcecpoweronsyncmode"));
        }

        return descriptors;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.openhab.binding.sony.internal.scalarweb.protocols.ScalarWebProtocol#refreshState()
     */
    @Override
    public void refreshState() {
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.openhab.binding.sony.internal.scalarweb.protocols.ScalarWebProtocol#refreshChannel(org.openhab.binding.sony.
     * internal.scalarweb.ScalarWebChannel)
     */
    @Override
    public void refreshChannel(ScalarWebChannel channel) {

    }

    /*
     * (non-Javadoc)
     * 
     * @see org.openhab.binding.sony.internal.scalarweb.protocols.ScalarWebProtocol#setChannel(org.openhab.binding.sony.
     * internal.scalarweb.ScalarWebChannel, org.eclipse.smarthome.core.types.Command)
     */
    @Override
    public void setChannel(ScalarWebChannel channel, Command command) {
        switch (channel.getId()) {
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
     * Sets the control mode.
     *
     * @param on the new control mode
     */
    private void setControlMode(boolean on) {
        handleExecute(ScalarWebMethod.SetCecControlMode, new Enabled(on));
    }

    /**
     * Sets the mhl auto input change mode.
     *
     * @param on the new mhl auto input change mode
     */
    private void setMhlAutoInputChangeMode(boolean on) {
        handleExecute(ScalarWebMethod.SetMhlAutoInputChangeMode, new Enabled(on));
    }

    /**
     * Sets the mhl power feed mode.
     *
     * @param on the new mhl power feed mode
     */
    private void setMhlPowerFeedMode(boolean on) {
        handleExecute(ScalarWebMethod.SetMhlPowerFeedMode, new Enabled(on));
    }

    /**
     * Sets the power off sync mode.
     *
     * @param on the new power off sync mode
     */
    private void setPowerOffSyncMode(boolean on) {
        handleExecute(ScalarWebMethod.SetPowerSyncMode, new PowerSyncMode(on, null));
    }

    /**
     * Sets the power on sync mode.
     *
     * @param on the new power on sync mode
     */
    private void setPowerOnSyncMode(boolean on) {
        handleExecute(ScalarWebMethod.SetPowerSyncMode, new PowerSyncMode(null, on));
    }
}
