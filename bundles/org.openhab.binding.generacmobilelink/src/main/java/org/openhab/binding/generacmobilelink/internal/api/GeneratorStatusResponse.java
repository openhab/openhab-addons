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
package org.openhab.binding.generacmobilelink.internal.api;

import java.util.ArrayList;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * {@link GeneratorStatusResponse} response from the MobileLink API
 *
 * @author Dan Cunningham - Initial contribution
 */
@SuppressWarnings("serial")
@NonNullByDefault
public class GeneratorStatusResponse extends ArrayList<GeneratorStatus> {

}
