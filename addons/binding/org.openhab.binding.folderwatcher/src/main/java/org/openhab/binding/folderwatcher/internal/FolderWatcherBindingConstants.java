/**
 * Copyright (c) 2010-2019 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.folderwatcher.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.smarthome.core.thing.ThingTypeUID;

/**
 * The {@link FolderWatcherBindingConstants} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Alexandr Salamatov - Initial contribution
 */
@NonNullByDefault
public class FolderWatcherBindingConstants {

    private static final String BINDING_ID = "folderwatcher";

    // List of all Thing Type UIDs
    public static final ThingTypeUID THING_TYPE_SAMPLE = new ThingTypeUID(BINDING_ID, "ftpfolder");

    // List of all Channel ids
    public static final String CHANNEL_1 = "newfile";
}
