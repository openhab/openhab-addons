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
package org.openhab.binding.gpstracker.internal.discovery;

import org.openhab.binding.gpstracker.internal.provider.ProviderType;

/**
 * The {@link TrackerDescription} is a tracker descriptor POJO
 *
 * @author Gabor Bicskei - Initial contribution
 */
public class TrackerDescription {
    private String id;
    private String name;
    private ProviderType type;

    public TrackerDescription(String id, String name, ProviderType type) {
        this.id = id;
        this.name = name;
        this.type = type;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public ProviderType getType() {
        return type;
    }
}
