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
package org.openhab.binding.solaredge.internal.handler;

import java.util.List;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.eclipse.smarthome.core.types.State;
import org.openhab.binding.solaredge.internal.config.SolarEdgeConfiguration;
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
     * @param status Bridge status
     * @param statusDetail Bridge status detail
     * @param description Bridge status description
     */
    void setStatusInfo(ThingStatus status, ThingStatusDetail statusDetail, String description);

    /**
     * Provides the web interface object.
     *
     * @return The web interface object
     */
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
