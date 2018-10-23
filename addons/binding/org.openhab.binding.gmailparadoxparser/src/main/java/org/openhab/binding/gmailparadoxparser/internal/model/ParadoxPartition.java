/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.gmailparadoxparser.internal.model;

import java.util.HashMap;
import java.util.Map;

import org.openhab.binding.gmailparadoxparser.internal.GmailParadoxParserHandlerFactory;
import org.openhab.binding.gmailparadoxparser.internal.ParadoxStatesCache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link GmailParadoxParserHandlerFactory} is responsible for creating things and thing
 * handlers.
 *
 * @author Konstantin_Polihronov - Initial contribution
 */
public class ParadoxPartition {
    private static final Logger logger = LoggerFactory.getLogger(ParadoxStatesCache.class);

    private static Map<String, String> statesMap = new HashMap<>();
    static {
        statesMap.put("Disarming", "Disarmed");
        statesMap.put("Arming", "Armed");
    }

    private String state;
    private String id;
    private String activatedBy;
    private String time;

    public ParadoxPartition(String state, String partitionId, String activatedBy, String time) {
        String translatedState = statesMap.get(state);
        this.state = translatedState != null ? translatedState : state;
        this.id = partitionId;
        this.activatedBy = activatedBy;
        this.time = time;
        logger.debug("Created partition:" + this.toString());
    }

    public String getState() {
        return state;
    }

    public void setState(String message) {
        this.state = message;
    }

    @Override
    public String toString() {
        return ("partition=" + id + ", state=" + state + ", activatedBy=" + activatedBy + ", time=" + time);
    }

    public String getPartitionId() {
        return id;
    }

    public void setPartitionId(String partitionId) {
        this.id = partitionId;
    }

    public String getActivatedBy() {
        return activatedBy;
    }

    public void setActivatedBy(String activatedBy) {
        this.activatedBy = activatedBy;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((id == null) ? 0 : id.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        ParadoxPartition other = (ParadoxPartition) obj;
        if (id == null) {
            if (other.id != null) {
                return false;
            }
        } else if (!id.equals(other.id)) {
            return false;
        }
        return true;
    }

}
