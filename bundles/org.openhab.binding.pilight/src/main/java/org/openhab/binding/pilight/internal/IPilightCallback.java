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
package org.openhab.binding.pilight.internal;

import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.pilight.internal.dto.Config;
import org.openhab.binding.pilight.internal.dto.Status;
import org.openhab.binding.pilight.internal.dto.Version;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;

/**
 * Callback interface to signal any listeners that an update was received from pilight
 *
 * @author Jeroen Idserda - Initial contribution
 * @author Stefan Röllin - Port to openHAB 2 pilight binding
 * @author Niklas Dörfler - Port pilight binding to openHAB 3 + add device discovery
 */
@NonNullByDefault
public interface IPilightCallback {

    /**
     * Update thing status
     *
     * @param status status of thing
     * @param statusDetail status detail of thing
     * @param description description of thing status
     */
    void updateThingStatus(ThingStatus status, ThingStatusDetail statusDetail, @Nullable String description);

    /**
     * Update for one or more device received.
     *
     * @param allStatus list of Object containing list of devices that were updated and their current state
     */
    void statusReceived(List<Status> allStatus);

    /**
     * Configuration received.
     *
     * @param config Object containing configuration of pilight
     */
    void configReceived(Config config);

    /**
     * Version information received.
     *
     * @param version Object containing software version information of pilight daemon
     */
    void versionReceived(Version version);
}
