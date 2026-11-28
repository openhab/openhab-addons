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
package org.openhab.binding.knx.internal.projectstore;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.knx.internal.monitor.ProjectLookup;
import org.openhab.binding.knx.internal.project.KnxArea;
import org.openhab.binding.knx.internal.project.KnxDevice;
import org.openhab.binding.knx.internal.project.KnxLine;
import org.openhab.binding.knx.internal.project.KnxProject;
import org.openhab.binding.knx.internal.project.KnxProjectException;
import org.openhab.binding.knx.internal.project.KnxProjectReader;
import org.openhab.binding.knx.internal.project.KnxSegment;
import org.openhab.binding.knx.internal.project.KnxTopology;
import org.openhab.core.OpenHAB;
import org.openhab.core.thing.ThingUID;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.calimero.GroupAddress;
import io.calimero.IndividualAddress;

/**
 * Keeps one imported ETS project per bridge: the extracted result in {@code <folder>/project-<bridge>.json} and,
 * once loaded, in memory. Never the project file or its password.
 * <p>
 * A bridge's file is loaded at the first call that needs it; the result is kept, a failure too. Import, remove
 * and load take one lock; the project file is read before it, and a loaded project is handed out without it.
 *
 * @author Stamate Viorel - Initial contribution
 */
@NonNullByDefault
@Component(service = KnxProjectStore.class)
public class KnxProjectStore {

    /** What the stored file of a bridge gives. */
    public enum FileState {
        /** No file. */
        NONE,
        /** The file is loaded. */
        LOADED,
        /** The file is damaged, too large or not a stored project. */
        UNREADABLE,
        /** The file was written in a newer format. */
        NEWER_VERSION
    }

    /** The project was read, but writing the result failed; the earlier file and project stay. */
    public static class StoreFailedException extends IOException {
        private static final long serialVersionUID = 1L;

        public StoreFailedException() {
            super("The project was read, but the result could not be stored.");
        }
    }

    /** Forces the written temporary file to disk and moves it onto the stored file. */
    @FunctionalInterface
    interface Mover {
        void move(Path temporary, Path target) throws IOException;

        /** Called before {@link #move} with the channel the temporary file was written through. */
        default void force(FileChannel channel) throws IOException {
            channel.force(true);
        }
    }

    static final long MAX_FILE_BYTES = 64L * 1024 * 1024;
    /** A temporary file of a write whose last change is older than this was left by a write that was killed. */
    static final Duration STALE_TEMPORARY = Duration.ofMinutes(10);
    private static final String TEMPORARY_PREFIX = ".project-";
    private static final String TEMPORARY_SUFFIX = ".tmp";

    private static final Pattern FILE_NAME = Pattern.compile("project-knx_(ip|serial)_(.+)\\.json");

    private final Logger logger = LoggerFactory.getLogger(KnxProjectStore.class);
    private final Path folder;
    private final Clock clock;
    private final Mover mover;
    private final long maxFileBytes;
    private final Object lock = new Object();
    private final Map<ThingUID, State> states = new ConcurrentHashMap<>();
    private final Map<ThingUID, ProjectLookup> lookups = new ConcurrentHashMap<>();

    private sealed interface State permits Loaded, NoProject {
    }

    private record Loaded(StoredProject stored, ProjectIndex index) implements State {
        static Loaded of(StoredProject stored) {
            return new Loaded(stored, ProjectIndex.of(stored.project()));
        }
    }

    private record NoProject(FileState fileState) implements State {
    }

    private static final State NONE = new NoProject(FileState.NONE);

    @Activate
    public KnxProjectStore() {
        this(Path.of(OpenHAB.getUserDataFolder(), "knx"), Clock.systemUTC());
    }

    public KnxProjectStore(Path folder, Clock clock) {
        this(folder, clock, KnxProjectStore::move, MAX_FILE_BYTES);
    }

    KnxProjectStore(Path folder, Clock clock, Mover mover, long maxFileBytes) {
        this.folder = folder;
        this.clock = clock;
        this.mover = mover;
        this.maxFileBytes = maxFileBytes;
        deleteStaleTemporaryFiles();
    }

    private void deleteStaleTemporaryFiles() {
        if (!Files.isDirectory(folder)) {
            return;
        }
        Instant before = clock.instant().minus(STALE_TEMPORARY);
        try (DirectoryStream<Path> temporaries = Files.newDirectoryStream(folder,
                TEMPORARY_PREFIX + "*" + TEMPORARY_SUFFIX)) {
            for (Path temporary : temporaries) {
                try {
                    if (Files.getLastModifiedTime(temporary).toInstant().isBefore(before)) {
                        Files.deleteIfExists(temporary);
                    }
                } catch (IOException | RuntimeException e) {
                    logger.debug("A stale temporary file of the ETS project store could not be deleted: {}",
                            e.getClass().getSimpleName());
                }
            }
        } catch (IOException | RuntimeException e) {
            logger.debug("The ETS project store could not look for stale temporary files: {}",
                    e.getClass().getSimpleName());
        }
    }

