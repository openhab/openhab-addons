/**
 * Copyright (c) 2010-2019 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.nibeuplink.handler;

import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.eclipse.smarthome.core.types.State;
import org.openhab.binding.nibeuplink.config.NibeUplinkConfiguration;
import org.openhab.binding.nibeuplink.internal.connector.UplinkWebInterface;
import org.openhab.binding.nibeuplink.internal.model.Channel;

/**
 * public interface of the {@link UplinkBaseHandler}
 *
 * @author Alexander Friese - initial contribution
 */
@NonNullByDefault
public interface NibeUplinkHandler extends ThingHandler, ChannelProvider {
    /**
     * Called from {@link NibeUplinkWebInterface#authenticate()} to update
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
    UplinkWebInterface getWebInterface();

    void updateChannelStatus(Map<Channel, State> values);

    NibeUplinkConfiguration getConfiguration();

}
