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
@org.osgi.annotation.bundle.Header(name = org.osgi.framework.Constants.DYNAMICIMPORT_PACKAGE, value = "*")
package org.openhab.persistence.jpa.internal;

/**
 * This dynamic import is required for loading the JDBC driver class.
 *
 * @author Wouter Born - Initial contribution
 */
