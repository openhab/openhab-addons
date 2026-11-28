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

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Deque;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.knx.internal.handler.KNXBridgeBaseThingHandler;
import org.openhab.binding.knx.internal.project.KnxChannelMapper;
import org.openhab.binding.knx.internal.project.KnxChannelSuggestion;
import org.openhab.binding.knx.internal.project.KnxDatapointType;
import org.openhab.binding.knx.internal.project.KnxDevice;
import org.openhab.binding.knx.internal.project.KnxGroupAddress;
import org.openhab.binding.knx.internal.project.KnxGroupObject;
import org.openhab.binding.knx.internal.project.KnxIndividualAddress;
import org.openhab.binding.knx.internal.project.KnxProject;
import org.openhab.binding.knx.internal.project.KnxProjectCheck;
import org.openhab.binding.knx.internal.project.KnxProjectCheck.Difference;
import org.openhab.binding.knx.internal.project.KnxProjectCheck.Kind;
import org.openhab.binding.knx.internal.project.KnxProjectException;
import org.openhab.binding.knx.internal.project.KnxProjectInfo;
import org.openhab.binding.knx.internal.project.KnxSpace;
import org.openhab.binding.knx.internal.projectstore.ConfiguredAddresses;
import org.openhab.binding.knx.internal.projectstore.KnxProjectStore;
import org.openhab.binding.knx.internal.projectstore.KnxProjectStore.FileState;
import org.openhab.binding.knx.internal.projectstore.KnxProjectStore.StoreFailedException;
import org.openhab.binding.knx.internal.projectstore.ProjectNames;
import org.openhab.binding.knx.internal.projectstore.StoredProject;
import org.openhab.core.i18n.LocaleProvider;
import org.openhab.core.io.console.Console;
import org.openhab.core.thing.ThingUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.calimero.GroupAddress;

/**
 * The console commands that work with the stored ETS project of a bridge.
 *
 * @author Stamate Viorel - Initial contribution
 */
@NonNullByDefault
final class ProjectCommands {

    /** The syntax and the description of one command, as {@code getUsages()} lists it. */
    record Usage(String syntax, String description) {
    }

    static final String CMD_PROJECT = "project";
    static final String CMD_CHANNELS = "channels";
    static final String CMD_CHECK = "check-project";
    /** A channel UID and a key are long; a label is cut late only to bound a hostile one. */
    private static final int LABEL_LENGTH = 300;
    // the client pads the sub group with spaces: "31/7/ 1 GROUP_WRITE(01)"
    private static final Pattern UNKNOWN_KEY = Pattern.compile("^\\s*(\\d{1,2})/(\\d)/\\s*(\\d{1,3})\\b");
    private static final Pattern DEVICE = Pattern.compile("(\\d{1,2})\\.(\\d{1,2})\\.(\\d{1,3})");
    /** Control, format and line or paragraph separator characters: what {@code ProjectNames.clean} also takes out. */
    private static final Pattern CONTROL = Pattern.compile("[\\p{Cc}\\p{Cf}\\p{Zl}\\p{Zp}]");
    private static final String SUB_IMPORT = "import";
    private static final String SUB_INFO = "info";
    private static final String SUB_REMOVE = "remove";
    private static final String SUB_ADDRESSES = "addresses";
    private static final String SUB_DEVICES = "devices";
    private static final String LIMIT_PREFIX = "limit=";
    private static final int DEFAULT_ROWS = 50;
    private static final int MAX_ROWS = 10000;
    private static final DateTimeFormatter IMPORTED_AT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm", Locale.ROOT);

    /** The words that follow {@code project}. */
    static final List<String> PROJECT_WORDS = List.of(SUB_IMPORT, SUB_INFO, SUB_ADDRESSES, SUB_DEVICES, SUB_REMOVE);

    static boolean isProjectWord(String word) {
        return PROJECT_WORDS.stream().anyMatch(word::equalsIgnoreCase);
    }

    static final Usage USAGE_IMPORT = new Usage(CMD_PROJECT + " " + SUB_IMPORT + " <bridge> <file>",
            "read an ETS project (.knxproj) once and keep what the monitor and the other project commands need; "
                    + "<file> is an absolute path or the name of a file in conf/misc; "
                    + "a password is asked hidden, never given here");

