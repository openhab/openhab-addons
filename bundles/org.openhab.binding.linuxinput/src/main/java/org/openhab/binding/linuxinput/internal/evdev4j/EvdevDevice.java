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
package org.openhab.binding.linuxinput.internal.evdev4j;

import static org.openhab.binding.linuxinput.internal.evdev4j.Utils.combineFlags;

import java.io.Closeable;
import java.io.IOException;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.spi.SelectorProvider;
import java.text.MessageFormat;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.linuxinput.internal.evdev4j.jnr.EvdevLibrary;

import jnr.constants.platform.Errno;
import jnr.constants.platform.OpenFlags;
import jnr.enxio.channels.NativeDeviceChannel;
import jnr.enxio.channels.NativeFileSelectorProvider;
import jnr.ffi.byref.PointerByReference;
import jnr.posix.POSIX;
import jnr.posix.POSIXFactory;

/**
 * Classbased access to libevdev-input functionality.
 *
 * @author Thomas Weißschuh - Initial contribution
 */
@NonNullByDefault
public class EvdevDevice implements Closeable {
    private static final SelectorProvider SELECTOR_PROVIDER = NativeFileSelectorProvider.getInstance();

    private final EvdevLibrary lib = EvdevLibrary.load();
    private final POSIX posix = POSIXFactory.getNativePOSIX();
    private final SelectableChannel channel;
    private final EvdevLibrary.Handle handle;

    public EvdevDevice(String path) throws IOException {
        int fd = posix.open(path, combineFlags(OpenFlags.class, OpenFlags.O_RDONLY, OpenFlags.O_CLOEXEC), 0);
        if (fd == -1) {
            throw new LastErrorException(posix, posix.errno(), path);
        }

        EvdevLibrary.Handle newHandle = EvdevLibrary.makeHandle(lib);
        PointerByReference ref = new PointerByReference();
        int ret = lib.new_from_fd(fd, ref);
        if (ret != 0) {
            throw new LastErrorException(posix, -ret);
        }
        newHandle.useMemory(ref.getValue());
        handle = newHandle;
        NativeDeviceChannel newChannel = new NativeDeviceChannel(SELECTOR_PROVIDER, fd, SelectionKey.OP_READ, true);
        newChannel.configureBlocking(false);
        channel = newChannel;
    }

    private void grab(EvdevLibrary.GrabMode mode) {
        int ret = lib.grab(handle, mode.getValue());
        if (ret != 0) {
            throw new LastErrorException(posix, -ret);
        }
    }

    public void grab() {
        grab(EvdevLibrary.GrabMode.GRAB);
    }

    public void ungrab() {
        grab(EvdevLibrary.GrabMode.UNGRAB);
    }

    @Override
    public String toString() {
        return MessageFormat.format("Evdev {0}|{1}|{2}", lib.get_name(handle), lib.get_phys(handle),
                lib.get_uniq(handle));
    }

    public Optional<InputEvent> nextEvent() {
        // EV_SYN/SYN_DROPPED handling?
        EvdevLibrary.InputEvent event = new EvdevLibrary.InputEvent(jnr.ffi.Runtime.getRuntime(lib));
        int ret = lib.next_event(handle, EvdevLibrary.ReadFlag.NORMAL, event);
        if (ret == -Errno.EAGAIN.intValue()) {
            return Optional.empty();
        }
        if (ret < 0) {
            throw new LastErrorException(posix, -ret);
        }
        return Optional.of(new InputEvent(lib, Instant.ofEpochSecond(event.sec.get(), event.usec.get()),
                event.type.get(), event.code.get(), event.value.get()));
    }

    public static Selector openSelector() throws IOException {
        return SELECTOR_PROVIDER.openSelector();
    }

    public SelectionKey register(Selector selector) throws ClosedChannelException {
        return channel.register(selector, SelectionKey.OP_READ);
    }

    @Override
    public synchronized void close() throws IOException {
        lib.free(handle);
        channel.close();
    }

