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
package org.openhab.binding.teleinfo.internal.data;

import static org.openhab.binding.teleinfo.internal.TeleinfoBindingConstants.*;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.thing.ThingTypeUID;

/**
 * Define all the frame type values
 * 
 * @author Olivier MARCEAU - Initial contribution
 *
 */
@NonNullByDefault
public enum FrameType {
    CBETM_SHORT(null),
    CBETM_LONG_BASE(THING_BASE_CBETM_ELECTRICITY_METER_TYPE_UID),
    CBETM_LONG_EJP(THING_EJP_CBETM_ELECTRICITY_METER_TYPE_UID),
    CBETM_LONG_HC(THING_HC_CBETM_ELECTRICITY_METER_TYPE_UID),
    CBETM_LONG_TEMPO(THING_TEMPO_CBETM_ELECTRICITY_METER_TYPE_UID),
    CBEMM_BASE(THING_BASE_CBEMM_ELECTRICITY_METER_TYPE_UID),
    CBEMM_EJP(THING_EJP_CBEMM_ELECTRICITY_METER_TYPE_UID),
    CBEMM_HC(THING_HC_CBEMM_ELECTRICITY_METER_TYPE_UID),
    CBEMM_TEMPO(THING_TEMPO_CBEMM_ELECTRICITY_METER_TYPE_UID),
    CBEMM_ICC_BASE(THING_BASE_CBEMM_EVO_ICC_ELECTRICITY_METER_TYPE_UID),
    CBEMM_ICC_EJP(THING_EJP_CBEMM_EVO_ICC_ELECTRICITY_METER_TYPE_UID),
    CBEMM_ICC_TEMPO(THING_TEMPO_CBEMM_EVO_ICC_ELECTRICITY_METER_TYPE_UID),
    CBEMM_ICC_HC(THING_HC_CBEMM_EVO_ICC_ELECTRICITY_METER_TYPE_UID),
    LSMT_PROD(THING_LSMT_PROD_ELECTRICITY_METER_TYPE_UID),
    LSMM_PROD(THING_LSMM_PROD_ELECTRICITY_METER_TYPE_UID),
    LSMM(THING_LSMM_ELECTRICITY_METER_TYPE_UID),
    LSMT(THING_LSMT_ELECTRICITY_METER_TYPE_UID),
    UNKNOWN(null);

    private @Nullable ThingTypeUID thingTypeUid;

    FrameType(@Nullable ThingTypeUID thingTypeUid) {
        this.thingTypeUid = thingTypeUid;
    }

    public @Nullable ThingTypeUID getThingTypeUid() {
        return thingTypeUid;
    }
}
