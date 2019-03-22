/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
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
package org.openhab.binding.domintell.internal.protocol;

import org.openhab.binding.domintell.internal.protocol.model.Discoverable;

/**
 * {@link ConfigurationEventHandler} is a functional interface to handle Domintell setup changes.
 *
 * @author Gabor Bicskei - Initial contribution
 */
public interface ConfigurationEventHandler {
    /**
     * Event for new module/item group discovery.
     *
     * @param discoverable Object to discover
     */
    void handleNewDiscoverable(Discoverable discoverable);
}
