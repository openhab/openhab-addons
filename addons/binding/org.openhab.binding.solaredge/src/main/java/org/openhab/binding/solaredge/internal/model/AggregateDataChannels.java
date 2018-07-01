/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.solaredge.internal.model;

/**
 * list of all available channels
 *
 * @author Alexander Friese - initial contribution
 */
public enum AggregateDataChannels implements Channel {

    /* DAY VALUES */

    DAY_PRODUCTION("production", "day production", ChannelType.AGGREGATE, ChannelGroup.AGGREGATE_DAY),

    DAY_CONSUMPTION("consumption", "day consumption", ChannelType.AGGREGATE, ChannelGroup.AGGREGATE_DAY),

    DAY_SELFCONSUMPTIONFORCONSUMPTION("selfConsumptionForConsumption", "day self consumption", ChannelType.AGGREGATE,
            ChannelGroup.AGGREGATE_DAY),

    DAY_SELFCONSUMPTIONCOVERAGE("selfConsumptionCoverage", "day self consumption coverage", ChannelType.AGGREGATE,
            ChannelGroup.AGGREGATE_DAY),

    DAY_BATTERYSELFCONSUMPTION("batterySelfConsumption", "day battery self consumption", ChannelType.AGGREGATE,
            ChannelGroup.AGGREGATE_DAY),

    DAY_IMPORT("import", "day import", ChannelType.AGGREGATE, ChannelGroup.AGGREGATE_DAY),

    DAY_EXPORT("export", "day export", ChannelType.AGGREGATE, ChannelGroup.AGGREGATE_DAY),

    /* WEEK VALUES */

    WEEK_PRODUCTION("production", "week production", ChannelType.AGGREGATE, ChannelGroup.AGGREGATE_WEEK),

    WEEK_CONSUMPTION("consumption", "week consumption", ChannelType.AGGREGATE, ChannelGroup.AGGREGATE_WEEK),

    WEEK_SELFCONSUMPTIONFORCONSUMPTION("selfConsumptionForConsumption", "week self consumption", ChannelType.AGGREGATE,
            ChannelGroup.AGGREGATE_WEEK),

    WEEK_SELFCONSUMPTIONCOVERAGE("selfConsumptionCoverage", "week self consumption coverage", ChannelType.AGGREGATE,
            ChannelGroup.AGGREGATE_WEEK),

    WEEK_BATTERYSELFCONSUMPTION("batterySelfConsumption", "week battery self consumption", ChannelType.AGGREGATE,
            ChannelGroup.AGGREGATE_WEEK),

    WEEK_IMPORT("import", "week import", ChannelType.AGGREGATE, ChannelGroup.AGGREGATE_WEEK),

    WEEK_EXPORT("export", "week export", ChannelType.AGGREGATE, ChannelGroup.AGGREGATE_WEEK),

    /* MONTH VALUES */

    MONTH_PRODUCTION("production", "month production", ChannelType.AGGREGATE, ChannelGroup.AGGREGATE_MONTH),

    MONTH_CONSUMPTION("consumption", "month consumption", ChannelType.AGGREGATE, ChannelGroup.AGGREGATE_MONTH),

    MONTH_SELFCONSUMPTIONFORCONSUMPTION("selfConsumptionForConsumption", "month self consumption",
            ChannelType.AGGREGATE, ChannelGroup.AGGREGATE_MONTH),

    MONTH_SELFCONSUMPTIONCOVERAGE("selfConsumptionCoverage", "month self consumption coverage", ChannelType.AGGREGATE,
            ChannelGroup.AGGREGATE_MONTH),

    MONTH_BATTERYSELFCONSUMPTION("batterySelfConsumption", "month battery self consumption", ChannelType.AGGREGATE,
            ChannelGroup.AGGREGATE_MONTH),

    MONTH_IMPORT("import", "month import", ChannelType.AGGREGATE, ChannelGroup.AGGREGATE_MONTH),

    MONTH_EXPORT("export", "month export", ChannelType.AGGREGATE, ChannelGroup.AGGREGATE_MONTH),

    /* YEAR VALUES */

    YEAR_PRODUCTION("production", "year production", ChannelType.AGGREGATE, ChannelGroup.AGGREGATE_YEAR),

    YEAR_CONSUMPTION("consumption", "year consumption", ChannelType.AGGREGATE, ChannelGroup.AGGREGATE_YEAR),

    YEAR_SELFCONSUMPTIONFORCONSUMPTION("selfConsumptionForConsumption", "year self consumption", ChannelType.AGGREGATE,
            ChannelGroup.AGGREGATE_YEAR),

    YEAR_SELFCONSUMPTIONCOVERAGE("selfConsumptionCoverage", "year self consumption coverage", ChannelType.AGGREGATE,
            ChannelGroup.AGGREGATE_YEAR),

    YEAR_BATTERYSELFCONSUMPTION("batterySelfConsumption", "year battery self consumption", ChannelType.AGGREGATE,
            ChannelGroup.AGGREGATE_YEAR),

    YEAR_IMPORT("import", "year import", ChannelType.AGGREGATE, ChannelGroup.AGGREGATE_YEAR),

    YEAR_EXPORT("export", "year export", ChannelType.AGGREGATE, ChannelGroup.AGGREGATE_YEAR),

    /* END */
    ;

    private final String id;
    private final String name;
    private final ChannelType channelType;
    private final ChannelGroup channelGroup;

    /**
     * Constructor
     *
     * @param id
     * @param name
     * @param type
     */
    AggregateDataChannels(String id, String name, ChannelType channelType, ChannelGroup channelGroup) {
        this.id = id;
        this.name = name;
        this.channelType = channelType;
        this.channelGroup = channelGroup;
    }

    public static AggregateDataChannels fromFQName(String fqName) {
        for (AggregateDataChannels channel : AggregateDataChannels.values()) {
            if (channel.getFQName().equals(fqName)) {
                return channel;
            }
        }
        return null;
    }

    @Override
    public final String getName() {
        return name;
    }

    @Override
    public final String getId() {
        return id;
    }

    @Override
    public final ChannelType getChannelType() {
        return channelType;
    }

    @Override
    public ChannelGroup getChannelGroup() {
        return channelGroup;
    }

    @Override
    public String getFQName() {
        String fQName = getChannelGroup().toString().toLowerCase() + "#" + getId();
        return fQName;
    }
}
