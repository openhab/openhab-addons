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

import java.util.Arrays;
import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.hive.internal.client.GroupId;
import org.openhab.binding.hive.internal.client.HiveApiConstants;

/**
 *
 *
 * @author Ross Brown - Initial contribution
 */
@NonNullByDefault
public class GroupIdGsonAdapterTest extends ComplexEnumGsonAdapterTest<GroupId, GroupIdGsonAdapter> {
    @Override
    protected GroupIdGsonAdapter getAdapter() {
        return new GroupIdGsonAdapter();
    }

    @Override
    protected List<List<Object>> getGoodParams() {
        return Arrays.asList(
                Arrays.asList(
                        GroupId.TRVBM,
                        HiveApiConstants.GROUP_ID_TRVBM
                ),
                Arrays.asList(
                        GroupId.TRVS,
                        HiveApiConstants.GROUP_ID_TRVS
                )
        );
    }

    @Override
    protected GroupId getUnexpectedEnum() {
        return GroupId.UNEXPECTED;
    }

    @Override
    protected String getUnexpectedString() {
        return "unexpectedGroup";
    }
}
