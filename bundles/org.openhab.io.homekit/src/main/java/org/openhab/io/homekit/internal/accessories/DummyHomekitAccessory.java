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
package org.openhab.io.homekit.internal.accessories;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.json.JsonValue;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.io.homekit.internal.HomekitTaggedItem;

import io.github.hapjava.accessories.HomekitAccessory;
import io.github.hapjava.characteristics.Characteristic;
import io.github.hapjava.services.Service;

/**
 * Implements a dummy placeholder accessory for when configuration is missing
 *
 * @author Cody Cutrer - Initial contribution
 */
@NonNullByDefault({})
public class DummyHomekitAccessory implements HomekitAccessory {
    private static class DummyCharacteristic implements Characteristic {
        private JsonObject json;
        private String type;

        public DummyCharacteristic(JsonObject json) {
            this.json = json;
            type = json.getString("type");
            // reconstitute shortened IDs
            if (type.length() < 8) {
                type = "0".repeat(8 - type.length()) + type + "-0000-1000-8000-0026BB765291";
            }
        }

        @Override
        public String getType() {
            return type;
        }

        @Override
        public void supplyValue(JsonObjectBuilder characteristicBuilder) {
            characteristicBuilder.add("value", json.get("value"));
        }

        @Override
        public CompletableFuture<JsonObject> toJson(int iid) {
            var builder = Json.createObjectBuilder();
            json.forEach((k, v) -> builder.add(k, v));
            builder.add("iid", iid);
            return CompletableFuture.completedFuture(builder.build());
        }

        @Override
        public void setValue(JsonValue jsonValue) {
        }
    }

    private static class DummyService implements Service {
        private String type;
        private List<Characteristic> characteristics = new ArrayList();
        private List<Service> linkedServices = new ArrayList();

        public DummyService(JsonObject json) {
            type = json.getString("type");
            json.getJsonArray("c").forEach(c -> {
                characteristics.add(new DummyCharacteristic((JsonObject) c));
            });
            var ls = json.getJsonArray("ls");
            if (ls != null) {
                ls.forEach(s -> {
                    addLinkedService(new DummyService((JsonObject) s));
                });
            }
        }

        @Override
        public String getType() {
            return type;
        }

        @Override
        public List<Characteristic> getCharacteristics() {
            return characteristics;
        }

        @Override
        public List<Service> getLinkedServices() {
            return linkedServices;
        }

        @Override
        public void addLinkedService(Service service) {
            linkedServices.add(service);
        }
    };

    int id;
    String item;
    List<Service> services = new ArrayList();

    public DummyHomekitAccessory(String item, String data) {
        this.id = HomekitTaggedItem.calculateId(item);
        this.item = item;

        var reader = Json.createReader(new StringReader(data));
        var services = reader.readArray();
        reader.close();

        services.forEach(s -> {
            this.services.add(new DummyService((JsonObject) s));
        });
    }

    @Override
    public int getId() {
        return id;
    }

    @Override
    public CompletableFuture<String> getName() {
        return CompletableFuture.completedFuture(item);
    }

    @Override
    public void identify() {
    }

    @Override
    public CompletableFuture<String> getSerialNumber() {
        return CompletableFuture.completedFuture(item);
    }

    @Override
    public CompletableFuture<String> getModel() {
        return CompletableFuture.completedFuture("none");
    }

    @Override
    public CompletableFuture<String> getManufacturer() {
        return CompletableFuture.completedFuture("none");
    }

    @Override
    public CompletableFuture<String> getFirmwareRevision() {
        return CompletableFuture.completedFuture("none");
    }
}
