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
package org.openhab.binding.knx.internal.console;

import java.nio.file.Path;
import java.time.Clock;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.knx.internal.KNXBindingConstants;
import org.openhab.binding.knx.internal.factory.KNXHandlerFactory;
import org.openhab.binding.knx.internal.handler.KNXBridgeBaseThingHandler;
import org.openhab.binding.knx.internal.monitor.MonitorStatus;
import org.openhab.binding.knx.internal.monitor.NotDecryptedReason;
import org.openhab.binding.knx.internal.monitor.SecurityState;
import org.openhab.binding.knx.internal.monitor.SendOutcome;
import org.openhab.binding.knx.internal.monitor.TelegramFilter;
import org.openhab.binding.knx.internal.monitor.TelegramFilterText;
import org.openhab.binding.knx.internal.monitor.TelegramMonitor;
import org.openhab.binding.knx.internal.monitor.TelegramRecord;
import org.openhab.binding.knx.internal.projectstore.KnxProjectStore;
import org.openhab.binding.knx.internal.security.KeyringFacts;
import org.openhab.binding.knx.internal.security.KeyringFacts.InterfaceFacts;
import org.openhab.core.OpenHAB;
import org.openhab.core.i18n.LocaleProvider;
import org.openhab.core.io.console.Console;
import org.openhab.core.io.console.ConsoleCommandCompleter;
import org.openhab.core.io.console.StringsCompleter;
import org.openhab.core.io.console.extensions.AbstractConsoleCommandExtension;
import org.openhab.core.io.console.extensions.ConsoleCommandExtension;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.util.HexUtils;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import io.calimero.IndividualAddress;

/**
 * The {@link KNXCommandExtension} is responsible for handling console commands
 *
 * @author Holger Friedrich - Initial contribution
 */
@NonNullByDefault
@Component(service = ConsoleCommandExtension.class)
public class KNXCommandExtension extends AbstractConsoleCommandExtension implements ConsoleCommandCompleter {