    /** Imports the project with the file's own name as source name. */
    public StoredProject importProject(ThingUID bridge, Path file, char @Nullable [] password,
            @Nullable String language) throws IOException, KnxProjectException {
        Path name = file.getFileName();
        return importProject(bridge, file, password, language, name == null ? "" : name.toString());
    }

    /**
     * Reads the project file and keeps the result for the bridge, in place of an earlier one. The file is neither
     * kept nor deleted; the password is not kept, and wiping it is left to the caller.
     *
     * @param sourceName the name to keep as the project's source; a folder part is cut off
     * @throws KnxProjectException if the reader cannot read the file; nothing changes
     * @throws StoreFailedException if the result cannot be written; the earlier file and project stay
     * @throws IOException if the file cannot be read; nothing changes
     */
    public StoredProject importProject(ThingUID bridge, Path file, char @Nullable [] password,
            @Nullable String language, String sourceName) throws IOException, KnxProjectException {
        KnxProject project = kept(new KnxProjectReader().read(file, password, language));
        StoredProject stored = new StoredProject(project, clock.instant(), withoutFolder(sourceName), language);
        Loaded loaded = Loaded.of(stored);
        synchronized (lock) {
            write(bridge, stored);
            states.put(bridge, loaded);
        }
        return stored;
    }

    /** {@return the project of the bridge, null if there is none or its file cannot be used} */
    public @Nullable StoredProject get(ThingUID bridge) {
        return state(bridge) instanceof Loaded loaded ? loaded.stored() : null;
    }

    public FileState fileState(ThingUID bridge) {
        return state(bridge) instanceof NoProject none ? none.fileState() : FileState.LOADED;
    }

    /**
     * Deletes the bridge's file, also an unusable one and one of a bridge that no longer exists.
     *
     * @return true if there was a file
     */
    public boolean remove(ThingUID bridge) throws IOException {
        synchronized (lock) {
            boolean existed = Files.deleteIfExists(fileOf(bridge));
            states.put(bridge, NONE);
            return existed;
        }
    }

    /**
     * {@return what the stored project of the bridge says about addresses; the same object for the life of the
     * store, which always answers from the current project and never throws}
     */
    public ProjectLookup lookup(ThingUID bridge) {
        return Objects.requireNonNull(lookups.computeIfAbsent(bridge, BridgeLookup::new));
    }

    /** {@return the bridges that have a file, also bridges that no longer exist, in UID order} */
    public List<ThingUID> stored() {
        List<ThingUID> bridges = new ArrayList<>();
        try (DirectoryStream<Path> files = Files.newDirectoryStream(folder)) {
            for (Path file : files) {
                Path name = file.getFileName();
                Matcher matcher = FILE_NAME.matcher(name == null ? "" : name.toString());
                if (!matcher.matches()) {
                    continue;
                }
                try {
                    ThingUID bridge = new ThingUID("knx:" + matcher.group(1) + ":" + matcher.group(2));
                    if (fileOf(bridge).equals(file)) {
                        bridges.add(bridge);
                    }
                } catch (IllegalArgumentException e) {
                    // not a file name this store writes
                }
            }
        } catch (NoSuchFileException e) {
            return List.of();
        } catch (IOException | RuntimeException e) {
            logger.debug("The folder of stored ETS projects cannot be listed: {}", e.getClass().getSimpleName());
        }
        bridges.sort(Comparator.comparing(ThingUID::getAsString));
        return List.copyOf(bridges);
    }

    private Path fileOf(ThingUID bridge) {
        return folder.resolve("project-" + bridge.getAsString().replace(':', '_') + ".json");
    }

    private State state(ThingUID bridge) {
        State state = states.get(bridge);
        if (state != null) {
            return state;
        }
        synchronized (lock) {
            state = states.get(bridge);
            if (state == null) {
                state = load(bridge);
                states.put(bridge, state);
            }
            return state;
        }
    }

    private State load(ThingUID bridge) {
        Path file = fileOf(bridge);
        if (!Files.exists(file)) {
            return NONE;
        }
        try {
            if (Files.size(file) > maxFileBytes) {
                return unusable(bridge, FileState.UNREADABLE, "-");
            }
            try (Reader reader = new BufferedReader(
                    new InputStreamReader(Files.newInputStream(file), StandardCharsets.UTF_8.newDecoder()))) {
                return Loaded.of(ProjectFile.read(reader, bridge, maxFileBytes));
            }
        } catch (StoredFileException e) {
            return e.newerVersion() ? unusable(bridge, FileState.NEWER_VERSION, "-")
                    : unusable(bridge, FileState.UNREADABLE, e.getClass().getSimpleName());
        } catch (IOException | RuntimeException e) {
            return unusable(bridge, FileState.UNREADABLE, e.getClass().getSimpleName());
        }
    }

    private State unusable(ThingUID bridge, FileState fileState, String cause) {
        logger.warn(
                "The stored ETS project of {} cannot be used ({}, {}). Import it again with: openhab:knx project import {} <file>",
                bridge, fileState, cause, bridge);
        return new NoProject(fileState);
    }

