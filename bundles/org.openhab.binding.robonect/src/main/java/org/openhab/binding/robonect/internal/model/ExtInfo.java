/* Copyright (c) 2010-2026 Contributors to the openHAB project
 * SPDX-License-Identifier: EPL-2.0 */
package org.openhab.binding.robonect.internal.model;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/** Response to an extension-state request. */
@NonNullByDefault
public class ExtInfo extends RobonectAnswer {
    private @Nullable Extensions ext;

    public @Nullable Extensions getExt() { return ext; }
}
