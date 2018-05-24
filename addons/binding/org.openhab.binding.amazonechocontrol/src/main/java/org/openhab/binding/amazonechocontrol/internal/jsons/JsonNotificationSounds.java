/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
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
 * The {@link JsonNotificationSounds} encapsulate the GSON data for a notification sounds
 *
 * @author Michael Geramb - Initial contribution
 */
@NonNullByDefault
public class JsonNotificationSounds {
    public @Nullable JsonNotificationSound @Nullable [] notificationSounds;
}
