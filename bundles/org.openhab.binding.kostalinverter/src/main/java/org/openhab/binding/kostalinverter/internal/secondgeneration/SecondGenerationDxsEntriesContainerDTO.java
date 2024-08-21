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
package org.openhab.binding.kostalinverter.internal.secondgeneration;

import java.util.List;

/**
 * The {@link SecondGenerationDxsEntriesContainerDTO} class defines a Container, which is
 * used in the second generation part of the binding.
 *
 * @author Örjan Backsell - Initial contribution Piko1020, Piko New Generation
 */

public class SecondGenerationDxsEntriesContainerDTO {
    public List<SecondGenerationDxsEntries> dxsEntries;
}
