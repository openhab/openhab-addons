/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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
package org.openhab.binding.valloxmv.internal;

/**
 * The {@link ValloxMVConfig} class holds the configuration properties of the thing.
 *
 * @author Björn Brings - Initial contribution
 */

public class ValloxMVConfig {
    private String ip;
    private int updateinterval;

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public int getUpdateinterval() {
        return updateinterval;
    }

    public void setUpdateinterval(int updateinterval) {
        this.updateinterval = updateinterval;
    }
}
