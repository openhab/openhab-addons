/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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
