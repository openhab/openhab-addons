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
package org.openhab.binding.logreader.internal.config;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * Configuration class for LogReader binding.
 *
 * @author Pauli Anttila - Initial contribution
 */
@NonNullByDefault
public class LogReaderConfiguration {
    public String filePath = "${OPENHAB_LOGDIR}/openhab.log";
    public int refreshRate = 1000;
    public String warningPatterns = "WARN+";
    public @Nullable String warningBlacklistingPatterns;
    public String errorPatterns = "ERROR+";
    public @Nullable String errorBlacklistingPatterns;
    public @Nullable String customPatterns;
    public @Nullable String customBlacklistingPatterns;

    @Override
    public String toString() {
        return "[" + "filePath=" + filePath + ", refreshRate=" + refreshRate + ", warningPatterns=" + warningPatterns
                + ", warningBlacklistingPatterns=" + warningBlacklistingPatterns + ", errorPatterns=" + errorPatterns
                + ", errorBlacklistingPatterns=" + errorBlacklistingPatterns + ", customPatterns=" + customPatterns
                + ", customBlacklistingPatterns=" + customBlacklistingPatterns + "]";
    }
}
