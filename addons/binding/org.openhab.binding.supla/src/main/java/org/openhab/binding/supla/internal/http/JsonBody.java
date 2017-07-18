/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.supla.internal.http;

import org.openhab.binding.supla.internal.mappers.JsonMapper;

import java.nio.charset.Charset;
import java.util.Map;

import static com.google.common.base.Preconditions.checkNotNull;

public final class JsonBody extends Body {
    private final JsonMapper mapper;

    public JsonBody(Map<String, String> body, JsonMapper mapper) {
        super(body);
        this.mapper = checkNotNull(mapper);
    }

    @Override
    public byte[] buildBytesToSend() {
        return mapper.map(body).getBytes(Charset.forName("UTF-8"));
    }
}
