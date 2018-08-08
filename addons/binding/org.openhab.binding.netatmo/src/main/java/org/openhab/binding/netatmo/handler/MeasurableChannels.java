/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.netatmo.handler;

import io.rudolph.netatmo.api.common.model.MeasureRequestResponse;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.types.State;
import org.openhab.binding.netatmo.internal.ChannelTypeUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.openhab.binding.netatmo.NetatmoBindingConstants.MEASURABLE_CHANNELS;

/**
 * {@link MeasurableChannels} is a helper class designed to handle
 * manipulation of requests and responses provided by calls to
 * someNetatmoApi.getMeasures(....)
 *
 * @author Gaël L'hopital - Initial contribution
 *
 */
public class MeasurableChannels {
    protected List<MeasureRequestResponse> measures;
    protected List<String> measuredChannels = new ArrayList<>();

    /*
     * If this channel value is provided as a measure, then add it
     * in the getMeasure parameter list
     */
    protected void addChannel(ChannelUID channelUID) {
        String channel = channelUID.getId();
        if (MEASURABLE_CHANNELS.contains(channel)) {
            measuredChannels.add(channel);
        }
    }

    /*
     * If this channel value is provided as a measure, then delete
     * it in the getMeasure parameter list
     */
    protected void removeChannel(ChannelUID channelUID) {
        String channel = channelUID.getId();
        measuredChannels.remove(channel);
    }

    protected Optional<State> getNAThingProperty(String channelId) {
        int index = measuredChannels.indexOf(channelId);
        if (index != -1 && measures != null) {
            if (measures.size() > 0) {
                List<List<Float>> valueList = measures.get(0).getValue();
                if (valueList.size() > 0) {
                    List<Float> values = valueList.get(0);
                    if (values.size() >= index) {
                        Float value = values.get(index);
                        return Optional.of(ChannelTypeUtils.toDecimalType(value));
                    }
                }
            }
        }
        return Optional.empty();
    }

    public void setMeasures(List<MeasureRequestResponse> measures) {
        this.measures = measures;
    }
}
