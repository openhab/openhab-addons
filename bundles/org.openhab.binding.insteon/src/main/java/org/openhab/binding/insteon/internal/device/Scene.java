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
package org.openhab.binding.insteon.internal.device;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Interface for classes that represent a scene
 *
 * @author Jeremy Setton - Initial contribution
 */
@NonNullByDefault
public interface Scene {
    /**
     * Returns the group for this scene
     *
     * @return the scene group
     */
    public int getGroup();

    /**
     * Refreshes this scene
     */
    public void refresh();
}