    static final Usage USAGE_INFO = new Usage(CMD_PROJECT + " " + SUB_INFO + " [<bridge>]",
            "show the stored ETS project of a bridge, or of every bridge: name, import time, counts");

    static final Usage USAGE_ADDRESSES = new Usage(
            CMD_PROJECT + " " + SUB_ADDRESSES + " <bridge> [<filter>] [limit=<n>]",
            "list the group addresses of the stored project; a filter that starts with a digit is an address prefix "
                    + "such as 31/7, else a part of the name; " + DEFAULT_ROWS + " rows unless limit says otherwise");
    static final Usage USAGE_DEVICES = new Usage(CMD_PROJECT + " " + SUB_DEVICES + " <bridge> [<filter>] [limit=<n>]",
            "list the devices of the stored project; a filter that starts with a digit is an address prefix such as "
                    + "7.7, else a part of the name");

    static final Usage USAGE_REMOVE = new Usage(CMD_PROJECT + " " + SUB_REMOVE + " <bridge>",
            "forget the stored ETS project of a bridge, also one whose bridge no longer exists (give its UID)");

    static final Usage USAGE_CHANNELS = new Usage(CMD_CHANNELS + " [<bridge>] <device>",
            "print the channel lines the project suggests for a device, such as 7.7.1, to copy into a device Thing; "
                    + "nothing is created");

    static final Usage USAGE_CHECK = new Usage(CMD_CHECK + " [<bridge>]",
            "compare the group addresses of the channels with the stored project and list what differs");

    private static final String PASSWORD_PROMPT = "Password of the ETS project: ";
    private static final String NOTHING_IMPORTED = " Nothing imported.";
    private static final String EARLIER_IN_USE = " The earlier project is still in use.";

    private final Logger logger = LoggerFactory.getLogger(ProjectCommands.class);
    private final KnxProjectStore store;
    private final LocaleProvider localeProvider;
    private final Supplier<List<KNXBridgeBaseThingHandler>> bridges;
    private final Supplier<Path> miscFolder;
    private final ZoneId zone;
    private final BiFunction<String, String, String> usageText;

    ProjectCommands(KnxProjectStore store, LocaleProvider localeProvider,
            Supplier<List<KNXBridgeBaseThingHandler>> bridges, Supplier<Path> miscFolder, ZoneId zone,
            BiFunction<String, String, String> usageText) {
        this.store = store;
        this.localeProvider = localeProvider;
        this.bridges = bridges;
        this.miscFolder = miscFolder;
        this.zone = zone;
        this.usageText = usageText;
    }

    /** Every usage of the {@code project} commands, in the order {@code getUsages()} shows them. */
    static List<Usage> usages() {
        return List.of(USAGE_IMPORT, USAGE_INFO, USAGE_ADDRESSES, USAGE_DEVICES, USAGE_REMOVE, USAGE_CHANNELS,
                USAGE_CHECK);
    }

    /** Runs {@code project <word> ...}; {@code args} are the words after {@code project}. */
    void project(String[] args, Console console) {
        String word = args.length == 0 ? "" : args[0];
        String[] rest = Arrays.copyOfRange(args, Math.min(1, args.length), args.length);
        if (SUB_IMPORT.equalsIgnoreCase(word)) {
            importProject(rest, console);
        } else if (SUB_ADDRESSES.equalsIgnoreCase(word)) {
            addresses(rest, console);
        } else if (SUB_DEVICES.equalsIgnoreCase(word)) {
            devices(rest, console);
        } else if (SUB_REMOVE.equalsIgnoreCase(word)) {
            remove(rest, console);
        } else if (SUB_INFO.equalsIgnoreCase(word)) {
            info(rest, console);
        } else {
            usages().stream().filter(usage -> usage.syntax().startsWith(CMD_PROJECT + " "))
                    .forEach(usage -> usage(usage, console));
        }
    }

    private void usage(Usage usage, Console console) {
        console.printUsage(usageText.apply(usage.syntax(), usage.description()));
    }

