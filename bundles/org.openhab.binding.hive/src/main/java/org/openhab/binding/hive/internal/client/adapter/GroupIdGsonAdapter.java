/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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
package org.openhab.binding.hive.internal.client.adapter;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.hive.internal.client.GroupId;
import org.openhab.binding.hive.internal.client.HiveApiConstants;

/**
 * A gson {@link com.google.gson.TypeAdapter} for {@link GroupId}.
 *
 * @author Ross Brown - Initial contribution
 */
@NonNullByDefault
public final class GroupIdGsonAdapter extends ComplexEnumGsonTypeAdapterBase<GroupId> {
    public GroupIdGsonAdapter() {
        super(EnumMapper.builder(GroupId.class)
                .setUnexpectedValue(GroupId.UNEXPECTED)
                .add(GroupId.TRVBM, HiveApiConstants.GROUP_ID_TRVBM)
                .add(GroupId.TRVS, HiveApiConstants.GROUP_ID_TRVS)
                .build());
    }
}
