/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
package org.openhab.binding.upnpcontrol.internal.services;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.jupnp.model.meta.RemoteDevice;
import org.jupnp.model.meta.RemoteService;
import org.jupnp.model.types.ServiceId;

/**
 * Class representing the configuration of the renderer. Instantiation will get configuration parameters from UPnP
 * {@link RemoteDevice}.
 *
 * @author Mark Herwege - Initial contribution
 */
@NonNullByDefault
public class UpnpRenderingControlConfiguration {
    protected static final String UPNP_RENDERING_CONTROL_SCHEMA = "urn:schemas-upnp-org:service:RenderingControl";

    public Set<String> audioChannels = Collections.emptySet();

    public boolean volume;
    public boolean mute;
    public boolean loudness;

    public long maxvolume = 100;

    public UpnpRenderingControlConfiguration() {
    }

    public UpnpRenderingControlConfiguration(@Nullable RemoteDevice device) {
        if (device == null) {
            return;
        }

        RemoteService rcService = device.findService(ServiceId.valueOf(UPNP_RENDERING_CONTROL_SCHEMA));
        if (rcService != null) {
            volume = (rcService.getStateVariable("Volume") != null);
            if (volume) {
                maxvolume = rcService.getStateVariable("Volume").getTypeDetails().getAllowedValueRange().getMaximum();
            }
            mute = (rcService.getStateVariable("Mute") != null);
            loudness = (rcService.getStateVariable("Loudness") != null);
            if (rcService.getStateVariable("A_ARG_TYPE_Channel") != null) {
                audioChannels = new HashSet<String>(Arrays
                        .asList(rcService.getStateVariable("A_ARG_TYPE_Channel").getTypeDetails().getAllowedValues()));
            }
        }
    }
}
