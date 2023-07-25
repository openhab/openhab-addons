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
package org.openhab.binding.freeboxos.internal.config;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.freeboxos.internal.api.rest.LanBrowserManager;
import org.openhab.binding.freeboxos.internal.api.rest.LanBrowserManager.HostName;

/**
 * The {@link WifiHostConfiguration} holds configuration information needed to
 * access/poll a wifi network device
 *
 * @author Gaël L'hopital - Initial contribution
 */
@NonNullByDefault
public class WifiHostConfiguration extends HostConfiguration {
    private String mDNS = "";

    public @Nullable HostName getIdentifier() {
        if (!mDNS.isEmpty()) {
            return new HostName(mDNS, LanBrowserManager.Source.MDNS);
        }
        return null;
    }
}
