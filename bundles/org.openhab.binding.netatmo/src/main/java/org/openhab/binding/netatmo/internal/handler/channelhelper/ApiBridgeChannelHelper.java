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
package org.openhab.binding.netatmo.internal.handler.channelhelper;

import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link ApiBridgeChannelHelper} handle specifics channels the Netatmo Bridge
 *
 * @author Gaël L'hopital - Initial contribution
 *
 */
@NonNullByDefault
public class ApiBridgeChannelHelper extends ChannelHelper {

    public ApiBridgeChannelHelper(Set<String> providedGroups) {
        super(providedGroups);
    }
}