    private void info(String[] args, Console console) {
        if (args.length > 1) {
            usage(USAGE_INFO, console);
            return;
        }
        List<KNXBridgeBaseThingHandler> all = bridges.get();
        List<KNXBridgeBaseThingHandler> chosen = all;
        if (args.length == 1) {
            Optional<KNXBridgeBaseThingHandler> found = KNXCommandExtension.findBridge(args[0], all);
            if (found.isEmpty()) {
                KNXCommandExtension.printUnknownBridge(args[0], all, console);
                return;
            }
            chosen = List.of(found.get());
        } else if (all.isEmpty()) {
            console.println("No KNX bridge.");
        }
        for (KNXBridgeBaseThingHandler handler : chosen) {
            StoredProject stored = usable(handler, console);
            if (stored != null) {
                printInfo(KNXCommandExtension.describe(handler), stored, console);
            }
        }
        if (args.length == 0) {
            Set<ThingUID> known = all.stream().map(handler -> handler.getThing().getUID()).collect(Collectors.toSet());
            for (ThingUID uid : store.stored()) {
                if (!known.contains(uid)) {
                    console.println("Stored project of a bridge that is not running (disabled or removed): " + uid
                            + ". If the bridge no longer exists, remove it with: openhab:knx " + CMD_PROJECT + " "
                            + SUB_REMOVE + " " + uid);
                }
            }
        }
    }

    /** The project of the bridge, or null after saying why there is none to use. */
    private @Nullable StoredProject usable(KNXBridgeBaseThingHandler handler, Console console) {
        ThingUID uid = handler.getThing().getUID();
        FileState state = store.fileState(uid);
        StoredProject stored = state == FileState.LOADED ? store.get(uid) : null;
        if (stored == null) {
            String describe = KNXCommandExtension.describe(handler);
            console.println(state == FileState.UNREADABLE || state == FileState.NEWER_VERSION
                    ? describe + ": the stored project cannot be used. Import it again."
                    : describe + ": no project. Import one with: openhab:knx " + CMD_PROJECT + " " + SUB_IMPORT
                            + " <bridge> <file>");
        }
        return stored;
    }

    private void printInfo(String describe, StoredProject stored, Console console) {
        KnxProject project = stored.project();
        String language = stored.language();
        console.println(describe + ": project \"" + ProjectNames.clean(project.info().name()) + "\", imported "
                + IMPORTED_AT.format(stored.importedAt().atZone(zone)) + " from \""
                + ProjectNames.clean(stored.sourceName()) + "\", texts: "
                + (language == null ? "default" : ProjectNames.clean(language)));
        long secured = project.groupAddresses().stream().filter(KnxGroupAddress::secured).count();
        int[] spacesAndFunctions = countSpaces(project.locations());
        console.println("  " + project.groupAddresses().size() + " group addresses (" + secured + " secured), "
                + project.topology().devices().size() + " devices, " + project.groupObjects().size()
                + " group objects, " + spacesAndFunctions[0] + " spaces, " + spacesAndFunctions[1] + " functions");
        KnxProjectInfo info = project.info();
        List<String> tool = new ArrayList<>();
        String createdBy = info.createdBy();
        String toolVersion = info.toolVersion();
        if (createdBy != null) {
            tool.add(ProjectNames.clean(createdBy));
        }
        if (toolVersion != null) {
            tool.add(ProjectNames.clean(toolVersion));
        }
        String lastModified = info.lastModified();
        List<String> parts = new ArrayList<>();
        if (!tool.isEmpty()) {
            parts.add("written by " + String.join(" ", tool));
        }
        if (lastModified != null) {
            parts.add("last change " + ProjectNames.clean(lastModified));
        }
        if (!parts.isEmpty()) {
            console.println("  " + String.join(", ", parts));
        }
    }

    /** {@return the number of spaces and the number of functions of the whole tree} */
    private static int[] countSpaces(List<KnxSpace> locations) {
        int spaces = 0;
        int functions = 0;
        Deque<KnxSpace> pending = new ArrayDeque<>(locations);
        while (!pending.isEmpty()) {
            KnxSpace space = pending.pop();
            spaces++;
            functions += space.functions().size();
            pending.addAll(space.spaces());
        }
        return new int[] { spaces, functions };
    }

