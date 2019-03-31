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
package org.openhab.binding.innogysmarthome.internal.client.entity.link;

import org.openhab.binding.innogysmarthome.internal.client.entity.capability.Capability;

/**
 * Defines a {@link Link} to a {@link Capability}
 *
 * @author Oliver Kuhl - Initial contribution
 */
public class CapabilityLink extends Link {
    public static final String LINK_BASE = "/capability/";

    @Override
    public String getId() {
        return getValue().replace(LINK_BASE, "");
    }
}
