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
package org.openhab.automation.jsscripting.internal.scope;

//import com.oracle.truffle.js.runtime.java.adapter.JavaAdapterFactory;

/**
 * Class utility to allow creation of 'extendable' classes with a classloader of the current bundle, rather than the
 * classloader of the file being extended.
 *
 * @author Jonathan Gilbert - Initial contribution
 */
public class ClassExtender {
    private ClassLoader classLoader = getClass().getClassLoader();
}
