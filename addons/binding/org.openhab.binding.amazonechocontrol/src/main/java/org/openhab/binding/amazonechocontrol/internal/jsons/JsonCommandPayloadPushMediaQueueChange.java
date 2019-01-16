/**
 * Copyright (c) 2010-2019 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.amazonechocontrol.internal.jsons;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * The {@link JsonCommandPayloadPushMediaQueueChange} encapsulate the GSON data of automation query
 *
 * @author Michael Geramb - Initial contribution
 */
@NonNullByDefault
public class JsonCommandPayloadPushMediaQueueChange extends JsonCommandPayloadPushDevice {
    public @Nullable String destinationUserId;
    public @Nullable String changeType;
    public @Nullable String playBackOrder;
    public @Nullable Boolean trackOrderChanged;
    public @Nullable Object loopMode;
}
