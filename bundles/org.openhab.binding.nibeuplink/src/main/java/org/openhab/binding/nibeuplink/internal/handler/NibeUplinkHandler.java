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
package org.openhab.binding.nibeuplink.internal.handler;

import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.nibeuplink.internal.config.NibeUplinkConfiguration;
import org.openhab.binding.nibeuplink.internal.connector.UplinkWebInterface;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.types.State;

/**
 * public interface of the {@link UplinkBaseHandler}
 *
 * @author Alexander Friese - initial contribution
 */
@NonNullByDefault
public interface NibeUplinkHandler extends ThingHandler, ChannelProvider {
    /**
     * Called from
     * {@link org.openhab.binding.nibeuplink.internal.connector.UplinkWebInterface.WebRequestExecutor#authenticate()}
     * to update the thing status because updateStatus is protected.
     *
     * @param status Bridge status
     * @param statusDetail Bridge status detail
     * @param description Bridge status description
     */
    void setStatusInfo(ThingStatus status, ThingStatusDetail statusDetail, @Nullable String description);

    /**
     * Provides the web interface object.
     *
     * @return The web interface object
     */
    UplinkWebInterface getWebInterface();

    void updateChannelStatus(Map<Channel, State> values);

    NibeUplinkConfiguration getConfiguration();
}
