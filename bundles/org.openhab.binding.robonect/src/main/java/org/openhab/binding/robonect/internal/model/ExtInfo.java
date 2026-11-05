/* Copyright (c) 2010-2026 Contributors to the openHAB project
 * SPDX-License-Identifier: EPL-2.0 */
package org.openhab.binding.robonect.internal.model;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * Response to an extension-state request.
 *
 * @author Nico Brüttner - Initial contribution
 */
@NonNullByDefault
public class ExtInfo extends RobonectAnswer {
    private @Nullable Extensions ext;

    /**
     * Returns the extension configuration.
     *
     * @return the extension configuration, or {@code null} if absent
     */
    public @Nullable Extensions getExt() {
        return ext;
    }
}
