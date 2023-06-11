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
package org.openhab.binding.livisismarthome.internal.client.api.entity.state;

/**
 * @author Oliver Kuhl - Initial contribution
 */
public abstract class BaseStateDTO {

    private String lastChanged;

    /**
     * @return the lastChanged
     */
    public String getLastChanged() {
        return lastChanged;
    }

    /**
     * @param lastChanged the lastChanged to set
     */
    public void setLastChanged(String lastChanged) {
        this.lastChanged = lastChanged;
    }
}
