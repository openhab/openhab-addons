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
package org.openhab.binding.nadreceiver.internal;

import java.util.EventListener;

import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;

/**
 * The {@link NadReceiverEventListener} interface contains methods to update different channels
 *
 * @author Marc Chételat - Initial contribution
 */
public interface NadReceiverEventListener extends EventListener {
    void updateThingStatus(ThingStatus status, ThingStatusDetail statusDetail, @Nullable String description);

    void updateSource(int source);

    void updateVolume(int volume);

    void updateMute(boolean mute);

    void updateModel(String model);

    void updatePowerStatus(boolean power);

    void addOrUpdateSourceName(String number, String sourceName);

    void addOrUpdateSourceState(String number, boolean enabled);
}
