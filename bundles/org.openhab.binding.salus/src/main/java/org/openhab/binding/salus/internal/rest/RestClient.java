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
package org.openhab.binding.salus.internal.rest;

import java.util.List;
import java.util.function.Function;

/**
 * @author Martin Grześlowski - Initial contribution
 */
public interface RestClient {
    Response<String> get(String url, Header... headers);

    Response<String> post(String url, Content content, Header... headers);

    record Content(String body, String type) {
        public Content(String body) {
            this(body, "application/json");
        }
    }

    record Header(String name, List<String> values) {
        public Header(String name, String value) {
            this(name, List.of(value));
        }
    }

    record Response<T> (int statusCode, T body) {
        public <Y> Response<Y> map(Function<T, Y> mapper) {
            return new Response<>(statusCode, mapper.apply(body));
        }
    }
}