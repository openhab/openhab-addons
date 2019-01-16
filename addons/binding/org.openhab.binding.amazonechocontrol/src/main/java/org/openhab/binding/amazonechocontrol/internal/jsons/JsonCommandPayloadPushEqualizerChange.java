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
 * The {@link JsonCommandPayloadPushEqualizerChange} encapsulate the GSON data of automation query
 *
 * @author Michael Geramb - Initial contribution
 */
@NonNullByDefault
public class JsonCommandPayloadPushEqualizerChange extends JsonCommandPayloadPushDevice {
    public @Nullable String destinationUserId;
    public @Nullable Integer bass;
    public @Nullable Integer treble;
    public @Nullable Integer midrange;
}
