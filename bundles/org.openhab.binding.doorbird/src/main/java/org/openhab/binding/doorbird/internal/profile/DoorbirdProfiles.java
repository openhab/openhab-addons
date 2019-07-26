/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
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
package org.openhab.binding.doorbird.internal.profile;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.smarthome.core.library.CoreItemFactory;
import org.eclipse.smarthome.core.thing.profiles.ProfileTypeBuilder;
import org.eclipse.smarthome.core.thing.profiles.ProfileTypeUID;
import org.eclipse.smarthome.core.thing.profiles.TriggerProfileType;
import org.openhab.binding.doorbird.internal.DoorbirdBindingConstants;

/**
 * The {@link DoorbirdProfile} class defines Doorbird Profile constants.
 *
 * @author Mark Hilbush - Initial contribution
 *
 */
@NonNullByDefault
public interface DoorbirdProfiles {
    ProfileTypeUID DOORBELL_SWITCH_UID = new ProfileTypeUID(DoorbirdBindingConstants.BINDING_ID, "switch");

    TriggerProfileType DOORBELL_COMMAND_TYPE = ProfileTypeBuilder.newTrigger(DOORBELL_SWITCH_UID, "Doorbell Switch")
            .withSupportedItemTypes(CoreItemFactory.SWITCH).build();
}
