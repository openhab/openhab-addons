/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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
package org.openhab.binding.easee.internal.handler;

import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.easee.internal.config.EaseeConfiguration;
import org.openhab.binding.easee.internal.connector.WebInterface;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.types.State;

/**
 * public interface of the {@link EaseeHandler}
 *
 * @author Alexander Friese - initial contribution
 */
@NonNullByDefault
public interface EaseeHandler extends ThingHandler, ChannelProvider {
    /**
     * Called from {@link WebInterface#authenticate()} to update
     * the thing status because updateStatus is protected.
     *
     * @param status Thing status
     * @param statusDetail Thing status detail
     * @param description Thing status description
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
    EaseeConfiguration getConfiguration();
}
