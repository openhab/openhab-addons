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
package org.openhab.transform.math.internal.profiles;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.thing.profiles.ProfileCallback;
import org.openhab.core.thing.profiles.ProfileContext;
import org.openhab.core.thing.profiles.ProfileTypeUID;
import org.openhab.core.transform.TransformationService;

/**
 * Profile to offer the {@link BitwiseAndTransformationService} on a ItemChannelLink.
 *
 * @author Christoph Weitkamp - Initial contribution
 * @author Jan N. Klug - Adapted To BoitwiseTransformations
 */
@NonNullByDefault
public class BitwiseAndTransformationProfile extends BitwiseTransformationProfile {

    public static final ProfileTypeUID PROFILE_TYPE_UID = new ProfileTypeUID(
            TransformationService.TRANSFORM_PROFILE_SCOPE, "BITAND");

    public BitwiseAndTransformationProfile(ProfileCallback callback, ProfileContext context,
            TransformationService service) {
        super(callback, context, service, PROFILE_TYPE_UID);
    }
}
