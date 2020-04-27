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
package org.openhab.binding.nibeuplink.internal.model;

import javax.measure.Unit;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * extension of the ScaledChannel class which adds support of QuantityType
 * write access is in general not support by this type of channel as Nibe cannot handle unit conversions
 *
 * @author Alexander Friese - initial contribution
 */
@NonNullByDefault
public class QuantityChannel extends ScaledChannel {

    private final Unit<?> unit;

    /**
     * constructor for channels with explicit scaling
     *
     * @param id identifier of the channel
     * @param name human readable name
     * @param channelGroup group of the channel
     * @param factor scaling factor
     * @param unit UoM unit
     */
    QuantityChannel(String id, String name, ChannelGroup channelGroup, ScaleFactor factor, Unit<?> unit) {
        super(id, name, channelGroup, factor, null, null);
        this.unit = unit;
    }

    /**
     * constructor for channels with defaulted scaling to 1
     *
     * @param id identifier of the channel
     * @param name human readable name
     * @param channelGroup group of the channel
     * @param unit UoM unit
     */
    QuantityChannel(String id, String name, ChannelGroup channelGroup, Unit<?> unit) {
        this(id, name, channelGroup, ScaleFactor.ONE, unit);
    }

    public Unit<?> getUnit() {
        return unit;
    }
}
