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
package org.openhab.binding.satel.internal.config;

import java.nio.charset.Charset;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * The {@link SatelBridgeConfig} contains common configuration values for Satel bridge things.
 *
 * @author Krzysztof Goworek - Initial contribution
 */
@NonNullByDefault
public class SatelBridgeConfig {

    private int timeout;
    private int refresh;
    private @Nullable String userCode;
    private @Nullable String encoding;
    private boolean extCommands;

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
        final String userCode = this.userCode;
        return userCode == null ? "" : userCode;
    }

    /**
     * @return encoding for texts received from the bridge (names, descriptions)
     */
    public Charset getEncoding() {
        final String encoding = this.encoding;
        return encoding == null ? Charset.defaultCharset() : Charset.forName(encoding);
    }

    /**
     * @return <code>true</code> if the module supports extended commands
     */
    public boolean hasExtCommandsSupport() {
        return extCommands;
    }
}
