/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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
package org.openhab.binding.hive.internal.client.adapter;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.io.StringWriter;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.Test;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonWriter;

/**
 *
 *
 * @author Ross Brown - Initial contribution
 */
@NonNullByDefault
public abstract class GsonAdapterTestBase<T extends TypeAdapter<?>> {
    public static final String JSON_NULL = "null";

    protected abstract T getAdapter();

    protected String toJsonString(final String string) {
        return "\"" + string + "\"";
    }

    /**
     * Makes sure we actually output null instead of throwing a NPE.
     */
    @Test
    public void testNullValue() throws IOException {
        /* Given */
        final StringWriter stringWriter = new StringWriter();
        final JsonWriter jsonWriter = new JsonWriter(stringWriter);

        final T adapter = getAdapter();


        /* When */
        adapter.write(jsonWriter, null);


        /* Then */
        // No exceptions hopefully!
        assertThat(stringWriter.toString()).isEqualTo(JSON_NULL);
    }
}
