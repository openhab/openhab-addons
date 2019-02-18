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
package org.openhab.ui.cometvisu.internal.editor.dataprovider.beans;

import java.util.HashMap;
import java.util.Map;

/**
 * {@link ItemBean} is a helper bean used by the dataprovider-servlet
 * which delivers some additional data for the CometVisu-Editor
 *
 * @author Tobias Bräutigam - Initial contribution
 */
public class ItemBean extends DataBean {
    public Map<String, String> hints = new HashMap<>();
}
