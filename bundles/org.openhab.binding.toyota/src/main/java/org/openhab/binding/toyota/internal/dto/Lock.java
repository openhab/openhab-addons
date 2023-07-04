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
package org.openhab.binding.toyota.internal.dto;

import java.util.ArrayList;

/**
 * This class describes the car locking status
 *
 * @author Gaël L'hopital - Initial contribution
 */
public class Lock {
    public String lockState;
    public String source;
    public ArrayList<String> failedUnlockPreconditions;
}