    private void write(ThingUID bridge, StoredProject stored) throws StoreFailedException {
        if (!ProjectFile.fits(stored.project())) {
            logger.debug("The ETS project of {} nests ranges or spaces too deep to be stored", bridge);
            throw new StoreFailedException();
        }
        Path temporary = null;
        try {
            Files.createDirectories(folder);
            temporary = Files.createTempFile(folder, TEMPORARY_PREFIX, TEMPORARY_SUFFIX);
            try (FileChannel channel = FileChannel.open(temporary, StandardOpenOption.WRITE);
                    Writer writer = new BufferedWriter(new OutputStreamWriter(
                            new LimitedOutputStream(Channels.newOutputStream(channel), maxFileBytes),
                            StandardCharsets.UTF_8.newEncoder()))) {
                ProjectFile.write(writer, bridge, stored);
                writer.flush();
                mover.force(channel);
            }
            mover.move(temporary, fileOf(bridge));
        } catch (IOException | RuntimeException e) {
            if (temporary != null) {
                try {
                    Files.deleteIfExists(temporary);
                } catch (IOException | RuntimeException deleteFailure) {
                    logger.debug("A temporary file of the ETS project store could not be deleted: {}",
                            deleteFailure.getClass().getSimpleName());
                }
            }
            logger.debug("The ETS project of {} could not be stored: {}", bridge, e.getClass().getSimpleName());
            throw new StoreFailedException();
        }
    }

    private static void move(Path temporary, Path target) throws IOException {
        try {
            Files.move(temporary, target, StandardCopyOption.ATOMIC_MOVE);
        } catch (IOException e) {
            Files.move(temporary, target, StandardCopyOption.REPLACE_EXISTING);
        }
    }

    private static String withoutFolder(String sourceName) {
        return sourceName.substring(Math.max(sourceName.lastIndexOf('/'), sourceName.lastIndexOf('\\')) + 1);
    }

    /** The project as kept: the devices without their parameter values and module instances. */
    static KnxProject kept(KnxProject project) {
        KnxTopology topology = project.topology();
        List<KnxArea> areas = topology.areas().stream().map(area -> new KnxArea(area.id(), area.address(), area.name(),
                area.lines().stream().map(KnxProjectStore::kept).toList())).toList();
        return new KnxProject(project.info(), project.installationName(), project.groupRanges(),
                project.groupAddresses(), new KnxTopology(areas, kept(topology.unplacedDevices())), project.locations(),
                project.ignoredInstallations(), project.products(), project.groupObjects());
    }

    private static KnxLine kept(KnxLine line) {
        List<KnxSegment> segments = line.segments().stream().map(segment -> new KnxSegment(segment.id(),
                segment.number(), segment.mediumTypeRefId(), kept(segment.devices()))).toList();
        return new KnxLine(line.id(), line.address(), line.name(), line.mediumTypeRefId(), segments,
                kept(line.devices()));
    }

    private static List<KnxDevice> kept(List<KnxDevice> devices) {
        return devices.stream()
                .map(device -> new KnxDevice(device.id(), device.individualAddress(), device.name(),
                        device.description(), device.productRefId(), device.hardware2ProgramRefId(),
                        device.mediumTypeRefId(), device.objects(), List.of(), List.of()))
                .toList();
    }

    /** Fails a write that goes beyond the size the store reads back. */
    private static class LimitedOutputStream extends FilterOutputStream {
        private final long limit;
        private long written;

        LimitedOutputStream(OutputStream out, long limit) {
            super(out);
            this.limit = limit;
        }

        @Override
        public void write(int b) throws IOException {
            count(1);
            out.write(b);
        }

        @Override
        public void write(byte @Nullable [] b, int off, int len) throws IOException {
            count(len);
            out.write(b, off, len);
        }

        private void count(int bytes) throws IOException {
            written += bytes;
            if (written > limit) {
                throw new IOException("The stored project would be larger than the store reads back.");
            }
        }
    }

    /** Asks the current state at every call. */
    private class BridgeLookup implements ProjectLookup {
        private final ThingUID bridge;

        BridgeLookup(ThingUID bridge) {
            this.bridge = bridge;
        }

        private @Nullable ProjectIndex index() {
            return state(bridge) instanceof Loaded loaded ? loaded.index() : null;
        }

        @Override
        public @Nullable String groupAddressName(GroupAddress address) {
            try {
                ProjectIndex index = index();
                return index == null ? null : index.addressName(address.getRawAddress());
            } catch (RuntimeException e) {
                return null;
            }
        }

        @Override
        public @Nullable String dataType(GroupAddress address) {
            try {
                ProjectIndex index = index();
                return index == null ? null : index.addressType(address.getRawAddress());
            } catch (RuntimeException e) {
                return null;
            }
        }

        @Override
        public @Nullable String deviceName(IndividualAddress address) {
            try {
                ProjectIndex index = index();
                return index == null ? null : index.deviceName(address.getRawAddress());
            } catch (RuntimeException e) {
                return null;
            }
        }
    }
}
