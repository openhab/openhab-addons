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
package org.openhab.io.neeo.internal;

import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import javax.ws.rs.client.ClientBuilder;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jetty.http.HttpStatus;
import org.openhab.core.thing.ThingUID;
import org.openhab.io.neeo.internal.models.NeeoThingUID;
import org.openhab.io.neeo.internal.net.HttpRequest;
import org.openhab.io.neeo.internal.net.HttpResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

/**
 * Stores the mapping of {@link ThingUID} to all brain keys
 *
 * @author Tim Roberts - Initial Contribution
 */
@NonNullByDefault
public class NeeoDeviceKeys {

    /** The logger */
    private final Logger logger = LoggerFactory.getLogger(NeeoDeviceKeys.class);

    /** The mapping between ThingUID to brain keys */
    private final Map<NeeoThingUID, Set<String>> uidToKey = new ConcurrentHashMap<>();

    /** The brain's url */
    private final String brainUrl;

    private final ClientBuilder clientBuilder;

    /**
     * Creates the object from the context and brainUrl
     *
     * @param brainUrl the non-empty brain url
     */
    NeeoDeviceKeys(String brainUrl, ClientBuilder clientBuilder) {
        NeeoUtil.requireNotEmpty(brainUrl, "brainUrl cannot be empty");

        this.brainUrl = brainUrl;
        this.clientBuilder = clientBuilder;
    }

    /**
     * Refreshes the keys from the brain
     *
     * @throws IOException Signals that an I/O exception has occurred.
     */
    void refresh() throws IOException {
        try (HttpRequest request = new HttpRequest(clientBuilder)) {
            logger.debug("Getting existing device mappings from {}{}", brainUrl, NeeoConstants.PROJECTS_HOME);
            final HttpResponse resp = request.sendGetCommand(brainUrl + NeeoConstants.PROJECTS_HOME);
            if (resp.getHttpCode() != HttpStatus.OK_200) {
                throw resp.createException();
            }

            uidToKey.clear();

            final JsonObject root = JsonParser.parseString(resp.getContent()).getAsJsonObject();
            for (Map.Entry<String, JsonElement> room : root.getAsJsonObject("rooms").entrySet()) {
                final JsonObject roomObj = (JsonObject) room.getValue();
                for (Map.Entry<String, JsonElement> dev : roomObj.getAsJsonObject("devices").entrySet()) {
                    final JsonObject devObj = (JsonObject) dev.getValue();
                    final String key = devObj.get("key").getAsString();

                    final JsonObject det = devObj.getAsJsonObject("details");
                    final String adapterName = det.get("adapterName").getAsString();

                    NeeoThingUID thingUID = null;
                    try {
                        thingUID = new NeeoThingUID(adapterName);
                    } catch (IllegalArgumentException e) {
                        logger.debug("Invalid UID (probably not an openhab thing): {} for key {}", adapterName, key);
                    }

                    if (thingUID != null) {
                        final Set<String> newMap = ConcurrentHashMap.newKeySet();
                        final Set<String> uidKeys = uidToKey.putIfAbsent(thingUID, newMap);
                        (uidKeys == null ? newMap : uidKeys).add(key);
                    }
                }
            }
        }
    }

    /**
     * Adds the NEEO device key to the relationship with the UID
     *
     * @param uid the non-null uid
     * @param key the non-empty key
     */
    public void put(NeeoThingUID uid, String key) {
        Objects.requireNonNull(uid, "uid cannot be null");
        NeeoUtil.requireNotEmpty(key, "key cannot be empty");

        final Set<String> newMap = ConcurrentHashMap.newKeySet();
        final Set<String> uidKeys = uidToKey.putIfAbsent(uid, newMap);
        (uidKeys == null ? newMap : uidKeys).add(key);
    }

    /**
     * Removes all keys for the UID
     *
     * @param uid the non-null uid
     * @return true if keys were removed, false otherwise
     */
    public boolean remove(NeeoThingUID uid) {
        Objects.requireNonNull(uid, "uid cannot be null");

        final Set<String> keys = uidToKey.remove(uid);
        return keys != null;
    }

    /**
     * Gets the keys for the given uid
     *
     * @param uid the non-null uid
     * @return a non-null, possibly empty set of device keys
     */
    public Set<String> get(NeeoThingUID uid) {
        Objects.requireNonNull(uid, "uid cannot be null");

        final Set<String> keys = uidToKey.get(uid);
        return keys == null ? new HashSet<>() : Collections.unmodifiableSet(keys);
    }

    /**
     * Returns true if the given uid has been bound on the NEEO Brain (ie has a key)
     *
     * @param uid the non-null uid
     * @return true if bound, false otherwise
     */
    boolean isBound(NeeoThingUID uid) {
        Objects.requireNonNull(uid, "uid cannot be null");

        return uidToKey.containsKey(uid);
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder(200);
        for (Entry<NeeoThingUID, Set<String>> entry : uidToKey.entrySet()) {
            final Set<String> entries = entry.getValue();
            if (entries == null) {
                continue;
            }
            if (sb.length() > 0) {
                sb.append(",");
            }
            sb.append("[");
            sb.append(entry.getKey());
            sb.append("=");
            sb.append(entries.stream().map(Object::toString).collect(Collectors.joining()));
            sb.append("]");
        }

        return "NeeoDeviceKeys [uidToKey=" + sb.toString() + ", brainUrl=" + brainUrl + "]";
    }
}