    /** What {@code project addresses} and {@code project devices} are asked: a bridge, a filter, a limit. */
    private record ListRequest(String bridge, @Nullable String filter, int limit) {
    }

    private static @Nullable ListRequest parseList(String[] args) {
        if (args.length == 0) {
            return null;
        }
        String filter = null;
        Integer limit = null;
        for (int i = 1; i < args.length; i++) {
            String arg = args[i];
            if (arg.regionMatches(true, 0, LIMIT_PREFIX, 0, LIMIT_PREFIX.length())) {
                limit = limit == null ? parseLimit(arg.substring(LIMIT_PREFIX.length())) : null;
                if (limit == null) {
                    return null;
                }
            } else if (filter == null) {
                filter = arg;
            } else {
                return null;
            }
        }
        return new ListRequest(args[0], filter, limit == null ? DEFAULT_ROWS : limit);
    }

    private static @Nullable Integer parseLimit(String text) {
        if (text.isEmpty() || text.length() > 5 || !text.chars().allMatch(c -> c >= '0' && c <= '9')) {
            return null;
        }
        int limit = Integer.parseInt(text);
        return limit >= 1 && limit <= MAX_ROWS ? limit : null;
    }

    /** An address prefix when the filter starts with a digit, else a part of the name, ignoring case. */
    private static boolean matches(@Nullable String filter, @Nullable String address, String name) {
        if (filter == null || filter.isEmpty()) {
            return true;
        }
        char first = filter.charAt(0);
        if (first >= '0' && first <= '9') {
            return address != null && address.startsWith(filter);
        }
        return name.toLowerCase(Locale.ROOT).contains(filter.toLowerCase(Locale.ROOT));
    }

    private void addresses(String[] args, Console console) {
        ListRequest request = parseList(args);
        if (request == null) {
            usage(USAGE_ADDRESSES, console);
            return;
        }
        listing(request, console, "group addresses", project -> {
            List<String> rows = new ArrayList<>();
            List<KnxGroupAddress> sorted = new ArrayList<>(project.groupAddresses());
            sorted.sort(Comparator.comparingInt(KnxGroupAddress::rawAddress));
            for (KnxGroupAddress address : sorted) {
                String name = ProjectNames.clean(address.name());
                String text = ProjectNames.clean(address.address());
                if (matches(request.filter(), text, name)) {
                    String types = address.datapointTypes().isEmpty() ? "-"
                            : address.datapointTypes().stream().map(KnxDatapointType::toString)
                                    .collect(Collectors.joining(", "));
                    rows.add(text + "  " + types + "  " + (address.secured() ? "secure" : "-") + "  \"" + name + "\"");
                }
            }
            return new Rows(rows, project.groupAddresses().size());
        });
    }

    private void devices(String[] args, Console console) {
        ListRequest request = parseList(args);
        if (request == null) {
            usage(USAGE_DEVICES, console);
            return;
        }
        listing(request, console, "devices", project -> {
            List<KnxDevice> sorted = new ArrayList<>(project.topology().devices());
            sorted.sort(Comparator.comparing(KnxDevice::individualAddress,
                    Comparator.nullsLast(Comparator.comparingInt(KnxIndividualAddress::area)
                            .thenComparingInt(KnxIndividualAddress::line)
                            .thenComparingInt(KnxIndividualAddress::device))));
            List<String> rows = new ArrayList<>();
            for (KnxDevice device : sorted) {
                KnxIndividualAddress individual = device.individualAddress();
                String address = individual == null ? null : individual.toString();
                String name = ProjectNames.clean(project.displayNameOf(device));
                if (matches(request.filter(), address, name)) {
                    List<KnxGroupObject> objects = project.groupObjectsOf(device);
                    rows.add((address == null ? "-" : address) + "  \"" + name + "\"  " + objects.size() + " objects, "
                            + objects.stream().filter(KnxGroupObject::isLinked).count() + " linked");
                }
            }
            return new Rows(rows, project.topology().devices().size());
        });
    }

