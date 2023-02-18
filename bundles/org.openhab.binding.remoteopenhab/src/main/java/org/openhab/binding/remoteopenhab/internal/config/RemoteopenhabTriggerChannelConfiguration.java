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
package org.openhab.binding.remoteopenhab.internal.config;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link RemoteopenhabTriggerChannelConfiguration} is responsible for holding
 * configuration informations associated to a remote openHAB trigger channel
 *
 * @author Laurent Garnier - Initial contribution
 */
@NonNullByDefault
public class RemoteopenhabTriggerChannelConfiguration {
    public static final String CHANNEL_UID = "channelUID";

    public String channelUID = "";
}
