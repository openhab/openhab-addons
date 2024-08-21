/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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
package org.openhab.binding.xmltv.internal.configuration;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link XmlChannelConfiguration} class contains fields mapping
 * Channel thing configuration parameters.
 *
 * @author Gaël L'hopital - Initial contribution
 */
@NonNullByDefault
public class XmlChannelConfiguration {
    public static final String CHANNEL_ID = "channelId";

    public String channelId = "";
    public int offset = 0;
    public int refresh = 60;
}
