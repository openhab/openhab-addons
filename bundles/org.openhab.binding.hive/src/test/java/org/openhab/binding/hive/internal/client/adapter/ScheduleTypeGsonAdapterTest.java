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
import java.util.Collections;
import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.hive.internal.client.HiveApiConstants;
import org.openhab.binding.hive.internal.client.ScheduleType;

/**
 *
 *
 * @author Ross Brown - Initial contribution
 */
@NonNullByDefault
public class ScheduleTypeGsonAdapterTest extends ComplexEnumGsonAdapterTest<ScheduleType, ScheduleTypeGsonAdapter> {
    @Override
    protected ScheduleTypeGsonAdapter getAdapter() {
        return new ScheduleTypeGsonAdapter();
    }

    @Override
    protected List<List<Object>> getGoodParams() {
        return Collections.singletonList(
                Arrays.asList(
                        ScheduleType.WEEKLY,
                        HiveApiConstants.SCHEDULE_TYPE_WEEKLY
                )
        );
    }

    @Override
    protected ScheduleType getUnexpectedEnum() {
        return ScheduleType.UNEXPECTED;
    }

    @Override
    protected String getUnexpectedString() {
        return "http://alertme.com/schema/json/configuration/configuration.device.schedule.unexpected.type.v1.json#";
    }
}
