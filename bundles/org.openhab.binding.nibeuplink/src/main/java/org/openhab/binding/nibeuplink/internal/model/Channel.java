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
    private final @Nullable String writeApiUrl;
    private final @Nullable String validationExpression;

    /**
     * constructor for channels with write access enabled wihtout a unit
     *
     * @param id identifier of the channel
     * @param name human readable name
     * @param channelGroup group of the channel
     * @param writeApiUrl API URL for channel updates
     * @param validationExpression expression to validate values before sent to the API
     */
    Channel(String id, String name, ChannelGroup channelGroup, @Nullable String writeApiUrl,
            @Nullable String validationExpression) {
        this.channelCode = id;
        this.id = id;
        this.name = name;
        this.channelGroup = channelGroup;
        this.writeApiUrl = writeApiUrl;
        this.validationExpression = validationExpression;
    }

    /**
     * constructor for channels without write access and without unit
     *
     * @param id identifier of the channel
     * @param name human readable name
     * @param channelGroup group of the channel
     */
    Channel(String id, String name, ChannelGroup channelGroup) {
        this(id, name, channelGroup, null, null);
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
