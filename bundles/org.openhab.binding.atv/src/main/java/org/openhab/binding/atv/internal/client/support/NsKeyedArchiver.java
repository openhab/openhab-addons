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
package org.openhab.binding.atv.internal.client.support;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.atv.internal.client.exceptions.InvalidResponseError;

/**
 * Minimal support for reading NSKeyedArchiver serialized data.
 *
 * <p>
 * {@link #readArchiveProperties} reads one or more properties from the archived plist
 * ({@code $archiver}/{@code $objects}/{@code $top} graph) by following UID references, as
 * used for Companion/RTI keyboard payloads. {@link #unarchive} additionally resolves the
 * full object graph recursively, decoding NSString/NSDictionary/NSArray/NSData basics
 * (NSNumber values are stored as plain plist numbers).
 *
 * @author Dan Cunningham - Initial contribution
 */
@NonNullByDefault
public final class NsKeyedArchiver {

    private NsKeyedArchiver() {
    }

    /**
     * Gets properties from an NSKeyedArchiver encoded plist.
     *
     * <p>
     * For each path, keys are looked up starting at {@code $top}, resolving any
     * {@code UID} through the {@code $objects} table after every step. A missing key or
     * an out-of-range UID yields {@code null} for that path; traversing through a
     * non-dictionary value is an error.
     *
     * @param archive binary plist bytes containing the archive
     * @param paths one or more key paths to read
     * @return one entry per path, {@code null} where the path could not be resolved
     * @throws InvalidResponseError if the archive is malformed
     */
    @SafeVarargs
    public static List<@Nullable Object> readArchiveProperties(byte[] archive, List<String>... paths) {
        Map<String, Object> data = parseArchive(archive);
        List<Object> objects = objectsTable(data);
        Object top = data.get("$top");
        if (!(top instanceof Map)) {
            throw new InvalidResponseError("NSKeyedArchiver archive has no $top dictionary");
        }

        List<@Nullable Object> results = new ArrayList<>(paths.length);
        for (List<String> path : paths) {
            results.add(readPath(top, path, objects));
        }
        return results;
    }

    private static @Nullable Object readPath(Object top, List<String> path, List<Object> objects) {
        Object element = top;
        for (String key : path) {
            if (!(element instanceof Map<?, ?> map)) {
                throw new InvalidResponseError("Cannot traverse key '" + key + "' through non-dictionary value");
            }
            if (!map.containsKey(key)) {
                return null;
            }
            element = map.get(key);
            if (element instanceof Uid uid) {
                if (uid.value() >= objects.size()) {
                    return null;
                }
                element = objects.get((int) uid.value());
            }
        }
        return element;
    }

    /**
     * Unarchives an NSKeyedArchiver encoded plist into plain Java objects.
     *
     * <p>
     * Returns the {@code $top} dictionary with all UID references resolved
     * recursively. Keyed objects are decoded by their {@code $classname}:
     * NSDictionary ({@code NS.keys}/{@code NS.objects}), NSArray/NSSet
     * ({@code NS.objects}), NSString ({@code NS.string}), NSData ({@code NS.data});
     * any other keyed object (e.g. {@code RTITextOperations},
     * {@code TIKeyboardOutput}, {@code NSUUID}) becomes a map of its fields with
     * the {@code $class} entry removed. The {@code $null} placeholder becomes
     * {@code null}.
     *
     * @param archive binary plist bytes containing the archive
     * @return the resolved {@code $top} dictionary
     * @throws InvalidResponseError if the archive is malformed or cyclic
     */
    public static Map<String, @Nullable Object> unarchive(byte[] archive) {
        Map<String, Object> data = parseArchive(archive);
        List<Object> objects = objectsTable(data);
        Object top = data.get("$top");
        if (!(top instanceof Map<?, ?> topMap)) {
            throw new InvalidResponseError("NSKeyedArchiver archive has no $top dictionary");
        }

        Map<String, @Nullable Object> result = new LinkedHashMap<>();
        for (Map.Entry<?, ?> entry : topMap.entrySet()) {
            result.put(String.valueOf(entry.getKey()), resolve(entry.getValue(), objects, new ArrayList<>()));
        }
        return result;
    }