    /** The matching rows and how many there are before the filter. */
    private record Rows(List<String> matching, int total) {
    }

    private void listing(ListRequest request, Console console, String kind, Function<KnxProject, Rows> select) {
        List<KNXBridgeBaseThingHandler> all = bridges.get();
        Optional<KNXBridgeBaseThingHandler> found = KNXCommandExtension.findBridge(request.bridge(), all);
        if (found.isEmpty()) {
            KNXCommandExtension.printUnknownBridge(request.bridge(), all, console);
            return;
        }
        StoredProject stored = usable(found.get(), console);
        if (stored == null) {
            return;
        }
        Rows rows = select.apply(stored.project());
        console.println(KNXCommandExtension.describe(found.get()) + ": " + rows.matching().size() + " of "
                + rows.total() + " " + kind);
        rows.matching().stream().limit(request.limit()).forEach(console::println);
        int left = rows.matching().size() - request.limit();
        if (left > 0) {
            console.println(left + " more. Narrow the filter or use limit=<n>.");
        }
    }

    private void remove(String[] args, Console console) {
        if (args.length != 1) {
            usage(USAGE_REMOVE, console);
            return;
        }
        List<KNXBridgeBaseThingHandler> all = bridges.get();
        ThingUID uid = KNXCommandExtension.findBridge(args[0], all).map(handler -> handler.getThing().getUID())
                .orElse(null);
        if (uid == null) {
            uid = store.stored().stream().filter(stored -> stored.getAsString().equalsIgnoreCase(args[0])).findFirst()
                    .orElse(null);
        }
        if (uid == null) {
            KNXCommandExtension.printUnknownBridge(args[0], all, console);
            return;
        }
        try {
            console.println(store.remove(uid) ? "Removed the project of " + uid + "." : uid + ": no project.");
        } catch (IOException | RuntimeException e) {
            logger.debug("Removing the ETS project of {} failed: {}", uid, e.getClass().getSimpleName());
            console.println("The project file could not be removed.");
        }
    }

    /**
     * The bridge named, or the only one that has a stored project; null after saying why there is none to use. The
     * state of its project is not checked.
     */
    private @Nullable KNXBridgeBaseThingHandler chooseBridge(@Nullable String name, Console console) {
        List<KNXBridgeBaseThingHandler> all = bridges.get();
        if (name != null) {
            Optional<KNXBridgeBaseThingHandler> found = KNXCommandExtension.findBridge(name, all);
            if (found.isEmpty()) {
                KNXCommandExtension.printUnknownBridge(name, all, console);
            }
            return found.orElse(null);
        }
        List<KNXBridgeBaseThingHandler> withProject = all.stream()
                .filter(handler -> store.fileState(handler.getThing().getUID()) != FileState.NONE).toList();
        if (withProject.size() == 1) {
            return withProject.get(0);
        }
        console.println("Name the bridge.");
        if (!withProject.isEmpty()) {
            console.println("Bridges with a project: " + withProject.stream()
                    .map(handler -> handler.getThing().getUID().toString()).collect(Collectors.joining(", ")));
        }
        return null;
    }

    void channels(String[] args, Console console) {
        if (args.length < 1 || args.length > 2) {
            usage(USAGE_CHANNELS, console);
            return;
        }
        KnxIndividualAddress address = parseDevice(args[args.length - 1]);
        if (address == null) {
            console.println("Give the device as an individual address, for example 7.7.1.");
            return;
        }
        KNXBridgeBaseThingHandler handler = chooseBridge(args.length == 2 ? args[0] : null, console);
        StoredProject stored = handler == null ? null : usable(handler, console);
        if (stored == null) {
            return;
        }
        KnxProject project = stored.project();
        KnxDevice device = project.device(address);
        if (device == null) {
            console.println("No device " + address + " in the project.");
            return;
        }
        List<KnxChannelSuggestion> suggestions = new KnxChannelMapper(project).forDevice(device);
        console.println(address + " \"" + ProjectNames.clean(project.displayNameOf(device)) + "\": "
                + suggestions.size() + " channel lines");
        for (KnxChannelSuggestion suggestion : suggestions) {
            String type = suggestion.channelType();
            console.println("  " + (type == null ? "-" : ProjectNames.clean(type)) + "  "
                    + CONTROL.matcher(suggestion.text()).replaceAll("") + "  — \""
                    + ProjectNames.clean(suggestion.label()) + "\"");
            for (String note : suggestion.notes()) {
                console.println("      note: " + ProjectNames.clean(note));
            }
        }
        console.println("Nothing was created. Copy the lines you want into a channel of a device Thing.");
    }

