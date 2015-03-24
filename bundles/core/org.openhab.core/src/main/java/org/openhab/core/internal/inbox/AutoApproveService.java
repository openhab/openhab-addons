/**
 * Copyright (c) 2014-2015 openHAB UG (haftungsbeschraenkt) and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.core.internal.inbox;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.smarthome.config.core.Configuration;
import org.eclipse.smarthome.config.discovery.DiscoveryResult;
import org.eclipse.smarthome.config.discovery.DiscoveryResultFlag;
import org.eclipse.smarthome.config.discovery.inbox.Inbox;
import org.eclipse.smarthome.config.discovery.inbox.InboxListener;
import org.eclipse.smarthome.core.thing.setup.ThingSetupManager;
import org.osgi.service.cm.ConfigurationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class is an OSGi service, which automatically approves all newly discovered things in the inbox.
 * As a result, the inbox will always be empty and as thing instances are immediately created.
 * This feature needs to be activated through a configuration, which is provided in services.cfg.
 * 
 * @author Kai Kreuzer
 *
 */
public class AutoApproveService implements InboxListener {

	final static private Logger logger = LoggerFactory.getLogger(AutoApproveService.class);
	
	private ThingSetupManager thingSetupManager;

	private Inbox inbox;
	
    protected void activate(Map<String, Object> configProps) throws ConfigurationException {
		String enabled = (String) configProps.get("enabled");
		enable(enabled);
    }

    protected void modified(Map<String, Object> configProps) throws ConfigurationException {
        String enabled = (String) configProps.get("enabled");
        enable(enabled);
    }
    
	private void enable(String enabled) {
        if("true".equalsIgnoreCase(enabled)) {
        	inbox.addInboxListener(this);
        	for(DiscoveryResult result : inbox.getAll()) {
        		if(result.getFlag().equals(DiscoveryResultFlag.NEW)) {
        			thingAdded(inbox, result);
        		}
        	}
    	} else {
        	this.inbox.removeInboxListener(this);
    	}
    }

    @Override
	public void thingAdded(Inbox source, DiscoveryResult result) {
		logger.debug("Approving inbox entry '{}'", result.toString());
		Map<String, Object> props = new HashMap<>(result.getProperties());

		Configuration conf = new Configuration(props);
		
    	thingSetupManager.addThing(result.getThingUID(), conf, result.getBridgeUID(), result.getLabel());
	}

	@Override
	public void thingUpdated(Inbox source, DiscoveryResult result) {
	}

	@Override
	public void thingRemoved(Inbox source, DiscoveryResult result) {
	}

    protected void setInbox(Inbox inbox) {    	
    	this.inbox = inbox;
    }
    
    protected void unsetInbox(Inbox inbox) {
    	this.inbox.removeInboxListener(this);
    	this.inbox = null;
    }


    protected void setThingSetupManager(ThingSetupManager thingSetupManager) {
        this.thingSetupManager = thingSetupManager;
    }

    protected void unsetThingSetupManager(ThingSetupManager thingSetupManager) {
        this.thingSetupManager = null;
    }

}
