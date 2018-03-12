/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.knx.internal.ets.profiles;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.smarthome.core.thing.profiles.ProfileTypeBuilder;
import org.eclipse.smarthome.core.thing.profiles.ProfileTypeUID;
import org.eclipse.smarthome.core.thing.profiles.StateProfileType;

/**
 * KNX profile constants.
 *
 * @author Karel Goderis - Initial contribution
 *
 */
@NonNullByDefault
public interface KNXProfiles {

    ProfileTypeUID CONTROL = new ProfileTypeUID(KNXProfileTypeUID.KNX_SCOPE, "control");
    ProfileTypeUID LISTEN = new ProfileTypeUID(KNXProfileTypeUID.KNX_SCOPE, "listen");

    StateProfileType CONTROL_TYPE = ProfileTypeBuilder.newState(CONTROL, "Control").build();
    StateProfileType LISTEN_TYPE = ProfileTypeBuilder.newState(LISTEN, "Listen").build();
}
