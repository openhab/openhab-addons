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
package org.openhab.binding.linkplay.internal.client.http.dto;

import java.util.List;

/**
 * Response from /multiroom:getSlaveList
 * 
 * @author Dan Cunningham - Initial contribution
 */
public class SlaveListResponse {

    public int slaves;
    public String wmrmVersion;
    public int surround;
    public List<Slave> slaveList = List.of();

    /**
     * Master units will report greater than 0 slaves.
     * Slave units, or units that are not part of a multiroom group, will report 0 slaves.
     */
    public boolean isMaster() {
        return slaves > 0;
    }

    @Override
    public String toString() {
        return "SlaveListResponse [slaves=" + slaves + ", wmrmVersion=" + wmrmVersion + ", surround=" + surround
                + ", slaveList=" + slaveList + "]";
    }
}
