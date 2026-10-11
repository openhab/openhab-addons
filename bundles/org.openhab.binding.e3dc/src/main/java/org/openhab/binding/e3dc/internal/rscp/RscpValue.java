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
package org.openhab.binding.e3dc.internal.rscp;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * One RSCP data item.
 *
 * <p>
 * RSCP responses are trees: an item of type {@code CONTAINER} carries children. Scalar values are
 * kept in their decoded Java representation so callers apply unit conversions themselves.
 *
 * @author Bernd Weymann - Initial contribution
 */
@NonNullByDefault
public final class RscpValue {

    private final int tag;
    private final RscpType type;
    private final @Nullable Object value;
    private final List<RscpValue> children;

    private RscpValue(int tag, RscpType type, @Nullable Object value, List<RscpValue> children) {
        this.tag = tag;
        this.type = type;
        this.value = value;
        this.children = children;
    }

    static RscpValue scalar(int tag, RscpType type, @Nullable Object value) {
        return new RscpValue(tag, type, value, List.of());
    }

    public static RscpValue container(int tag, List<RscpValue> children) {
        return new RscpValue(tag, RscpType.CONTAINER, null, Collections.unmodifiableList(children));
    }

    public static RscpValue of(int tag, RscpType type, @Nullable Object value) {
        return new RscpValue(tag, type, value, List.of());
    }

    public static RscpValue none(int tag) {
        return new RscpValue(tag, RscpType.NONE, null, List.of());
    }

    public static RscpValue cstring(int tag, String value) {
        return new RscpValue(tag, RscpType.CSTRING, value, List.of());
    }

    public static RscpValue uchar8(int tag, int value) {
        return new RscpValue(tag, RscpType.UCHAR8, value, List.of());
    }

    public static RscpValue int32(int tag, int value) {
        return new RscpValue(tag, RscpType.INT32, value, List.of());
    }

    public int getTag() {
        return tag;
    }

    public RscpType getType() {
        return type;
    }

    public @Nullable Object getValue() {
        return value;
    }

    public List<RscpValue> getChildren() {
        return children;
    }

    public boolean isError() {
        return type == RscpType.ERROR;
    }

    /** The error a device reported, or {@code null} if this item is not an error item. */
    public @Nullable RscpError getError() {
        return value instanceof RscpError error ? error : null;
    }

    /** First descendant with the given tag, depth first, or {@code null}. */
    public @Nullable RscpValue find(int searchTag) {
        if (tag == searchTag) {
            return this;
        }
        for (RscpValue child : children) {
            RscpValue found = child.find(searchTag);
            if (found != null) {
                return found;
            }
        }
        return null;
    }

    /**
     * First descendant matching the given chain of tags, each nested in the previous one.
     *
     * <p>
     * Some responses are nested more than one container deep, for example the history data where the
     * requested values sit inside a sum container. Chaining {@link #find(int)} calls walks such a path
     * without searching the whole subtree again on every level.
     */
    public @Nullable RscpValue find(List<Integer> tagPath) {
        RscpValue current = this;
        for (int searchTag : tagPath) {
            RscpValue found = current.find(searchTag);
            if (found == null) {
                return null;
            }
            current = found;
        }
        return current;
    }

    /** All descendants with the given tag in document order. */
    public List<RscpValue> findAll(int searchTag) {
        List<RscpValue> result = new ArrayList<>();
        if (tag == searchTag) {
            result.add(this);
        }
        for (RscpValue child : children) {
            result.addAll(child.findAll(searchTag));
        }
        return result;
    }

    public @Nullable Number findNumber(int searchTag) {
        RscpValue found = find(searchTag);
        return found != null && found.value instanceof Number number ? number : null;
    }

    public @Nullable Double findDouble(int searchTag) {
        Number number = findNumber(searchTag);
        return number != null ? number.doubleValue() : null;
    }

    public @Nullable String findString(int searchTag) {
        RscpValue found = find(searchTag);
        return found != null && found.value instanceof String string ? string : null;
    }

    /** True if this item or any descendant is an error item. */
    public boolean containsError() {
        if (isError()) {
            return true;
        }
        for (RscpValue child : children) {
            if (child.containsError()) {
                return true;
            }
        }
        return false;
    }

    /** The first error found in this item or its descendants, or {@code null}. */
    public @Nullable RscpError findError() {
        if (isError()) {
            return getError();
        }
        for (RscpValue child : children) {
            RscpError error = child.findError();
            if (error != null) {
                return error;
            }
        }
        return null;
    }

    /** The value as a number, or {@code null} if it is absent or of another kind. */
    public @Nullable Number asNumber() {
        return value instanceof Number number ? number : null;
    }

    /** The value as a byte array, or {@code null} if it is absent or of another kind. */
    public byte @Nullable [] asBytes() {
        return value instanceof byte[] bytes ? bytes : null;
    }

    @Override
    public String toString() {
        String name = RscpTag.nameOf(tag);
        if (type == RscpType.CONTAINER) {
            return name + "[" + children.size() + "]";
        }
        return name + "=" + describeValue();
    }

    /** Renders the value the way it is logged; credentials never appear in a response. */
    public String describeValue() {
        if (value == null) {
            return "<none>";
        }
        if (value instanceof byte[] bytes) {
            if (type == RscpType.CSTRING) {
                return new String(bytes, StandardCharsets.UTF_8);
            }
            StringBuilder sb = new StringBuilder(bytes.length * 2);
            for (byte b : bytes) {
                sb.append(String.format("%02x", b));
            }
            return "0x" + sb;
        }
        return String.valueOf(value);
    }
}
