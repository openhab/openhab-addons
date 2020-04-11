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
package org.openhab.binding.smarther.internal.handler;

import static org.openhab.binding.smarther.internal.SmartherBindingConstants.DATE_FORMAT;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.binding.BaseDynamicStateDescriptionProvider;
import org.eclipse.smarthome.core.thing.type.DynamicStateDescriptionProvider;
import org.eclipse.smarthome.core.types.StateOption;
import org.joda.time.DateTime;
import org.openhab.binding.smarther.internal.api.model.Program;
import org.osgi.service.component.annotations.Component;

/**
 * Dynamically create the users list of programs and setting dates.
 *
 * @author Fabio Possieri - Initial contribution
 */
@Component(service = { DynamicStateDescriptionProvider.class, SmartherDynamicStateDescriptionProvider.class })
@NonNullByDefault
public class SmartherDynamicStateDescriptionProvider extends BaseDynamicStateDescriptionProvider {

    private static final String LABEL_FOREVER = "Forever";
    private static final String LABEL_TODAY = "Today";
    private static final String LABEL_TOMORROW = "Tomorrow";

    public void setEndDates(ChannelUID channelUID, int maxEndDays) {
        List<StateOption> endDates = new ArrayList<StateOption>();

        endDates.add(new StateOption("", LABEL_FOREVER));

        final DateTime today = DateTime.now().withTimeAtStartOfDay();
        endDates.add(new StateOption(today.toString(DATE_FORMAT), LABEL_TODAY));
        if (maxEndDays > 1) {
            endDates.add(new StateOption(today.plusDays(1).toString(DATE_FORMAT), LABEL_TOMORROW));

            for (int i = 2; i < maxEndDays; i++) {
                final String newDate = today.plusDays(i).toString(DATE_FORMAT);
                endDates.add(new StateOption(newDate, newDate));
            }
        }

        setStateOptions(channelUID, endDates);
    }

    public void setPrograms(ChannelUID channelUID, @Nullable List<Program> programs) {
        if (programs != null) {
            setStateOptions(channelUID,
                    programs.stream()
                            .map(program -> new StateOption(String.valueOf(program.getNumber()), program.getName()))
                            .collect(Collectors.toList()));
        }
    }

}
