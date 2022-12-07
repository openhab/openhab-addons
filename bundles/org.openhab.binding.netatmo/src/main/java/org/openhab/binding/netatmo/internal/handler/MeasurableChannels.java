/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
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
package org.openhab.binding.netatmo.internal.handler;

import static org.openhab.binding.netatmo.internal.NetatmoBindingConstants.MEASURABLE_CHANNELS;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.types.State;
import org.openhab.binding.netatmo.internal.ChannelTypeUtils;

import io.swagger.client.CollectionFormats.CSVParams;
import io.swagger.client.model.NAMeasureResponse;

/**
 * {@link MeasurableChannels} is a helper class designed to handle
 * manipulation of requests and responses provided by calls to
 * someNetatmoApi.getMeasures(....)
 *
 * @author Gaël L'hopital - Initial contribution
 *
 */
public class MeasurableChannels {
    protected NAMeasureResponse measures;
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
            if (measures.getBody().size() > 0) {
                List<List<Float>> valueList = measures.getBody().get(0).getValue();
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

    public Optional<CSVParams> getAsCsv() {
        if (measuredChannels.size() > 0) {
            return Optional.of(new CSVParams(measuredChannels));
        }
        return Optional.empty();
    }

    public void setMeasures(NAMeasureResponse measures) {
        this.measures = measures;
    }
}
