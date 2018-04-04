/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.ui.cometvisu.internal.rss.beans;

import java.util.ArrayList;
import java.util.List;

/**
 * {@link Entry} is used by the CometVisu rss-plugin
 *
 * @author Tobias Bräutigam
 * @since 2.0.0
 */
public class Entry {
    public String id;
    public String title;
    public String content;
    public List<String> tags = new ArrayList<String>();
    public String state;
    public long publishedDate;
}
