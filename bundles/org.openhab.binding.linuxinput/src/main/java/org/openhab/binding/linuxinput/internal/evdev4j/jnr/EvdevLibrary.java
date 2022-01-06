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
package org.openhab.binding.linuxinput.internal.evdev4j.jnr;

import static org.openhab.binding.linuxinput.internal.evdev4j.Utils.constantFromInt;
import static org.openhab.binding.linuxinput.internal.evdev4j.jnr.Utils.trimEnd;

import java.util.Optional;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.linuxinput.internal.evdev4j.Utils;

import jnr.constants.Constant;
import jnr.ffi.LibraryLoader;
import jnr.ffi.Runtime;
import jnr.ffi.Struct;
import jnr.ffi.annotations.IgnoreError;
import jnr.ffi.annotations.In;
import jnr.ffi.annotations.Out;
import jnr.ffi.byref.PointerByReference;
import jnr.ffi.mapper.FunctionMapper;

/**
 * JNR library for libdevdev library (libevdev.h).
 *
 * @author Thomas Weißschuh - Initial contribution
 */
@NonNullByDefault
@IgnoreError
public interface EvdevLibrary {
    static EvdevLibrary load() {
        FunctionMapper evdevFunctionMapper = (functionName, context) -> "libevdev_" + trimEnd("_", functionName);
        return LibraryLoader.create(EvdevLibrary.class).library("evdev").mapper(evdevFunctionMapper).load();
    }

    static Handle makeHandle(EvdevLibrary lib) {
        return new Handle(Runtime.getRuntime(lib));
    }

    class Handle extends Struct {
        public Handle(Runtime runtime) {
            super(runtime);
        }
    }

    enum GrabMode {
        GRAB(3),
        UNGRAB(4);

        private int value;

        GrabMode(int value) {
            this.value = value;
        }

        public int getValue() {
            return value;
        }
    }

    final class InputEvent extends Struct {
        public UnsignedLong sec = new UnsignedLong();
        public UnsignedLong usec = new UnsignedLong();
        public Unsigned16 type = new Unsigned16();
        public Unsigned16 code = new Unsigned16();
        public Signed32 value = new Signed32();

        public InputEvent(Runtime runtime) {
            super(runtime);
        }
    }

    int new_from_fd(int fd, @Out PointerByReference handle);

    void free(@In Handle handle);

    int grab(@In Handle handle, int grab);

    int next_event(@In Handle handle, int flags, @Out InputEvent event);

    String event_type_get_name(int type);

    String event_code_get_name(int type, int code);

    String event_value_get_name(int type, int code, int value);

    boolean has_event_type(@In Handle handle, int type);

    int enable_event_type(@In Handle handle, int type);

    int event_type_get_max(int type);

    int disable_event_type(@In Handle handle, int type);

    boolean has_event_code(@In Handle handle, int type, int code);

    int enable_event_code(@In Handle handle, int type, int code);

    int disable_event_code(@In Handle handle, int type, int code);

    String get_name(@In Handle handle);

    void set_name(@In Handle handle, String name);

    String get_phys(@In Handle handle);

    String get_uniq(@In Handle handle);

    int get_id_product(@In Handle handle);

    int get_id_vendor(@In Handle handle);

    int get_id_bustype(@In Handle handle);

    int get_id_version(@In Handle handle);

    int get_driver_version(@In Handle handle);

    @SuppressWarnings("unused")
    class ReadFlag {
        private ReadFlag() {
        }

        public static final int SYNC = 1;
        public static final int NORMAL = 2;
        public static final int FORCE_SYNC = 4;
        public static final int BLOCKING = 8;
    }

    class KeyEventValue {
        private KeyEventValue() {
        }

        public static final int UP = 0;
        public static final int DOWN = 1;
        public static final int REPEAT = 2;
    }

    enum Type implements Constant {
        SYN(0x00),
        KEY(0x01),
        REL(0x02),
        ABS(0x03),
        MSC(0x04),
        SW(0x05),
        LED(0x11),
        SND(0x12),
        REP(0x14),
        FF(0x15),
        PWR(0x16),
        FF_STATUS(0x17),
        MAX(0x1f),
        CNT(0x20);

        private final int i;

        Type(int i) {
            this.i = i;
        }

        public static Optional<Type> fromInt(int i) {
            return constantFromInt(Type.values(), i);
        }

        @Override
        public int intValue() {
            return i;
        }

        @Override
        public long longValue() {
            return i;
        }

        @Override
        public boolean defined() {
            return true;
        }
    }

    enum BusType implements Constant {
        PCI(0x01),
        ISAPNP(0x02),
        USB(0x03),
        HIL(0x04),
        BLUETOOTH(0x05),
        VIRTUAL(0x06),

        ISA(0x10),
        I8042(0x11),
        XTKBD(0x12),
        RS232(0x13),
        GAMEPORT(0x14),
        PARPORT(0x15),
        AMIGA(0x16),
        ADB(0x17),
        I2C(0x18),
        HOST(0x19),
        GSC(0x1A),
        ATARI(0x1B),
        SPI(0x1C),
        RMI(0x1D),
        CEC(0x1E),
        INTEL_ISHTP(0x1F);

        private final int i;

        BusType(int i) {
            this.i = i;
        }

        public static Optional<BusType> fromInt(int i) {
            return Utils.constantFromInt(BusType.values(), i);
        }

        @Override
        public int intValue() {
            return i;
        }

        @Override
        public long longValue() {
            return i;
        }

        @Override
        public boolean defined() {
            return true;
        }
    }
}
