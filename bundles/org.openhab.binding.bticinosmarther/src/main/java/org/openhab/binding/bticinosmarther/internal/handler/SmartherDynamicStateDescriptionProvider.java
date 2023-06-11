/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
package org.openhab.binding.bticinosmarther.internal.handler;

import static org.openhab.binding.bticinosmarther.internal.SmartherBindingConstants.DTF_DATE;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.bticinosmarther.internal.api.dto.Program;
import org.openhab.binding.bticinosmarther.internal.util.DateUtil;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.binding.BaseDynamicStateDescriptionProvider;
import org.openhab.core.thing.type.DynamicStateDescriptionProvider;
import org.openhab.core.types.StateOption;
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
        List<StateOption> endDates = new ArrayList<>();

        endDates.add(new StateOption("", LABEL_FOREVER));

        final LocalDateTime today = LocalDate.now().atStartOfDay();

        endDates.add(new StateOption(DateUtil.format(today, DTF_DATE), LABEL_TODAY));
        if (maxEndDays > 1) {
            endDates.add(new StateOption(DateUtil.format(today.plusDays(1), DTF_DATE), LABEL_TOMORROW));
            for (int i = 2; i < maxEndDays; i++) {
                final String newDate = DateUtil.format(today.plusDays(i), DTF_DATE);
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
