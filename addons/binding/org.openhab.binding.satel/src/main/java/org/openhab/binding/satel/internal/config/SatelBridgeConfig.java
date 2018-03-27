/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.satel.internal.config;

/**
 * The {@link SatelBridgeConfig} contains common configuration values for Satel bridge things.
 *
 * @author Krzysztof Goworek - Initial contribution
 */
public class SatelBridgeConfig {

    private int timeout;
    private int refresh;
    private String userCode;
    private String encoding;

    /**
     * @return value of timeout in milliseconds
     */
    public int getTimeout() {
        return timeout;
    }

    /**
     * @return polling interval in milliseconds
     */
    public int getRefresh() {
        return refresh;
    }

    /**
     * @return user code in behalf of all the commands will be executed
     */
    public String getUserCode() {
        return userCode;
    }

    /**
     * @return encoding for texts received from the bridge (names, descriptions)
     */
    public String getEncoding() {
        return encoding;
    }

}
