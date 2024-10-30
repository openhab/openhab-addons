/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
package org.openhab.binding.myuplink.internal.handler;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.thing.Channel;

/**
 * this interface provides all methods which deal with channels
 *
 * @author Alexander Friese - initial contribution
 */
@NonNullByDefault
public interface ChannelProvider {

    @Nullable
    Channel getChannel(String groupId, String channelId);

    @Nullable
    Channel getChannel(String uniqueChannelId);
}
