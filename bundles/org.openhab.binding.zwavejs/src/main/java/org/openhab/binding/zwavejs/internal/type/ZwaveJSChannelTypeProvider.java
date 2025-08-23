/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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
package org.openhab.binding.zwavejs.internal.type;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.thing.type.ChannelType;
import org.openhab.core.thing.type.ChannelTypeProvider;

/**
 * This interface represents a provider for Z-Wave JS channel types.
 * It extends the {@link ChannelTypeProvider} interface and provides
 * additional functionality specific to Z-Wave JS.
 * 
 * @see ChannelTypeProvider
 *
 * @author Leo Siepel - Initial contribution
 */
@NonNullByDefault
public interface ZwaveJSChannelTypeProvider extends ChannelTypeProvider {

    /*
     * Adds a new channel type to the provider.
     *
     * @param channelType the channel type to be added
     */
    void addChannelType(ChannelType channelType);
}
