/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
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

import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.solaredge.internal.config.SolarEdgeConfiguration;
import org.openhab.binding.solaredge.internal.connector.WebInterface;
import org.openhab.binding.solaredge.internal.model.AggregatePeriod;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.types.State;

/**
 * public interface of the {@link SolarEdgeHandler}
 *
 * @author Alexander Friese - initial contribution
 */
@NonNullByDefault
public interface SolarEdgeHandler extends ThingHandler, ChannelProvider {
    /**
     * Called from
     * {@link org.openhab.binding.solaredge.internal.connector.WebInterface.WebRequestExecutor#authenticate()}
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

    /** Returns the API V2 credential, refreshing an OAuth token when necessary. */
    String getPublicApiV2Credential();

    /** Returns whether API V2 can currently authenticate, without attempting a token refresh. */
    boolean hasPublicApiV2Credential();

    /** Invalidates the cached OAuth access token so the next request refreshes it. */
    void invalidatePublicApiV2Credential();

    /** Records an attempted Monitoring API V2 request and updates the Thing property. */
    void recordPublicApiV2Request();

    /** Updates rate-limit Thing properties from a Monitoring API V2 response. */
    void updatePublicApiV2RateLimit(@Nullable String limit, @Nullable String remaining, @Nullable String retryAfter);

    /** Supplies the latest V2 production power for deriving site consumption. */
    void updatePublicApiV2Production(long cycleId, @Nullable Double production);

    /** Supplies the latest V2 grid powers for deriving site consumption. */
    void updatePublicApiV2Grid(long cycleId, @Nullable Double imported, @Nullable Double exported,
            @Nullable Double consumption);

    /** Supplies the latest V2 storage powers for deriving site consumption. */
    void updatePublicApiV2Storage(long cycleId, @Nullable Double charged, @Nullable Double discharged,
            @Nullable Double level);

    /** Supplies V2 production energy for deriving aggregate consumption. */
    void updatePublicApiV2AggregateProduction(long cycleId, AggregatePeriod period, @Nullable Double production);

    /** Supplies V2 grid energies for deriving aggregate consumption. */
    void updatePublicApiV2AggregateGrid(long cycleId, AggregatePeriod period, @Nullable Double imported,
            @Nullable Double exported, @Nullable Double consumption);

    /** Supplies V2 storage energies for deriving aggregate consumption. */
    void updatePublicApiV2AggregateStorage(long cycleId, AggregatePeriod period, @Nullable Double charged,
            @Nullable Double discharged);
}
