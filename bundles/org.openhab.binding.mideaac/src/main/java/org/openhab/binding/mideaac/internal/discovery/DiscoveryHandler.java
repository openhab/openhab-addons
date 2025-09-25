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
package org.openhab.binding.mideaac.internal.discovery;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.config.discovery.DiscoveryResult;

/**
 * Discovery {@link DiscoveryHandler}
 * 
 * @author Jacek Dobrowolski - Initial contribution
 */
@NonNullByDefault
public interface DiscoveryHandler {
    /**
     * Discovery result
     * 
     * @param discoveryResult AC device
     */
    public void discovered(DiscoveryResult discoveryResult);
}
