/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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
package org.openhab.binding.viessmann.internal.interfaces;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;

/**
 * The {@link ApiInterface} is responsible for handling ViessmannApi
 *
 *
 * @author Ronny Grun - Initial contribution
 */
@NonNullByDefault
public interface ApiInterface {

    void setInstallationGatewayId(@Nullable String newInstallation, @Nullable String newGateway);

    void updateBridgeStatus(@Nullable ThingStatus status);

    void updateBridgeStatusExtended(@Nullable ThingStatus status, @Nullable ThingStatusDetail statusDetail,
            @Nullable String statusMessage);

    String getThingUIDasString();

    void waitForApiCallLimitReset(@Nullable Long limitReset);
}
