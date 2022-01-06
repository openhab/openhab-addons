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
package org.openhab.binding.shelly.internal.coap;

import java.util.List;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.shelly.internal.coap.ShellyCoapJSonDTO.CoIotDescrBlk;
import org.openhab.binding.shelly.internal.coap.ShellyCoapJSonDTO.CoIotDescrSen;
import org.openhab.binding.shelly.internal.coap.ShellyCoapJSonDTO.CoIotSensor;
import org.openhab.binding.shelly.internal.handler.ShellyColorUtils;
import org.openhab.core.types.State;

/**
 * The {@link ShellyCoapListener} describes the listening interface to process Coap responses
 *
 * @author Markus Michels - Initial contribution
 */
@NonNullByDefault
public interface ShellyCoIoTInterface {
    public int getVersion();

    public CoIotDescrSen fixDescription(@Nullable CoIotDescrSen sen, Map<String, CoIotDescrBlk> blkMap);

    public void completeMissingSensorDefinition(Map<String, CoIotDescrSen> sensorMap);

    public boolean handleStatusUpdate(List<CoIotSensor> sensorUpdates, CoIotDescrSen sen, int serial, CoIotSensor s,
            Map<String, State> updates, ShellyColorUtils col);

    public String getLastWakeup();
}
