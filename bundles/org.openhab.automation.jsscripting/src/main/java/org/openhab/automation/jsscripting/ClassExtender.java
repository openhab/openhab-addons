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
package org.openhab.automation.jsscripting;

import com.oracle.truffle.js.runtime.java.adapter.JavaAdapterFactory;

/**
 * Class utility to allow creation of 'extendable' classes with a classloader of the GraalJS bundle, rather than the
 * classloader of the file being extended.
 *
 * @author Jonathan Gilbert - Initial contribution
 */
public class ClassExtender {
    private static ClassLoader classLoader = ClassExtender.class.getClassLoader();

    public static Object extend(String className) {
        try {
            return extend(Class.forName(className));
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("Cannot find class " + className, e);
        }
    }

    public static Object extend(Class<?> clazz) {
        return JavaAdapterFactory.getAdapterClassFor(clazz, null, classLoader);
    }
}
