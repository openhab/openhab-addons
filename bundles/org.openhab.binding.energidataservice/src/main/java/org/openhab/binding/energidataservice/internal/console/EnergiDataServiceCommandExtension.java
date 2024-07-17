/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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
package org.openhab.binding.energidataservice.internal.console;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.energidataservice.internal.DatahubTariff;
import org.openhab.binding.energidataservice.internal.EnergiDataServiceBindingConstants;
import org.openhab.binding.energidataservice.internal.PriceComponent;
import org.openhab.binding.energidataservice.internal.exception.DataServiceException;
import org.openhab.binding.energidataservice.internal.handler.EnergiDataServiceHandler;
import org.openhab.core.io.console.Console;
import org.openhab.core.io.console.ConsoleCommandCompleter;
import org.openhab.core.io.console.StringsCompleter;
import org.openhab.core.io.console.extensions.AbstractConsoleCommandExtension;
import org.openhab.core.io.console.extensions.ConsoleCommandExtension;
import org.openhab.core.thing.ThingRegistry;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * The {@link EnergiDataServiceCommandExtension} is responsible for handling console commands.
 *
 * @author Jacob Laursen - Initial contribution
 */
@NonNullByDefault
@Component(service = ConsoleCommandExtension.class)
public class EnergiDataServiceCommandExtension extends AbstractConsoleCommandExtension {

    private static final String SUBCMD_UPDATE = "update";

    private static final StringsCompleter SUBCMD_COMPLETER = new StringsCompleter(List.of(SUBCMD_UPDATE), false);

    private final ThingRegistry thingRegistry;

    private class EnergiDataServiceConsoleCommandCompleter implements ConsoleCommandCompleter {
        @Override
        public boolean complete(String[] args, int cursorArgumentIndex, int cursorPosition, List<String> candidates) {
            if (cursorArgumentIndex <= 0) {
                return SUBCMD_COMPLETER.complete(args, cursorArgumentIndex, cursorPosition, candidates);
            } else if (cursorArgumentIndex == 1) {
                return new StringsCompleter(Stream.of(PriceComponent.values()).map(PriceComponent::toString).toList(),
                        false).complete(args, cursorArgumentIndex, cursorPosition, candidates);
            }
            return false;
        }
    }

    @Activate
    public EnergiDataServiceCommandExtension(final @Reference ThingRegistry thingRegistry) {
        super(EnergiDataServiceBindingConstants.BINDING_ID, "Interact with the Energi Data Service binding.");
        this.thingRegistry = thingRegistry;
    }

    @Override
    public void execute(String[] args, Console console) {
        if (args.length < 1) {
            printUsage(console);
            return;
        }

        switch (args[0].toLowerCase()) {
            case SUBCMD_UPDATE -> update(args, console);
            default -> printUsage(console);
        }
    }

    private void update(String[] args, Console console) {
        ParsedUpdateParameters updateParameters;
        try {
            updateParameters = new ParsedUpdateParameters(args);

            for (EnergiDataServiceHandler handler : thingRegistry.getAll().stream().map(thing -> thing.getHandler())
                    .filter(EnergiDataServiceHandler.class::isInstance).map(EnergiDataServiceHandler.class::cast)
                    .toList()) {
                Instant measureStart = Instant.now();
                int items = switch (updateParameters.priceComponent) {
                    case SPOT_PRICE ->
                        handler.updateSpotPriceTimeSeries(updateParameters.startDate, updateParameters.endDate);
                    default -> {
                        DatahubTariff datahubTariff = updateParameters.priceComponent.getDatahubTariff();
                        yield datahubTariff == null ? 0
                                : handler.updateTariffTimeSeries(datahubTariff, updateParameters.startDate,
                                        updateParameters.endDate);
                    }
                };
                Instant measureEnd = Instant.now();
                console.println(items + " prices updated as time series in "
                        + Duration.between(measureStart, measureEnd).toMillis() + " milliseconds.");
            }
        } catch (InterruptedException e) {
            console.println("Interrupted.");
        } catch (DataServiceException e) {
            console.println("Failed to fetch prices: " + e.getMessage());
        } catch (IllegalArgumentException e) {
            String message = e.getMessage();
            if (message != null) {
                console.println(message);
            }
            printUsage(console);
            return;
        }
    }

    private class ParsedUpdateParameters {
        PriceComponent priceComponent;
        LocalDate startDate;
        LocalDate endDate;

        private int ARGUMENT_POSITION_PRICE_COMPONENT = 1;
        private int ARGUMENT_POSITION_START_DATE = 2;
        private int ARGUMENT_POSITION_END_DATE = 3;

        ParsedUpdateParameters(String[] args) {
            if (args.length < 3 || args.length > 4) {
                throw new IllegalArgumentException("Incorrect number of parameters");
            }

            priceComponent = PriceComponent.fromString(args[ARGUMENT_POSITION_PRICE_COMPONENT].toLowerCase());

            try {
                startDate = LocalDate.parse(args[ARGUMENT_POSITION_START_DATE]);
            } catch (DateTimeParseException e) {
                throw new IllegalArgumentException("Invalid start date: " + e.getMessage(), e);
            }

            try {
                endDate = args.length == 3 ? startDate : LocalDate.parse(args[ARGUMENT_POSITION_END_DATE]);
            } catch (DateTimeParseException e) {
                throw new IllegalArgumentException("Invalid end date: " + e.getMessage(), e);
            }

            if (endDate.isBefore(startDate)) {
                throw new IllegalArgumentException("End date must be equal to or higher than start date");
            }

            if (endDate.isAfter(LocalDate.now())) {
                throw new IllegalArgumentException("Future end date is not allowed");
            }
        }
    }

    @Override
    public List<String> getUsages() {
        return Arrays.asList(buildCommandUsage(SUBCMD_UPDATE + " ["
                + String.join("|", Stream.of(PriceComponent.values()).map(PriceComponent::toString).toList())
                + "] <StartDate> [<EndDate>]", "Update time series in requested period"));
    }

    @Override
    public @Nullable ConsoleCommandCompleter getCompleter() {
        return new EnergiDataServiceConsoleCommandCompleter();
    }
}
