/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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
package org.openhab.binding.huesync.internal.log;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Helper class to use [en] resource files to log messages to be consistent
 * with exception message displayed on screen.
 * 
 * @author Patrik Gfeller - Initial contribution
 */
public class HueSyncLogFactory {

    public static Logger getLogger(Class<?> clazz) {
        return LoggerFactory.getLogger(clazz);
    }
}
