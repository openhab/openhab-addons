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
import org.openhab.binding.lgthinq.errors.LGApiException;
import org.openhab.binding.lgthinq.lgapi.model.ACCapability;
import org.openhab.binding.lgthinq.lgapi.model.LGDevice;

/**
 * The {@link LGDeviceThing} is a main interface contract for all LG Thinq things
 *
 * @author Nemer Daud - Initial contribution
 */
public interface LGDeviceThing {

    public void onDeviceAdded(@NonNullByDefault LGDevice device);

    public String getDeviceId();

    public String getDeviceAlias();

    public String getDeviceModelName();

    public String getDeviceUriJsonConfig();

    public boolean onDeviceStateChanged();

    public void onDeviceRemoved();

    public void onDeviceGone();

    public void updateChannelDynStateDescription() throws LGApiException;

    public ACCapability getAcCapabilities() throws LGApiException;
}
