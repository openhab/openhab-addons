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
package org.openhab.binding.knx.internal.project;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.knx.internal.project.ProgramFile.ComObjectEntry;
import org.openhab.binding.knx.internal.project.ProgramFile.ComObjectRefEntry;

/**
 * Joins an object instance of the project to its definition in the application program and lays the three
 * levels over each other: the instance, the {@code ComObjectRef}, the {@code ComObject}.
 *
 * @author Stamate Viorel - Initial contribution
 */
@NonNullByDefault
final class GroupObjects {

    // group 1: the module instance id, group 2: the module definition, group 3: the object inside it
    private static final Pattern MODULE_OBJECT = Pattern.compile("((MD-\\d+)_.*_MI-[\\d-]+)_(.+)");
    private static final Pattern SIZE = Pattern.compile("(\\d{1,5}) (Bit|Bits|Byte|Bytes)");
    private static final String ENABLED = "Enabled";
    private static final String DISABLED = "Disabled";

    private GroupObjects() {
    }

    /**
     * The {@code ComObjectRef@Id} values that can define the instance, in the order they are tried: the
     * reference itself (ETS 4 writes it in full), the program id before it, and for an object inside a module
     * the program id, the module definition and the part after the last module instance.
     */
    static List<String> referenceIds(String refId, String programId) {
        if (refId.isEmpty()) {
            return List.of();
        }
        List<String> ids = new ArrayList<>(3);
        ids.add(refId);
        ids.add(programId + "_" + refId);
        Matcher module = MODULE_OBJECT.matcher(refId);
        if (module.matches()) {
            ids.add(programId + "_" + module.group(2) + "_" + module.group(3));
        }
        return ids;
    }

    /** The module instance of an object inside a module, {@code MD-n_M-n_MI-n} or a submodule's longer form. */
    static @Nullable String moduleInstanceId(String refId) {
        Matcher module = MODULE_OBJECT.matcher(refId);
        return module.matches() ? module.group(1) : null;
    }

    /** The group objects of a device whose program is read. */
    static List<KnxGroupObject> resolve(KnxDevice device, ProgramFile program, Runnable unresolvedBase) {
        List<KnxGroupObject> objects = new ArrayList<>();
        for (KnxObjectInstance instance : device.objects()) {
            objects.add(resolve(device, instance, program, unresolvedBase));
        }
        return objects;
    }

    /** The group objects of a device whose definitions cannot be found. */
    static List<KnxGroupObject> withoutDefinition(KnxDevice device, KnxObjectDefinition why) {
        List<KnxGroupObject> objects = new ArrayList<>();
        for (KnxObjectInstance instance : device.objects()) {
            objects.add(build(device, instance, why, null, null, null, null, () -> {
            }));
        }
        return objects;
    }

    private static KnxGroupObject resolve(KnxDevice device, KnxObjectInstance instance, ProgramFile program,
            Runnable unresolvedBase) {
        for (String id : referenceIds(instance.refId(), program.id())) {
            ComObjectRefEntry reference = program.comObjectRefs.get(id);
            if (reference != null) {
                ComObjectEntry object = program.comObjects.get(reference.refId());
                return object == null
                        ? build(device, instance, KnxObjectDefinition.NOT_IN_PROGRAM, null, null, null, null,
                                unresolvedBase)
                        : build(device, instance, KnxObjectDefinition.FOUND, program, id, reference, object,
                                unresolvedBase);
            }
        }
        return build(device, instance, KnxObjectDefinition.NOT_IN_PROGRAM, null, null, null, null, unresolvedBase);
    }

