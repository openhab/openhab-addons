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
package org.openhab.binding.ambientweather.internal.processor;

/**
 * The {@link ProcessorNotFoundException} is thrown when a data event is
 * received for which there's no processor that can process the event.
 *
 * @author Mark Hilbush - Initial contribution
 */
public class ProcessorNotFoundException extends Exception {
    private static final long serialVersionUID = -7522973666620330850L;

    public ProcessorNotFoundException(String message) {
        super(message);
    }
}
