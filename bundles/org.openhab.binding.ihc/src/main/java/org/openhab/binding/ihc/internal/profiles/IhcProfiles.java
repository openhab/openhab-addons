/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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
package org.openhab.binding.ihc.internal.profiles;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.library.CoreItemFactory;
import org.openhab.core.thing.profiles.ProfileTypeBuilder;
import org.openhab.core.thing.profiles.ProfileTypeUID;
import org.openhab.core.thing.profiles.TriggerProfileType;

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
