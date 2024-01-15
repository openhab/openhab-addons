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
package org.openhab.binding.freecurrency.internal.dto;

import java.math.BigDecimal;
import java.util.Map;

/**
 * The {@link ExchangeRatesDTO} class is used to retrieve the exchange-rates for all currencies
 *
 * @author Jan N. Klug - Initial contribution
 */
public class ExchangeRatesDTO {
    public Map<String, String> meta;
    public Map<String, BigDecimal> data;
}
