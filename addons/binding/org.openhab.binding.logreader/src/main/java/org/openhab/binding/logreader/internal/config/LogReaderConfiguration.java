/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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
