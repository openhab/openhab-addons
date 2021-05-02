/**
 * Copyright (c) 2010-2021 Contributors to the openHAB project
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
package org.openhab.binding.updateopenhab.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link TargetVersion} determines what type of update to apply
 *
 * @author Andrew Fiddian-Green - Initial contribution
 */
@NonNullByDefault
public enum TargetVersion {
    STABLE("stable"),
    MILESTONE("unstable"),
    SNAPSHOT("testing");

    public final String label;

    private TargetVersion(String label) {
        this.label = label;
    }
}
