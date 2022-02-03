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
package org.openhab.binding.lgthinq.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.lgthinq.internal.errors.LGThinqApiException;
import org.openhab.binding.lgthinq.lgservices.model.ACCapability;
import org.openhab.binding.lgthinq.lgservices.model.LGDevice;

/**
 * The {@link LGThinqDeviceThing} is a main interface contract for all LG Thinq things
 *
 * @author Nemer Daud - Initial contribution
 */
@NonNullByDefault
public interface LGThinqDeviceThing {

    void onDeviceAdded(@NonNullByDefault LGDevice device);

    String getDeviceId();

    String getDeviceAlias();

    String getDeviceModelName();

    String getDeviceUriJsonConfig();

    boolean onDeviceStateChanged();

    void onDeviceRemoved();

    void onDeviceGone();

    void updateChannelDynStateDescription() throws LGThinqApiException;

    ACCapability getAcCapabilities() throws LGThinqApiException;
}
