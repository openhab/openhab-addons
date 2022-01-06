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
package org.openhab.binding.bmwconnecteddrive.internal.utils;

import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.bmwconnecteddrive.internal.handler.RemoteServiceHandler.RemoteService;
import org.openhab.core.types.StateOption;

/**
 * Helper class for Remote Service Commands
 *
 * @author Norbert Truchsess - Initial contribution
 */
@NonNullByDefault
public class RemoteServiceUtils {

    private static final Map<String, RemoteService> COMMAND_SERVICES = Stream.of(RemoteService.values())
            .collect(Collectors.toUnmodifiableMap(RemoteService::getCommand, service -> service));

    private static final Set<RemoteService> ELECTRIC_SERVICES = EnumSet.of(RemoteService.CHARGE_NOW,
            RemoteService.CHARGING_CONTROL);

    public static Optional<RemoteService> getRemoteService(final String command) {
        return Optional.ofNullable(COMMAND_SERVICES.get(command));
    }

    public static List<StateOption> getOptions(final boolean isElectric) {
        return Stream.of(RemoteService.values())
                .filter(service -> isElectric ? true : !ELECTRIC_SERVICES.contains(service))
                .map(service -> new StateOption(service.getCommand(), service.getLabel()))
                .collect(Collectors.toUnmodifiableList());
    }
}
