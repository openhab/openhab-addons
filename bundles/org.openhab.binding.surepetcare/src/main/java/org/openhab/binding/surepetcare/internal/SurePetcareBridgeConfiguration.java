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
package org.openhab.binding.surepetcare.internal;

import org.apache.commons.lang.StringUtils;

/**
 * The {@link SurePetcareBridgeConfiguration} is a container for all the bridge configuration.
 *
 * @author Rene Scherer - Initial contribution
 */
public class SurePetcareBridgeConfiguration {

    private String username;
    private String password;
    private Long refreshIntervalTopology;
    private Long refreshIntervalStatus;

    public String getUsername() {
        return StringUtils.trimToNull(username);
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return StringUtils.trimToNull(password);
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Long getRefreshIntervalTopology() {
        return refreshIntervalTopology;
    }

    public void setRefreshIntervalTopology(Long refreshIntervalTopology) {
        this.refreshIntervalTopology = refreshIntervalTopology;
    }

    public Long getRefreshIntervalStatus() {
        return refreshIntervalStatus;
    }

    public void setRefreshIntervalStatus(Long refreshIntervalStatus) {
        this.refreshIntervalStatus = refreshIntervalStatus;
    }

}
