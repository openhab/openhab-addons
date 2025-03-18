/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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
import org.eclipse.jdt.annotation.Nullable;

/**
 * The {@link PushMediaQueueChangeTO} encapsulates PUSH_MEDIA_QUEUE_CHANGE messages
 *
 * @author Jan N. Klug - Initial contribution
 */
public class PushMediaQueueChangeTO extends PushDeviceTO {
    public String changeType;
    public @Nullable String playBackOrder;
    public boolean trackOrderChanged;
    public @Nullable String loopMode;

    @Override
    public @NonNull String toString() {
        return "PushMediaQueueChangeTO{changeType='" + changeType + "', playBackOrder='" + playBackOrder + "'"
                + ", trackOrderChanged=" + trackOrderChanged + ", loopMode='" + loopMode + "', destinationUserId='"
                + destinationUserId + "', dopplerId=" + dopplerId + "}";
    }
}
