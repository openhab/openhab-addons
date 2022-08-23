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
package org.openhab.binding.insteon.internal.message;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Definition (layout) of an Insteon message. Says which bytes go where.
 * For more info, see the public Insteon Developer's Guide, 2nd edition,
 * and the Insteon Modem Developer's Guide.
 *
 * @author Daniel Pfrommer - Initial contribution
 * @author Rob Nielsen - Port to openHAB 2 insteon binding
 * @author Jeremy Setton - Improvements for openHAB 3 insteon binding
 */
@NonNullByDefault
public class MsgDefinition {
    private Map<String, Field> fields = new HashMap<>();

    MsgDefinition() {
    }

    /*
     * Copy constructor, needed to make a copy of a message
     *
     * @param m the definition to copy
     */
    MsgDefinition(MsgDefinition m) {
        fields = new HashMap<>(m.fields);
    }

    public List<Field> getFields() {
        return fields.values().stream().sorted(Comparator.comparing(Field::getOffset)).collect(Collectors.toList());
    }

    public boolean containsField(String name) {
        return fields.containsKey(name);
    }

    public void addField(Field field) {
        fields.put(field.getName(), field);
    }

    /**
     * Finds field of a given name
     *
     * @param name name of the field to search for
     * @return reference to field
     * @throws FieldException if no such field can be found
     */
    public Field getField(String name) throws FieldException {
        Field field = fields.get(name);
        if (field == null) {
            throw new FieldException("field " + name + " not found");
        }
        return field;
    }
}