    private static final String CMD_LIST_UNKNOWN_GA = "list-unknown-ga";
    private static final String CMD_KEYRING = "keyring";
    private static final String CMD_MONITOR = "monitor";
    private static final String MONITOR_CLEAR = "clear";
    private static final StringsCompleter CMD_COMPLETER = new StringsCompleter(List.of(CMD_LIST_UNKNOWN_GA, CMD_KEYRING,
            CMD_MONITOR, ProjectCommands.CMD_PROJECT, ProjectCommands.CMD_CHANNELS, ProjectCommands.CMD_CHECK), false);
    private static final StringsCompleter PROJECT_WORD_COMPLETER = new StringsCompleter(ProjectCommands.PROJECT_WORDS,
            false);
    private static final int MONITOR_DEFAULT_ROWS = 50;
    private static final DateTimeFormatter ROW_TIME = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS",
            Locale.ROOT);

    private final KNXHandlerFactory knxHandlerFactory;
    private final Clock clock;
    private final ZoneId zone;
    private final ProjectCommands projectCommands;

    @Activate
    public KNXCommandExtension(final @Reference KNXHandlerFactory knxHandlerFactory,
            final @Reference KnxProjectStore projectStore, final @Reference LocaleProvider localeProvider) {
        this(knxHandlerFactory, projectStore, localeProvider, Clock.systemUTC(), ZoneId.systemDefault());
    }

    KNXCommandExtension(KNXHandlerFactory knxHandlerFactory, KnxProjectStore projectStore,
            LocaleProvider localeProvider, Clock clock, ZoneId zone) {
        this(knxHandlerFactory, projectStore, localeProvider, clock, zone,
                () -> Path.of(OpenHAB.getConfigFolder(), "misc"));
    }

    KNXCommandExtension(KNXHandlerFactory knxHandlerFactory, KnxProjectStore projectStore,
            LocaleProvider localeProvider, Clock clock, ZoneId zone, Supplier<Path> miscFolder) {
        super(KNXBindingConstants.BINDING_ID, "Interact with KNX devices.");
        this.knxHandlerFactory = knxHandlerFactory;
        this.clock = clock;
        this.zone = zone;
        this.projectCommands = new ProjectCommands(projectStore, localeProvider, this::sortedBridges, miscFolder, zone,
                this::buildCommandUsage);
    }

    @Override
    public void execute(String[] args, Console console) {
        if (args.length == 1 && CMD_LIST_UNKNOWN_GA.equalsIgnoreCase(args[0])) {
            for (KNXBridgeBaseThingHandler bridgeHandler : knxHandlerFactory.getBridges()) {
                console.println("KNX bridge \"" + bridgeHandler.getThing().getLabel()
                        + "\": group address, type, number of bytes, and number of occurrence since last reload of binding:");
                for (Entry<String, Long> entry : bridgeHandler.getCommandExtensionData().unknownGA().entrySet()) {
                    console.println(projectCommands.unknownAddressLine(bridgeHandler.getThing().getUID(),
                            entry.getKey(), entry.getValue()));
                }
            }
            return;
        }
        if (args.length >= 1 && args.length <= 2 && CMD_KEYRING.equalsIgnoreCase(args[0])) {
            printKeyring(args.length == 2 ? args[1] : null, console);
            return;
        }
        if (args.length >= 1 && CMD_MONITOR.equalsIgnoreCase(args[0])) {
            monitor(Arrays.copyOfRange(args, 1, args.length), console);
            return;
        }
        if (args.length >= 1 && ProjectCommands.CMD_PROJECT.equalsIgnoreCase(args[0])) {
            projectCommands.project(Arrays.copyOfRange(args, 1, args.length), console);
            return;
        }
        if (args.length >= 1 && ProjectCommands.CMD_CHANNELS.equalsIgnoreCase(args[0])) {
            projectCommands.channels(Arrays.copyOfRange(args, 1, args.length), console);
            return;
        }
        if (args.length >= 1 && ProjectCommands.CMD_CHECK.equalsIgnoreCase(args[0])) {
            projectCommands.checkProject(Arrays.copyOfRange(args, 1, args.length), console);
            return;
        }
        printUsage(console);
    }

    List<KNXBridgeBaseThingHandler> sortedBridges() {
        return knxHandlerFactory.getBridges().stream()
                .sorted(Comparator.comparing(handler -> handler.getThing().getUID().toString())).toList();
    }

    /** Finds a bridge by its UID, its id (any case) or its label. */
    static Optional<KNXBridgeBaseThingHandler> findBridge(String bridgeName, List<KNXBridgeBaseThingHandler> bridges) {
        return bridges.stream().filter(handler -> {
            ThingUID uid = handler.getThing().getUID();
            return bridgeName.equalsIgnoreCase(uid.toString()) || bridgeName.equalsIgnoreCase(uid.getId())
                    || bridgeName.equals(handler.getThing().getLabel());
        }).findFirst();
    }

    static void printUnknownBridge(String bridgeName, List<KNXBridgeBaseThingHandler> bridges, Console console) {
        console.println("No KNX bridge \"" + bridgeName + "\". Known bridges: " + bridges.stream()
                .map(handler -> handler.getThing().getUID().toString()).collect(Collectors.joining(", ")));
    }

    /** What the arguments of {@code monitor} ask for. */
    private record MonitorRequest(@Nullable String bridge, TelegramFilter filter, boolean clear) {
    }

    private void monitor(String[] args, Console console) {
        MonitorRequest request;
        try {
            request = parseMonitor(args);
        } catch (IllegalArgumentException e) {
            console.println(String.valueOf(e.getMessage()));
            return;
        }
        List<KNXBridgeBaseThingHandler> bridges = sortedBridges();
        List<KNXBridgeBaseThingHandler> chosen = bridges;
        String bridgeName = request.bridge();
        if (bridgeName != null) {
            Optional<KNXBridgeBaseThingHandler> found = findBridge(bridgeName, bridges);
            if (found.isEmpty()) {
                printUnknownBridge(bridgeName, bridges, console);
                return;
            }
            chosen = List.of(found.get());
        }
        if (chosen.isEmpty()) {
            console.println("No KNX bridge.");
        }
        for (KNXBridgeBaseThingHandler handler : chosen) {
            TelegramMonitor monitor = handler.getTelegramMonitor();
            if (request.clear()) {
                monitor.clear();
                console.println(describe(handler) + ": telegrams cleared.");
            } else {
                printMonitor(handler, monitor, request.filter(), console);
            }
        }
    }

    private MonitorRequest parseMonitor(String[] args) {
        String bridge = null;
        boolean clear = false;
        TelegramFilter.Builder filter = TelegramFilter.builder().limit(MONITOR_DEFAULT_ROWS);
        for (String arg : args) {
            int equals = arg.indexOf('=');
            if (equals < 0) {
                if (MONITOR_CLEAR.equalsIgnoreCase(arg)) {
                    clear = true;
                } else if (bridge == null) {
                    bridge = arg;
                } else {
                    throw new IllegalArgumentException("Cannot read \"" + arg + "\": only one bridge can be given.");
                }
                continue;
            }
            String key = arg.substring(0, equals).toLowerCase(Locale.ROOT);
            String value = arg.substring(equals + 1);
            try {
                TelegramFilterText.apply(filter, key, value, clock, zone);
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("Cannot read \"" + arg + "\": " + e.getMessage(), e);
            }
        }
        return new MonitorRequest(bridge, filter.build(), clear);
    }

    /** Prints the header and the rows; the rows carry plain values only, never a key. */
    private void printMonitor(KNXBridgeBaseThingHandler handler, TelegramMonitor monitor, TelegramFilter filter,
            Console console) {
        MonitorStatus status = monitor.status();
        MonitorStatus.Connection connection = status.connection();
        console.println(describe(handler) + ": "
                + (connection == null ? "not connected"
                        : connection.kind().text() + ", own address " + connection.ownAddress())
                + ", buffer " + status.bufferSize() + ", rows " + status.rows() + ", received " + status.received()
                + ", sent " + status.sent() + ", not decrypted " + status.notDecryptedTotal()
                + notDecryptedPerReason(status)
                + (status.plainOnSecuredAddress() > 0 ? ", plain on secured addresses " + status.plainOnSecuredAddress()
                        : ""));
        List<TelegramRecord> rows = monitor.query(filter);
        if (rows.isEmpty()) {
            console.println("  no telegrams");
        }
        for (TelegramRecord row : rows) {
            console.println(row(row));
        }
    }

    private static String notDecryptedPerReason(MonitorStatus status) {
        List<String> parts = new ArrayList<>();
        for (NotDecryptedReason reason : NotDecryptedReason.values()) {
            long count = status.notDecrypted().getOrDefault(reason, 0L);
            if (count > 0) {
                parts.add(reason + " " + count);
            }
        }
        if (status.notDecryptedOtherReason() > 0) {
            parts.add("other " + status.notDecryptedOtherReason());
        }
        return parts.isEmpty() ? "" : " (" + String.join(", ", parts) + ")";
    }

    private String row(TelegramRecord row) {
        String security = row.security().text();
        if (row.plainOnSecuredAddress()) {
            security += " on secured address";
        }
        if (row.reason() != null) {
            security += " (" + row.reason() + ")";
        } else if (row.security() == SecurityState.NOT_DECRYPTED) {
            security += " (unknown reason)";
        }
        String dataType = row.dataType();
        SendOutcome outcome = row.outcome();
        return String.format(Locale.ROOT, "  %6d %s %-3s %-8s %-8s -> %-8s %-7s %-7s %s%s", row.number(),
                ROW_TIME.format(row.time().atZone(zone)), row.direction().name().toLowerCase(Locale.ROOT),
                row.type().text(), named(row.source().toString(), row.sourceName()),
                named(row.destination().toString(), row.destinationName()), security, dataType == null ? "-" : dataType,
                value(row), outcome == null ? "" : "  " + outcome.text());
    }

    private static String named(String address, @Nullable String name) {
        return name == null ? address : address + " \"" + name + "\"";
    }

    private static String value(TelegramRecord row) {
        String value = row.value();
        if (value != null) {
            String unit = row.unit();
            return unit == null ? value : value + " " + unit;
        }
        byte[] payload = row.payload();
        return payload.length == 0 ? "-" : "raw " + HexUtils.bytesToHex(payload, " ");
    }

    private void printKeyring(@Nullable String bridgeName, Console console) {
        List<KNXBridgeBaseThingHandler> bridges = sortedBridges();
        if (bridgeName == null) {
            List<KNXBridgeBaseThingHandler> withKeyring = bridges.stream()
                    .filter(handler -> handler.getKeyringFacts() != null).toList();
            if (withKeyring.isEmpty()) {
                console.println("No KNX bridge has a keyring loaded.");
            }
            withKeyring.forEach(handler -> printKeyring(handler, console));
            return;
        }
        Optional<KNXBridgeBaseThingHandler> found = findBridge(bridgeName, bridges);
        if (found.isEmpty()) {
            printUnknownBridge(bridgeName, bridges, console);
        } else if (found.get().getKeyringFacts() == null) {
            console.println(describe(found.get()) + " has no keyring loaded.");
        } else {
            printKeyring(found.get(), console);
        }
    }

    static String describe(KNXBridgeBaseThingHandler handler) {
        return "KNX bridge \"" + handler.getThing().getLabel() + "\" (" + handler.getThing().getUID() + ")";
    }

    /** Prints what the keyring says; never a key, a password or a hash. */
    private static void printKeyring(KNXBridgeBaseThingHandler handler, Console console) {
        KeyringFacts facts = handler.getKeyringFacts();
        if (facts == null) {
            return;
        }
        console.println(describe(handler));
        console.println("  keyring file: " + handler.getKeyringFileName());
        console.println("  signature: " + (handler.isKeyringSignatureValid() ? "valid" : "NOT valid")
                + " for the configured password");
        console.println("  backbone key: " + (facts.hasBackboneKey() ? "present" : "not present"));
        console.println("  secured group addresses: " + facts.securedGroupAddresses().size());
        console.println("  interfaces: " + facts.interfaces().size());
        Optional<IndividualAddress> own = handler.getOwnAddress();
        for (InterfaceFacts iface : facts.interfaces()) {
            IndividualAddress host = iface.host();
            console.println(String.format("    %-8s %-10s host %-9s user %-4s linked secured group addresses: %d%s",
                    iface.address(), iface.type(), host == null ? "-" : host,
                    iface.userId() == 0 ? "-" : iface.userId(), iface.linkedSecuredGroups().size(),
                    own.isPresent() && own.get().equals(iface.address()) ? "  <- openHAB sends from here" : ""));
        }
    }

    @Override
    public List<String> getUsages() {
        List<String> usages = new ArrayList<>(List.of(
                buildCommandUsage(CMD_LIST_UNKNOWN_GA, "list group addresses which are not configured in openHAB"),
                buildCommandUsage(CMD_KEYRING + " [<bridge>]",
                        "check the ETS keyring of a bridge: password, interfaces and links, no keys are shown"),
                buildCommandUsage(CMD_MONITOR
                        + " [<bridge>] [src=<a>,..] [dst=<ga>,..] [type=write|read|response] [dir=in|out] [dpt=<dpt>,..]"
                        + " [from=<time>] [to=<time>] [context=<ms>] [limit=<n>]",
                        "show the last telegrams of a bridge, or of every bridge: the newest " + MONITOR_DEFAULT_ROWS
                                + " unless limit says otherwise, oldest first; time as -10m, 14:05:30, "
                                + "2026-11-14T14:05 or an instant ending in Z"),
                buildCommandUsage(CMD_MONITOR + " [<bridge>] " + MONITOR_CLEAR,
                        "forget the telegrams the monitor keeps; the running numbers go on")));
        ProjectCommands.usages().forEach(usage -> usages.add(buildCommandUsage(usage.syntax(), usage.description())));
        return usages;
    }

    @Override
    public @Nullable ConsoleCommandCompleter getCompleter() {
        return this;
    }

    @Override
    public boolean complete(String[] args, int cursorArgumentIndex, int cursorPosition, List<String> candidates) {
        if (cursorArgumentIndex <= 0) {
            return CMD_COMPLETER.complete(args, cursorArgumentIndex, cursorPosition, candidates);
        }
        String command = args.length > 0 ? args[0] : "";
        boolean project = ProjectCommands.CMD_PROJECT.equalsIgnoreCase(command);
        if (project && cursorArgumentIndex == 1) {
            return PROJECT_WORD_COMPLETER.complete(args, cursorArgumentIndex, cursorPosition, candidates);
        }
        boolean bridgeArgument = cursorArgumentIndex == 1 && (CMD_KEYRING.equalsIgnoreCase(command)
                || CMD_MONITOR.equalsIgnoreCase(command) || ProjectCommands.CMD_CHANNELS.equalsIgnoreCase(command)
                || ProjectCommands.CMD_CHECK.equalsIgnoreCase(command));
        if (project && cursorArgumentIndex == 2 && args.length > 1 && ProjectCommands.isProjectWord(args[1])) {
            bridgeArgument = true;
        }
        if (bridgeArgument) {
            return new StringsCompleter(knxHandlerFactory.getBridges().stream()
                    .map(handler -> handler.getThing().getUID().toString()).toList(), false)
                    .complete(args, cursorArgumentIndex, cursorPosition, candidates);
        }
        return false;
    }
}
