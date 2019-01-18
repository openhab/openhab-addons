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
package org.openhab.binding.logreader.internal.config;

/**
 * Configuration class for {@link LogReaderBinding} binding.
 *
 * @author Pauli Anttila - Initial contribution
 */
public class LogReaderConfiguration {
    public String filePath;
    public int refreshRate;
    public String warningPatterns;
    public String warningBlacklistingPatterns;
    public String errorPatterns;
    public String errorBlacklistingPatterns;
    public String customPatterns;
    public String customBlacklistingPatterns;

    @Override
    public String toString() {
        return "[" + "filePath=" + filePath + ", refreshRate=" + refreshRate + ", warningPatterns=" + warningPatterns
                + ", warningBlacklistingPatterns=" + warningBlacklistingPatterns + ", errorPatterns=" + errorPatterns
                + ", errorBlacklistingPatterns=" + errorBlacklistingPatterns + ", customPatterns=" + customPatterns
                + ", customBlacklistingPatterns=" + customBlacklistingPatterns + "]";
    }
}
