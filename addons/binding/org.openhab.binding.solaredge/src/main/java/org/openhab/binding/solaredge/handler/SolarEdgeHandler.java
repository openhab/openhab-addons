/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.solaredge.handler;

import java.util.List;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.eclipse.smarthome.core.types.State;
import org.openhab.binding.solaredge.config.SolarEdgeConfiguration;
import org.openhab.binding.solaredge.internal.connector.WebInterface;
import org.openhab.binding.solaredge.internal.model.Channel;

/**
 * public interface of the {@link SolarEdgeHandler}
 *
 * @author Alexander Friese - initial contribution
 */
@NonNullByDefault
public interface SolarEdgeHandler extends ThingHandler {
    /**
     * Called from {@link WebInterface#authenticate()} to update
     * the thing status because updateStatus is protected.
     *
     * @param status       Bridge status
     * @param statusDetail Bridge status detail
     * @param description  Bridge status description
     */
    void setStatusInfo(ThingStatus status, ThingStatusDetail statusDetail, String description);

    /**
     * Provides the web interface object.
     *
     * @return The web interface object
     */
    @Nullable
    WebInterface getWebInterface();

    /**
     * method which updates the channels.
     *
     * @param values key-value list where key is the channel
     */
    void updateChannelStatus(Map<Channel, State> values);

    /**
     * return the binding's configuration
     *
     * @return
     */
    SolarEdgeConfiguration getConfiguration();

    /**
     * returns a list containing all channels
     *
     * @return
     */
    List<Channel> getChannels();
}
