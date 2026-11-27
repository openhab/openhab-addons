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
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * One RSCP data item. RSCP responses are trees: an item of type {@code CONTAINER} carries children. Scalar
 * values are kept in their decoded Java representation so callers apply unit conversions themselves.
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

    /**
     * A container with the given children.
     */
    public static RscpValue container(int tag, List<RscpValue> children) {
        return new RscpValue(tag, RscpType.CONTAINER, null, Collections.unmodifiableList(children));
    }

    /**
     * An item of the given type and value.
     */
    public static RscpValue of(int tag, RscpType type, @Nullable Object value) {
        return new RscpValue(tag, type, value, List.of());
    }

    /**
     * An item without data, as a request uses it.
     */
    public static RscpValue none(int tag) {
        return new RscpValue(tag, RscpType.NONE, null, List.of());
    }

    /**
     * An item with a string value.
     */
    public static RscpValue cstring(int tag, String value) {
        return new RscpValue(tag, RscpType.CSTRING, value, List.of());
    }

    /**
     * An unsigned char item.
     */
    public static RscpValue uchar8(int tag, int value) {
        return new RscpValue(tag, RscpType.UCHAR8, value, List.of());
    }

    /**
     * A boolean item.
     */
    public static RscpValue bool(int tag, boolean value) {
        return new RscpValue(tag, RscpType.BOOL, value, List.of());
    }

    /**
     * An unsigned 32 bit item.
     */
    public static RscpValue uint32(int tag, long value) {
        return new RscpValue(tag, RscpType.UINT32, value, List.of());
    }

    /**
     * A signed 32 bit item.
     */
    public static RscpValue int32(int tag, int value) {
        return new RscpValue(tag, RscpType.INT32, value, List.of());
    }

    /** A moment, which the interface carries as seconds and nanoseconds since 1970. */
    public static RscpValue timestamp(int tag, Instant instant) {
        return new RscpValue(tag, RscpType.TIMESTAMP, instant, List.of());
    }

    /**
     * The tag of the item.
     */
    public int getTag() {
        return tag;
    }

    /**
     * The type of the item.
     */
    public RscpType getType() {
        return type;
    }

    /**
     * The raw value, or null for a container or an empty item.
     */
    public @Nullable Object getValue() {
        return value;
    }

    /**
     * The children of a container.
     */
    public List<RscpValue> getChildren() {
        return children;
    }

    /**
     * Whether the item reports an error.
     */
    public boolean isError() {
        return type == RscpType.ERROR;
    }

    /**
     * The error of the item, or null.
     */
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
     * First descendant matching the given chain of tags, each nested in the previous one, as needed for the
     * history data where the requested values sit inside a sum container.
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

    /**
     * All children with the given tag, in the order of the answer.
     */
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

    /**
     * The number of the child with the given tag, or null.
     */
    public @Nullable Number findNumber(int searchTag) {
        RscpValue found = find(searchTag);
        return found != null && found.value instanceof Number number ? number : null;
    }

    /**
     * The number of the child with the given tag as double, or null.
     */
    public @Nullable Double findDouble(int searchTag) {
        Number number = findNumber(searchTag);
        return number != null ? number.doubleValue() : null;
    }

    /**
     * The text of the child with the given tag, or null.
     */
    public @Nullable String findString(int searchTag) {
        RscpValue found = find(searchTag);
        if (found == null) {
            return null;
        }
        // a received text is kept as the bytes the device sent, a text built here is a String
        if (found.value instanceof byte[] bytes && found.type == RscpType.CSTRING) {
            return new String(bytes, StandardCharsets.UTF_8).replace("\0", "");
        }
        return found.value instanceof String string ? string : null;
    }

    /**
     * Whether the item or one of its children reports an error.
     */
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

    /**
     * The first error in the item tree, or null.
     */
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

    /**
     * The value as a number, or null.
     */
    public @Nullable Number asNumber() {
        return value instanceof Number number ? number : null;
    }

    /**
     * The value as bytes, or null.
     */
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

    /**
     * This item with all of its descendants, one per line and indented by the nesting. It shows what a device
     * answered, which {@link #toString()} does not; credentials never appear in a response, so it is safe to
     * log.
     */
    public String describeTree() {
        StringBuilder tree = new StringBuilder();
        appendTree(tree, "");
        return tree.toString().stripTrailing();
    }

    private void appendTree(StringBuilder tree, String indent) {
        tree.append(indent).append(this).append(System.lineSeparator());
        for (RscpValue child : children) {
            child.appendTree(tree, indent + "    ");
        }
    }

    /**
     * The value for the log.
     */
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
