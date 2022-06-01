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
package org.openhab.binding.lgthinq.lgservices.model.fridge;

import org.openhab.binding.lgthinq.lgservices.model.LGAPIVerion;
import org.openhab.binding.lgthinq.lgservices.model.fridge.v2.FridgeCapabilityV2;
import org.openhab.binding.lgthinq.lgservices.model.fridge.v2.FridgeSnapshotV2;

/**
 * The {@link FridgeFactory}
 *
 * @author Nemer Daud - Initial contribution
 */
public class FridgeFactory {

    public static FridgeCapability getFridgeCapability(LGAPIVerion version) {
        switch (version) {
            case V1_0:
                throw new IllegalArgumentException("V1_0 not supported by Fridge Thing yet");
            case V2_0:
                return new FridgeCapabilityV2();
            default:
                throw new IllegalArgumentException("Version " + version + " not expected");
        }
    }

    public static FridgeSnapshot getFridgeSnapshot(LGAPIVerion version) {
        switch (version) {
            case V1_0:
                throw new IllegalArgumentException("V1_0 not supported by Fridge Thing yet");
            case V2_0:
                return new FridgeSnapshotV2();
            default:
                throw new IllegalArgumentException("Version " + version + " not expected");
        }
    }
}