    private static Map<String, Object> parseArchive(byte[] archive) {
        Object parsed = BinaryPlist.parse(archive);
        if (!(parsed instanceof Map)) {
            throw new InvalidResponseError("NSKeyedArchiver archive is not a dictionary");
        }
        @SuppressWarnings("unchecked")
        Map<String, Object> map = (Map<String, Object>) parsed;
        return map;
    }

    private static List<Object> objectsTable(Map<String, Object> data) {
        Object objects = data.get("$objects");
        if (!(objects instanceof List)) {
            throw new InvalidResponseError("NSKeyedArchiver archive has no $objects list");
        }
        @SuppressWarnings("unchecked")
        List<Object> list = (List<Object>) objects;
        return list;
    }

    private static @Nullable Object resolve(@Nullable Object value, List<Object> objects, List<Long> uidStack) {
        if (value instanceof Uid uid) {
            if (uid.value() >= objects.size()) {
                throw new InvalidResponseError("UID out of range: " + uid.value());
            }
            if (uidStack.contains(uid.value())) {
                throw new InvalidResponseError("Cyclic UID reference: " + uid.value());
            }
            uidStack.add(uid.value());
            Object resolved = resolve(objects.get((int) uid.value()), objects, uidStack);
            uidStack.remove(uidStack.size() - 1);
            return resolved;
        }
        if ("$null".equals(value)) {
            return null;
        }
        if (value instanceof List<?> list) {
            List<@Nullable Object> resolved = new ArrayList<>(list.size());
            for (Object item : list) {
                resolved.add(resolve(item, objects, uidStack));
            }
            return resolved;
        }
        if (value instanceof Map<?, ?> map) {
            return resolveKeyedObject(map, objects, uidStack);
        }
        return value;
    }

    private static @Nullable Object resolveKeyedObject(Map<?, ?> map, List<Object> objects, List<Long> uidStack) {
        String className = classNameOf(map, objects);
        if (className != null) {
            switch (className) {
                case "NSDictionary", "NSMutableDictionary" -> {
                    Object keys = resolve(map.get("NS.keys"), objects, uidStack);
                    Object values = resolve(map.get("NS.objects"), objects, uidStack);
                    if (keys instanceof List<?> keyList && values instanceof List<?> valueList
                            && keyList.size() == valueList.size()) {
                        Map<String, @Nullable Object> dict = new LinkedHashMap<>();
                        for (int i = 0; i < keyList.size(); i++) {
                            dict.put(String.valueOf(keyList.get(i)), valueList.get(i));
                        }
                        return dict;
                    }
                    throw new InvalidResponseError("Malformed NSDictionary in archive");
                }
                case "NSArray", "NSMutableArray", "NSSet", "NSMutableSet" -> {
                    Object values = resolve(map.get("NS.objects"), objects, uidStack);
                    if (values instanceof List) {
                        return values;
                    }
                    throw new InvalidResponseError("Malformed NSArray in archive");
                }
                case "NSString", "NSMutableString" -> {
                    return resolve(map.get("NS.string"), objects, uidStack);
                }
                case "NSData", "NSMutableData" -> {
                    return resolve(map.get("NS.data"), objects, uidStack);
                }
                default -> {
                    // Fall through to the generic field map below.
                }
            }
        }

        Map<String, @Nullable Object> resolved = new LinkedHashMap<>();
        for (Map.Entry<?, ?> entry : map.entrySet()) {
            String key = String.valueOf(entry.getKey());
            if ("$class".equals(key)) {
                continue;
            }
            resolved.put(key, resolve(entry.getValue(), objects, uidStack));
        }
        return resolved;
    }

    private static @Nullable String classNameOf(Map<?, ?> map, List<Object> objects) {
        Object classRef = map.get("$class");
        Object classObj = classRef;
        if (classRef instanceof Uid uid && uid.value() < objects.size()) {
            classObj = objects.get((int) uid.value());
        }
        if (classObj instanceof Map<?, ?> classMap && classMap.get("$classname") instanceof String name) {
            return name;
        }
        return null;
    }
}