    private static @Nullable KnxIndividualAddress parseDevice(String text) {
        Matcher matcher = DEVICE.matcher(text);
        if (!matcher.matches()) {
            return null;
        }
        int area = Integer.parseInt(matcher.group(1));
        int line = Integer.parseInt(matcher.group(2));
        int device = Integer.parseInt(matcher.group(3));
        return area <= 15 && line <= 15 && device <= 255 ? new KnxIndividualAddress(area, line, device) : null;
    }

    void checkProject(String[] args, Console console) {
        if (args.length > 1) {
            usage(USAGE_CHECK, console);
            return;
        }
        KNXBridgeBaseThingHandler handler = chooseBridge(args.length == 1 ? args[0] : null, console);
        StoredProject stored = handler == null ? null : usable(handler, console);
        if (handler == null || stored == null) {
            return;
        }
        ConfiguredAddresses configured = ConfiguredAddresses.of(handler.getThing());
        KnxProject project = stored.project();
        List<Difference> found = new KnxProjectCheck(project).check(configured.addresses());
        long notes = found.stream().filter(Difference::noteOnly).count();
        console.println(KNXCommandExtension.describe(handler) + ": " + configured.addresses().size()
                + " configured addresses checked, " + (found.size() - notes) + " differences, " + notes + " notes");
        for (Difference difference : found) {
            KnxGroupAddress inProject = difference.projectAddress();
            KnxDatapointType projectType = difference.projectType();
            String text = (difference.noteOnly() ? "  note: " : "  ") + difference.kind() + "  "
                    + (inProject == null ? new GroupAddress(difference.configured().rawAddress()).toString()
                            : ProjectNames.clean(inProject.address()))
                    + "  " + ProjectNames.clean(difference.configured().label(), LABEL_LENGTH);
            String configuredType = difference.configured().datapointType();
            if (difference.kind() == Kind.DATAPOINT_TYPE_DIFFERS && projectType != null && configuredType != null) {
                text += "  configured " + ProjectNames.clean(configuredType) + ", project " + projectType;
            }
            console.println(text);
        }
        if (found.isEmpty()) {
            console.println("No differences.");
        }
        if (configured.thingsWithoutHandler() > 0) {
            console.println(
                    configured.thingsWithoutHandler() + " device Things are not initialised and were not checked.");
        }
    }

    /**
     * {@return a line of {@code list-unknown-ga}: the key and the count, and the project's name of the address after
     * them when the key starts with a group address and the bridge's project knows it}
     */
    String unknownAddressLine(ThingUID bridge, String key, long count) {
        String line = key + "  " + count;
        Matcher matcher = UNKNOWN_KEY.matcher(key);
        if (!matcher.find()) {
            return line;
        }
        try {
            GroupAddress address = new GroupAddress(Integer.parseInt(matcher.group(1)),
                    Integer.parseInt(matcher.group(2)), Integer.parseInt(matcher.group(3)));
            String name = store.lookup(bridge).groupAddressName(address);
            return name == null ? line : line + "  \"" + ProjectNames.clean(name) + "\"";
        } catch (RuntimeException e) {
            return line;
        }
    }

