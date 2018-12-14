/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.ihc.internal.profiles;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.smarthome.core.library.CoreItemFactory;
import org.eclipse.smarthome.core.thing.profiles.ProfileTypeBuilder;
import org.eclipse.smarthome.core.thing.profiles.ProfileTypeUID;
import org.eclipse.smarthome.core.thing.profiles.TriggerProfileType;

/**
 * IHC / ELKO profile constants.
 *
 * @author Pauli Anttila - initial contribution.
 *
 */
@NonNullByDefault
public interface IhcProfiles {

    ProfileTypeUID PUSHBUTTON_COMMAND = new ProfileTypeUID("ihc", "pushbutton-to-command");

    TriggerProfileType PUSHBUTTON_COMMAND_TYPE = ProfileTypeBuilder
            .newTrigger(PUSHBUTTON_COMMAND, "Push Button To Command")
            .withSupportedItemTypes(CoreItemFactory.DIMMER, CoreItemFactory.ROLLERSHUTTER, CoreItemFactory.CONTACT,
                    CoreItemFactory.SWITCH, CoreItemFactory.PLAYER)
            .build();

}
