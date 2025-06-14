/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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
package org.openhab.binding.sedif.internal.dto;

import java.util.ArrayList;
import java.util.List;

/**
 * The {@link Contracts} holds Contract information
 *
 * @author Laurent Arnal - Initial contribution
 */
public class Contracts extends Value {
    public List<Contract> contrats = new ArrayList<Contract>();
}
