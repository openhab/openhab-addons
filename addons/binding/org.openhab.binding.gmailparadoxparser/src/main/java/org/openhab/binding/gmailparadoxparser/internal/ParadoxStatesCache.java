/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.gmailparadoxparser.internal;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.openhab.binding.gmailparadoxparser.internal.model.ParadoxPartition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link GmailParadoxParserHandlerFactory} is responsible for creating things and thing
 * handlers.
 *
 * @author Konstantin_Polihronov - Initial contribution
 */
public class ParadoxStatesCache implements Cache<String, ParadoxPartition> {

    private static ParadoxStatesCache instance;
    private Map<String, ParadoxPartition> partitionsStates = new HashMap<String, ParadoxPartition>();
    private final Logger logger = LoggerFactory.getLogger(ParadoxStatesCache.class);

    public static ParadoxStatesCache getInstance() {
        synchronized (ParadoxStatesCache.class) {
            if (instance == null) {
                instance = new ParadoxStatesCache();
            }
        }
        return instance;
    }

    @Override
    public void put(String key, ParadoxPartition value) {
        synchronized (ParadoxStatesCache.class) {
            logger.debug("Putting key: " + key);
            logger.debug("Value: " + value.toString());
            partitionsStates.put(key, value);
        }

    }

    @Override
    public ParadoxPartition get(String key) {
        return partitionsStates.get(key);
    }

    @Override
    public void refresh(List<String> retrievedMessages) {
        Set<ParadoxPartition> partitionsUpdatedStates = ParadoxMailParser.parseToParadoxPartitions(retrievedMessages);

        if (partitionsUpdatedStates.isEmpty()) {
            logger.debug("Received empty set. Nothing to update.");
            return;
        }

        synchronized (ParadoxStatesCache.class) {
            for (ParadoxPartition paradoxPartition : partitionsUpdatedStates) {
                put(paradoxPartition.getPartitionId(), paradoxPartition);
            }
        }
    }
}