    @NonNullByDefault({})
    public String getName() {
        return lib.get_name(handle);
    }

    public void setName(String name) {
        lib.set_name(handle, name);
    }

    @NonNullByDefault({})
    public String getPhys() {
        return lib.get_phys(handle);
    }

    @NonNullByDefault({})
    public String getUniq() {
        return lib.get_uniq(handle);
    }

    public int getProdutId() {
        return lib.get_id_product(handle);
    }

    public int getVendorId() {
        return lib.get_id_vendor(handle);
    }

    public int getBusId() {
        return lib.get_id_bustype(handle);
    }

    public Optional<EvdevLibrary.BusType> getBusType() {
        return EvdevLibrary.BusType.fromInt(getBusId());
    }

    public int getVersionId() {
        return lib.get_id_version(handle);
    }

    public int getDriverVersion() {
        return lib.get_driver_version(handle);
    }

    public boolean has(EvdevLibrary.Type type) {
        return lib.has_event_type(handle, type.intValue());
    }

    public boolean has(EvdevLibrary.Type type, int code) {
        return lib.has_event_code(handle, type.intValue(), code);
    }

    public void enable(EvdevLibrary.Type type) {
        int result = lib.enable_event_type(handle, type.intValue());
        if (result != 0) {
            throw new FailedOperationException();
        }
    }

    public void enable(EvdevLibrary.Type type, int code) {
        int result = lib.enable_event_code(handle, type.intValue(), code);
        if (result != 0) {
            throw new FailedOperationException();
        }
    }

    public Collection<Key> enumerateKeys() {
        int minKey = 0;
        int maxKey = lib.event_type_get_max(EvdevLibrary.Type.KEY.intValue());
        List<Key> result = new ArrayList<>();
        for (int i = minKey; i <= maxKey; i++) {
            if (has(EvdevLibrary.Type.KEY, i)) {
                result.add(new Key(lib, i));
            }
        }
        return result;
    }

    public static class Key {
        private final EvdevLibrary lib;
        private final int code;

        private Key(EvdevLibrary lib, int code) {
            this.lib = lib;
            this.code = code;
        }

        public int getCode() {
            return code;
        }

        public String getName() {
            return lib.event_code_get_name(EvdevLibrary.Type.KEY.intValue(), code);
        }

        @Override
        public String toString() {
            return String.valueOf(code);
        }
    }

    public static class InputEvent {
        private EvdevLibrary lib;

        private final Instant time;
        private final int type;
        private final int code;
        private final int value;

        private InputEvent(EvdevLibrary lib, Instant time, int type, int code, int value) {
            this.lib = lib;
            this.time = time;
            this.type = type;
            this.code = code;
            this.value = value;
        }

        public Instant getTime() {
            return time;
        }

        public int getType() {
            return type;
        }

        public EvdevLibrary.Type type() {
            return EvdevLibrary.Type.fromInt(type)
                    .orElseThrow(() -> new IllegalStateException("Could not find 'Type' for value " + type));
        }

        public Optional<String> typeName() {
            return Optional.ofNullable(lib.event_type_get_name(type));
        }

        public int getCode() {
            return code;
        }

        public Optional<String> codeName() {
            return Optional.ofNullable(lib.event_code_get_name(type, code));
        }

        public int getValue() {
            return value;
        }

        public Optional<String> valueName() {
            return Optional.ofNullable(lib.event_value_get_name(type, code, value));
        }

        @Override
        public String toString() {
            return MessageFormat.format("{0}: {1}/{2}/{3}", DateTimeFormatter.ISO_INSTANT.format(time),
                    typeName().orElse(String.valueOf(type)), codeName().orElse(String.valueOf(code)),
                    valueName().orElse(String.valueOf(value)));
        }
    }

    public static class FailedOperationException extends RuntimeException {
        private static final long serialVersionUID = -8556632931670798678L;
    }
}
