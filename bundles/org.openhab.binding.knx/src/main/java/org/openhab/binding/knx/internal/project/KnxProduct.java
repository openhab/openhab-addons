/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
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
package org.openhab.binding.knx.internal.project;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * A product of a manufacturer's {@code Hardware.xml}, as a device of the project refers to it.
 *
 * @param id {@code Product@Id}, the device's {@link KnxDevice#productRefId()}
 * @param text the product text in the file's default language
 * @param orderNumber the manufacturer's order number
 *
 * @author Stamate Viorel - Initial contribution
 */
@NonNullByDefault
public record KnxProduct(String id, @Nullable String text, @Nullable String orderNumber) {
}
