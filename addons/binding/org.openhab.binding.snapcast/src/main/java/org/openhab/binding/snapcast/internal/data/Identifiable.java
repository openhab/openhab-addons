/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
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
package org.openhab.binding.snapcast.internal.data;

/**
 * The {@link Identifiable} is a data structure with a id.
 *
 * @author Steffen Brandemann - Initial contribution
 */
public interface Identifiable {

    public String getId();

    public void setId(String id);
}
