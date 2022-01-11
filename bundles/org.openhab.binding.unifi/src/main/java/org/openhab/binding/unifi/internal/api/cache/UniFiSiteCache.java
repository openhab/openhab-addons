/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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
package org.openhab.binding.unifi.internal.api.cache;

import org.openhab.binding.unifi.internal.api.model.UniFiSite;

/**
 * The {@link UniFiSiteCache} is a specific implementation of {@link UniFiCache} for the purpose of caching
 * {@link UniFiSite} instances.
 *
 * The cache uses the following prefixes: <code>id</code>, <code>name</code>, <code>desc</code>
 *
 * @author Matthew Bowman - Initial contribution
 */
public class UniFiSiteCache extends UniFiCache<UniFiSite> {

    public UniFiSiteCache() {
        super(PREFIX_ID, PREFIX_NAME, PREFIX_DESC);
    }

    @Override
    protected String getSuffix(UniFiSite site, String prefix) {
        switch (prefix) {
            case PREFIX_ID:
                return site.getId();
            case PREFIX_NAME:
                return site.getName();
            case PREFIX_DESC:
                return site.getDescription();
        }
        return null;
    }
}
