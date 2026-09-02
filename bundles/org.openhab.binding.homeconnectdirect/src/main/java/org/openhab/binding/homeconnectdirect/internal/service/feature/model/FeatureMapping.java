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
package org.openhab.binding.homeconnectdirect.internal.service.feature.model;

import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.homeconnectdirect.internal.common.DoubleKeyMap;

/**
 * Feature mapping model.
 *
 * @author Jonas Brüstel - Initial contribution
 */
@NonNullByDefault
public record FeatureMapping(Map<Integer, String> features, Map<Integer, String> errors,
        DoubleKeyMap<Integer, String, EnumDescription> enums) {

    public String mapFeatureIdToKey(Integer uid) {
        return features.getOrDefault(uid, String.valueOf(uid));
    }

    public String mapErrorIdToKey(Integer eid) {
        return errors.getOrDefault(eid, String.valueOf(eid));
    }

    public String mapEnumIdToKey(Integer enid) {
        EnumDescription desc = enums.getByKey1(enid);
        return desc != null ? desc.enKey() : String.valueOf(enid);
    }

    public @Nullable String mapEnumIdToKeyNullable(@Nullable Integer enid) {
        if (enid == null) {
            return null;
        }
        return mapEnumIdToKey(enid);
    }

    public String mapEnumValueToKey(Integer enid, Integer enumValue) {
        EnumDescription desc = enums.getByKey1(enid);
        if (desc != null) {
            String value = desc.values().get(enumValue);
            if (value != null) {
                return value;
            }
        }
        return String.valueOf(enumValue);
    }
}
