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
package org.openhab.ui.cometvisu.internal.rss.beans;

import java.util.ArrayList;
import java.util.List;

/**
 * {@link Entry} is used by the CometVisu rss-plugin
 *
 * @author Tobias Bräutigam - Initial contribution
 */
public class Entry {
    public String id;
    public String title;
    public String content;
    public List<String> tags = new ArrayList<>();
    public String state;
    public long publishedDate;
}
