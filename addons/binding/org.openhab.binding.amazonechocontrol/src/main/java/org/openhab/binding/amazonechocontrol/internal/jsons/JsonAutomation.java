/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.amazonechocontrol.internal.jsons;

import java.util.TreeMap;

/**
 * The {@link JsonAutomation} encapsulate the GSON data of automation query
 *
 * @author Michael Geramb - Initial contribution
 */
public class JsonAutomation {
    public String automationId;
    public String name;
    public Trigger[] triggers;
    public TreeMap<String, Object> sequence;
    public String status;
    public long creationTimeEpochMillis;
    public long lastUpdatedTimeEpochMillis;

    public class Trigger {
        public Payload payload;
        public String id;
        public String type;
    }

    public class Payload {
        public String customerId;
        public String utterance;
        public String locale;
        public String marketplaceId;
    }
}
