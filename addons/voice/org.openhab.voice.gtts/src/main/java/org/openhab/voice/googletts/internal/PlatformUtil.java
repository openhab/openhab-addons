/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.voice.googletts.internal;

import java.util.Locale;

import static org.openhab.voice.googletts.internal.PlatformUtil.Architecture.*;

/**
 * Utility to check system architecture.
 *
 * @author Gabor Bicskei - Initial contribution
 */
class PlatformUtil {
    private static final String SYSTEM_PROPERTY = "os.arch";

    //available architectures
    public enum Architecture {
        X86_64,
        X86_32,
        ITANIUM_64,
        SPARC_32,
        SPARC_64,
        ARM_32,
        AARCH_64,
        PPC_32,
        PPC_64,
        PPCLE_64,
        S390_32,
        S390_64,
        UNKNOWN
    }

    static Architecture checkArchitecture() {
        String arch = System.getProperty(SYSTEM_PROPERTY);
        String normalizedArch = arch.toLowerCase(Locale.US).replaceAll("[^a-z0-9]+", "");
        if (normalizedArch.matches("^(x8664|amd64|ia32e|em64t|x64)$")) {
            return Architecture.X86_64;
        } else if (normalizedArch.matches("^(x8632|x86|i[3-6]86|ia32|x32)$")) {
            return X86_32;
        } else if (normalizedArch.matches("^(ia64|itanium64)$")) {
            return ITANIUM_64;
        } else if (normalizedArch.matches("^(sparc|sparc32)$")) {
            return SPARC_32;
        } else if (normalizedArch.matches("^(sparcv9|sparc64)$")) {
            return SPARC_64;
        } else if (normalizedArch.matches("^(arm|arm32)$")) {
            return ARM_32;
        } else if ("aarch64".equals(normalizedArch)) {
            return AARCH_64;
        } else if (normalizedArch.matches("^(ppc|ppc32)$")) {
            return PPC_32;
        } else if ("ppc64".equals(normalizedArch)) {
            return PPC_64;
        } else if ("ppc64le".equals(normalizedArch)) {
            return PPCLE_64;
        } else if ("s390".equals(normalizedArch)) {
            return S390_32;
        } else {
            return "s390x".equals(normalizedArch) ? S390_64 : UNKNOWN;
        }
    }
}
