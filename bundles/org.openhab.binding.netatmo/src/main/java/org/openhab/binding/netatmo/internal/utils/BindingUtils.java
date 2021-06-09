/**
 * Copyright (c) 2010-2021 Contributors to the openHAB project
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
package org.openhab.binding.netatmo.internal.utils;

import java.util.Collections;
import java.util.Dictionary;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.osgi.service.component.ComponentContext;

/**
 *
 * @author Gaël L'hopital - Initial contribution
 *
 */

public class BindingUtils {
    public static Map<String, Object> ComponentContextToMap(ComponentContext componentContext) {
        Dictionary<String, Object> properties = componentContext.getProperties();
        List<String> keys = Collections.list(properties.keys());
        Map<String, Object> dictCopy = keys.stream().collect(Collectors.toMap(Function.identity(), properties::get));
        return dictCopy;
    }
}
