/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
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
package org.openhab.binding.amazonechocontrol.internal.dto.push;

import org.eclipse.jdt.annotation.NonNull;

/**
 * The {@link PushDndStateChangeTO} encapsulates PUSH_DND_STATE_CHANGE messages
 *
 * @author Martin Littkovsky - Initial contribution
 */
public class PushDndStateChangeTO extends PushDeviceTO {
    public boolean enabled;

    @Override
    public @NonNull String toString() {
        return "PushDndStateChangeTO{enabled=" + enabled + ", destinationUserId='" + destinationUserId + "', dopplerId="
                + dopplerId + "}";
    }
}
