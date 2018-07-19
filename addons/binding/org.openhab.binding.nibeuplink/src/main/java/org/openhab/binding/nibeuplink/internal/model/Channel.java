/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.nibeuplink.internal.model;

import javax.measure.Unit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * the channel class
 *
 * @author Alexander Friese - initial contribution
 */
@NonNullByDefault
public class Channel {

    protected String channelCode;
    private final String id;
    private final String name;
    private final ChannelGroup channelGroup;
    private final @Nullable Unit<?> unit;
    private final @Nullable String writeApiUrl;
    private final @Nullable String validationExpression;

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
    Channel(String id, String name, ChannelGroup channelGroup, @Nullable Unit<?> unit, @Nullable String writeApiUrl,
            @Nullable String validationExpression) {
        this.channelCode = id;
        this.id = id;
        this.name = name;
        this.channelGroup = channelGroup;
        this.unit = unit;
        this.writeApiUrl = writeApiUrl;
        this.validationExpression = validationExpression;
    }

    /**
     * constructor for channels with write access enabled wihtout a unit
     *
     * @param id
     * @param name
     * @param channelGroup
     * @param writeApiUrl
     * @param validationExpression
     */
    Channel(String id, String name, ChannelGroup channelGroup, String writeApiUrl, String validationExpression) {
        this(id, name, channelGroup, null, writeApiUrl, validationExpression);
    }

    /**
     * constructor for channels without write access
     *
     * @param id
     * @param name
     * @param channelGroup
     * @param unit
     */
    Channel(String id, String name, ChannelGroup channelGroup, Unit<?> unit) {
        this(id, name, channelGroup, unit, null, null);
    }

    /**
     * constructor for channels without write access and without unit
     *
     * @param id
     * @param name
     * @param channelGroup
     */
    Channel(String id, String name, ChannelGroup channelGroup) {
        this(id, name, channelGroup, null, null, null);
    }

    public final String getName() {
        return name;
    }

    public final String getId() {
        return id;
    }

    public final String getChannelCode() {
        return channelCode;
    }

    public ChannelGroup getChannelGroup() {
        return channelGroup;
    }

    public @Nullable Unit<?> getUnit() {
        return unit;
    }

    public String getFQName() {
        return channelGroup.toString().toLowerCase() + "#" + id;
    }

    public @Nullable String getWriteApiUrlSuffix() {
        return writeApiUrl;
    }

    public boolean isReadOnly() {
        String localCopy = writeApiUrl;
        return localCopy == null || localCopy.isEmpty();
    }

    public @Nullable String getValidationExpression() {
        return validationExpression;
    }
}