    private static KnxGroupObject build(KnxDevice device, KnxObjectInstance instance, KnxObjectDefinition definition,
            @Nullable ProgramFile program, @Nullable String referenceId, @Nullable ComObjectRefEntry reference,
            @Nullable ComObjectEntry object, Runnable unresolvedBase) {
        String text = first(instance.text(), reference == null ? null : reference.text(),
                object == null ? null : object.text());
        String name;
        String functionText;
        if (program != null && referenceId != null && reference != null && object != null) {
            name = ObjectNames.name(device, instance, program, referenceId, reference, object);
            functionText = functionText(instance, program, referenceId, reference, object);
        } else {
            name = ObjectNames.nameWithoutDefinition(instance);
            functionText = first(instance.functionText(), null, null);
        }
        List<KnxDatapointType> types = instance.datapointTypes();
        if (types.isEmpty() && reference != null) {
            types = reference.datapointTypes();
        }
        if (types.isEmpty() && object != null) {
            types = object.datapointTypes();
        }
        String sizeText = first(null, reference == null ? null : reference.objectSize(),
                object == null ? null : object.objectSize());
        KnxObjectFlags own = instance.flags();
        KnxObjectFlags byReference = reference == null ? KnxObjectFlags.NONE : reference.flags();
        KnxObjectFlags byObject = object == null ? KnxObjectFlags.NONE : object.flags();
        KnxGroupObjectFlags flags = new KnxGroupObjectFlags(flag(own.read(), byReference.read(), byObject.read()),
                flag(own.write(), byReference.write(), byObject.write()),
                flag(own.communication(), byReference.communication(), byObject.communication()),
                flag(own.transmit(), byReference.transmit(), byObject.transmit()),
                flag(own.update(), byReference.update(), byObject.update()),
                flag(own.readOnInit(), byReference.readOnInit(), byObject.readOnInit()));
        return new KnxGroupObject(device.id(), device.individualAddress(), instance,
                number(device, instance, program, reference, object, unresolvedBase), text, name, functionText, types,
                sizeText, sizeBits(sizeText), flags, instance.groupAddressIds(), instance.sendingGroupAddressId(),
                instance.channelId(), moduleInstanceId(instance.refId()), definition);
    }

    /**
     * The object's number: as the program has it, and inside a module with a {@code BaseNumber} the device's
     * number, base plus that. A base that cannot be worked out gives {@code null}, never the number inside the
     * module.
     */
    private static @Nullable Integer number(KnxDevice device, KnxObjectInstance instance, @Nullable ProgramFile program,
            @Nullable ComObjectRefEntry reference, @Nullable ComObjectEntry object, Runnable unresolvedBase) {
        if (object == null) {
            return null;
        }
        String base = object.baseNumber();
        if (base == null || base.isBlank()) {
            return object.number();
        }
        Integer number = program == null || reference == null ? null
                : ObjectNames.deviceNumber(device, instance, program, reference, object);
        if (number == null) {
            unresolvedBase.run();
        }
        return number;
    }

    /** The overlay of the three levels; a program's text in the chosen language. */
    private static @Nullable String functionText(KnxObjectInstance instance, ProgramFile program, String referenceId,
            ComObjectRefEntry reference, ComObjectEntry object) {
        String own = first(instance.functionText(), null, null);
        if (own != null) {
            return own;
        }
        String byReference = first(reference.functionText(), null, null);
        if (byReference != null) {
            return ObjectNames.translated(program, byReference, referenceId);
        }
        String byObject = first(object.functionText(), null, null);
        return byObject == null ? null : ObjectNames.translated(program, byObject, reference.refId());
    }

    /** The first value that is there and not empty. */
    private static @Nullable String first(@Nullable String own, @Nullable String byReference,
            @Nullable String byObject) {
        if (own != null && !own.isEmpty()) {
            return own;
        }
        if (byReference != null && !byReference.isEmpty()) {
            return byReference;
        }
        return byObject != null && !byObject.isEmpty() ? byObject : null;
    }

    /** The first level with {@code Enabled} or {@code Disabled} decides; other values do not count. */
    private static boolean flag(@Nullable String own, @Nullable String byReference, @Nullable String byObject) {
        Boolean value = flag(own);
        if (value == null) {
            value = flag(byReference);
        }
        if (value == null) {
            value = flag(byObject);
        }
        return Boolean.TRUE.equals(value);
    }

    private static @Nullable Boolean flag(@Nullable String value) {
        if (ENABLED.equals(value)) {
            return Boolean.TRUE;
        }
        return DISABLED.equals(value) ? Boolean.FALSE : null;
    }

    /** {@code n Bit}, {@code n Bits}, {@code n Byte}, {@code n Bytes}; any other form has no known size. */
    static @Nullable Integer sizeBits(@Nullable String sizeText) {
        if (sizeText == null) {
            return null;
        }
        Matcher size = SIZE.matcher(sizeText.trim());
        if (!size.matches()) {
            return null;
        }
        int n = Integer.parseInt(size.group(1));
        return size.group(2).startsWith("Bit") ? n : n * 8;
    }
}
