/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.nibeuplink.internal.model;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * extension of the channel class which adds support of QuantityType
 *
 * @author Alexander Friese - initial contribution
 */
@NonNullByDefault
public class ScaledChannel extends Channel {

    static final double F01 = 0.1;
    static final double F001 = 0.01;

    private final double factor;

    /**
     * constructor for channels with write access enabled + unit
     *
     * @param id
     * @param name
     * @param channelGroup
     * @param unit
     * @param writeApiUrl
     * @param validationExpression
     */
    ScaledChannel(String id, String name, ChannelGroup channelGroup, double factor, @Nullable String writeApiUrl,
            @Nullable String validationExpression) {
        super(id, name, channelGroup, writeApiUrl, validationExpression);
        this.factor = factor;
    }

    /**
     * constructor for channels without write access
     *
     * @param id
     * @param name
     * @param channelGroup
     * @param unit
     */
    ScaledChannel(String id, String name, ChannelGroup channelGroup, double factor) {
        this(id, name, channelGroup, factor, null, null);
    }

    public final double getFactor() {
        return factor;
    }
}
