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
package org.openhab.binding.innogysmarthome.internal.client.entity.event;

/**
 * @author Oliver Kuhl - Initial contribution
 *
 */
public class EventData {
    private Integer configVersion;

    private String id;

    /**
     * @return the configVersion
     */
    public Integer getConfigVersion() {
        return configVersion;
    }

    /**
     * @param configVersion the configVersion to set
     */
    public void setConfigVersion(Integer configVersion) {
        this.configVersion = configVersion;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}
