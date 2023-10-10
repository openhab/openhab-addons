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
package org.openhab.binding.velux.internal.utils;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.osgi.framework.FrameworkUtil;

/**
 * This is a helper class for dealing with information from MANIFEST file.
 *
 * It provides the following methods:
 * <ul>
 * <li>{@link #getBundleVersion} returns the bundle version as specified within the MANIFEST.</li>
 * </ul>
 * <p>
 *
 * @author Guenther Schreiner - Initial contribution
 */
@NonNullByDefault
public class ManifestInformation {

    /*
     * ************************
     * ***** Constructors *****
     */

    /**
     * Suppress default constructor for creating a non-instantiable class.
     */
    private ManifestInformation() {
        throw new AssertionError();
    }

    // Class access methods

    /**
     * Returns the bundle version as specified within the MANIFEST file.
     *
     * @return <B>bundleVersion</B> the resulted bundle version as {@link String}.
     */
    public static String getBundleVersion() {
        return FrameworkUtil.getBundle(ManifestInformation.class).getBundleContext().getBundle().toString();
    }
}
