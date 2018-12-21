/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.veluxklf200.internal.engine;

import java.util.ArrayList;
import java.util.List;

import org.openhab.binding.veluxklf200.internal.commands.BaseKLFCommand;
import org.openhab.binding.veluxklf200.internal.commands.structure.KLFCommandCodes;
import org.openhab.binding.veluxklf200.internal.components.VeluxNodeType;
import org.openhab.binding.veluxklf200.internal.components.VeluxPosition;
import org.openhab.binding.veluxklf200.internal.components.VeluxState;
import org.openhab.binding.veluxklf200.internal.utility.KLFUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Under normal circumstances, the KLF200 will respond to commands that have
 * been sent to it. However, it will also issue responses (notifications) in the
 * case that something else on the network issued a command. For example, if a
 * remote control is used to operate a velux device, the KLF will issue
 * notifications in respect of the devices movements. These are captured /
 * processed here.
 *
 * @author MFK - Initial Contribution
 */
public class KLFEventNotification {

    /** Logging. */
    private final Logger logger = LoggerFactory.getLogger(KLFEventNotification.class);

    /** List of response codes that we are watching out for. */
    private short watchList[] = new short[] { KLFCommandCodes.GW_NODE_STATE_POSITION_CHANGED_NTF };

    /** List of third-parties that have registered to be notified of events. */
    private List<KLFEventListener> listeners;

    /**
     * Instantiates a new KLF event notification.
     */
    protected KLFEventNotification() {
        this.listeners = new ArrayList<KLFEventListener>();
    }

    /**
     * Called by others to notify us that an event was recieved that we may be
     * interested in.
     *
     * @param responseCode
     *                         the response code
     * @param data
     *                         the data
     */
    protected void notifyEvent(short responseCode, byte[] data) {
        logger.trace("Event Notified: {}, -> {}", String.format("0x%04X", responseCode), data);
        switch (responseCode) {
            case KLFCommandCodes.GW_NODE_STATE_POSITION_CHANGED_NTF:
                logger.trace("Handling Notification for ({}) GW_NODE_STATE_POSITION_CHANGED_NTF.",
                        String.format("0x%04X", responseCode));

                logger.debug(
                        "Node {} position changed, state: {}, current position: {} open, target position:{} open, time remaining: {} seconds.",
                        data[BaseKLFCommand.FIRSTBYTE], VeluxState.create(data[BaseKLFCommand.FIRSTBYTE + 1]),
                        VeluxPosition.create(data[BaseKLFCommand.FIRSTBYTE + 2], data[BaseKLFCommand.FIRSTBYTE + 3])
                                .getPercentageOpen(),
                        VeluxPosition.create(data[BaseKLFCommand.FIRSTBYTE + 4], data[BaseKLFCommand.FIRSTBYTE + 5])
                                .getPercentageOpen(),
                        KLFUtils.extractTwoBytes(data[BaseKLFCommand.FIRSTBYTE + 14],
                                data[BaseKLFCommand.FIRSTBYTE + 15]));
                for (KLFEventListener listen : listeners) {
                    // IMPORTANT
                    // For now, only implementing notification callbacks for "Interior Venetian Blinds". In time, this
                    // can be expanded to include other types as required.
                    listen.handleEvent(VeluxNodeType.VERTICAL_INTERIOR_BLINDS, data[BaseKLFCommand.FIRSTBYTE],
                            VeluxPosition.create(data[BaseKLFCommand.FIRSTBYTE + 2], data[BaseKLFCommand.FIRSTBYTE + 3])
                                    .getPosition());
                }
                break;
            default:
                logger.error("Notified of event ({}), but unable to handle it. Data: {}",
                        String.format("0x%04X", responseCode), data);
                break;
        }
    }

    /**
     * We are interested in only certain events / notifications. This checks to
     * see if a particular event (command code) is on our watch-list (something
     * that we are interested in being notified about).
     *
     * @param code
     *                 the code
     * @return true, if is on watch list
     */
    public boolean isOnWatchList(short code) {
        for (short elem : this.watchList) {
            if (elem == code) {
                return true;
            }
        }
        return false;
    }

    /**
     * Called by third-party that is interested in being notified when things change. This adds them to a list of
     * 'listeners' who will be notified when something happens
     *
     * @param listener Class that implements the {@link KLFEventListener} interface.
     */
    public void registerListener(KLFEventListener listener) {
        this.listeners.add(listener);
    }
}