    private void importProject(String[] args, Console console) {
        if (args.length != 2) {
            usage(USAGE_IMPORT, console);
            return;
        }
        List<KNXBridgeBaseThingHandler> all = bridges.get();
        Optional<KNXBridgeBaseThingHandler> found = KNXCommandExtension.findBridge(args[0], all);
        if (found.isEmpty()) {
            KNXCommandExtension.printUnknownBridge(args[0], all, console);
            return;
        }
        Path file = resolveFile(args[1]);
        if (file == null) {
            console.println("Give an absolute path, or the name of a file in conf/misc." + NOTHING_IMPORTED);
            return;
        }
        if (!Files.isRegularFile(file)) {
            console.println("File not found." + NOTHING_IMPORTED);
            return;
        }
        ThingUID uid = found.get().getThing().getUID();
        String language = localeProvider.getLocale().toLanguageTag();
        Attempt attempt = attempt(uid, file, null, language);
        if (attempt.reason() == KnxProjectException.Reason.PASSWORD_REQUIRED) {
            attempt = importWithPassword(uid, file, language, console);
        }
        StoredProject stored = attempt.stored();
        if (stored == null) {
            console.println(attempt.failure() + NOTHING_IMPORTED
                    + (store.fileState(uid) == FileState.LOADED ? EARLIER_IN_USE : ""));
            return;
        }
        KnxProject project = stored.project();
        console.println("Imported \"" + ProjectNames.clean(stored.sourceName()) + "\" for "
                + KNXCommandExtension.describe(found.get()) + ": " + project.groupAddresses().size()
                + " group addresses, " + project.topology().devices().size() + " devices, "
                + project.groupObjects().size() + " group objects.");
        if (project.ignoredInstallations() > 0) {
            console.println(project.ignoredInstallations() + " more installations in the file were ignored.");
        }
        console.println("The file was read once and is not kept.");
    }

    /** An absolute path as it is; a plain name from {@code conf/misc}; null for anything else. */
    @Nullable
    Path resolveFile(String argument) {
        try {
            Path path = Path.of(argument);
            if (!argument.isEmpty() && path.isAbsolute()) {
                return path;
            }
        } catch (InvalidPathException e) {
            return null;
        }
        if (argument.isEmpty() || argument.indexOf('/') >= 0 || argument.indexOf('\\') >= 0 || ".".equals(argument)
                || "..".equals(argument)) {
            return null;
        }
        return miscFolder.get().resolve(argument);
    }

    private Attempt importWithPassword(ThingUID uid, Path file, String language, Console console) {
        char[] password = askPassword(console);
        if (password == null) {
            return new Attempt(null, null, "The project is password protected and this console cannot ask for the "
                    + "password. Use the openHAB console over ssh.");
        }
        try {
            return attempt(uid, file, password, language);
        } finally {
            Arrays.fill(password, '\0');
        }
    }

    /** Asks once, hidden; null when the console cannot ask or the answer is empty. */
    private char @Nullable [] askPassword(Console console) {
        try {
            return Optional.ofNullable(console.readLine(PASSWORD_PROMPT, '*')).filter(answer -> !answer.isEmpty())
                    .map(String::toCharArray).orElse(null);
        } catch (IOException | RuntimeException e) {
            return null;
        }
    }

    /** What one call of the store gave: the project, or the reason and the fixed text of the failure. */
    private record Attempt(@Nullable StoredProject stored, KnxProjectException.@Nullable Reason reason,
            @Nullable String failure) {
    }

    private Attempt attempt(ThingUID uid, Path file, char @Nullable [] password, String language) {
        try {
            return new Attempt(store.importProject(uid, file, password, language), null, null);
        } catch (KnxProjectException e) {
            return new Attempt(null, e.getReason(), failureText(e.getReason()));
        } catch (StoreFailedException e) {
            return new Attempt(null, null, "The project was read, but the result could not be stored.");
        } catch (IOException e) {
            return new Attempt(null, null, "The file could not be read.");
        } catch (RuntimeException e) {
            logger.debug("Importing an ETS project failed: {}", e.getClass().getSimpleName());
            return new Attempt(null, null, "The file could not be read.");
        }
    }

    private static String failureText(KnxProjectException.Reason reason) {
        return switch (reason) {
            case NOT_A_PROJECT -> "This is not an ETS project file (.knxproj).";
            case PASSWORD_REQUIRED, WRONG_PASSWORD -> "Wrong password.";
            case UNSUPPORTED_PROTECTION -> "This kind of project protection is not supported yet.";
            case LIMIT_EXCEEDED -> "The file is larger than the reader accepts.";
            case MALFORMED -> "The file is damaged.";
        };
    }
}
