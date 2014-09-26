/**
 * Copyright (c) 2010-2014, openHAB.org and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.core.internal.inbox;

import java.util.Dictionary;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.smarthome.config.core.Configuration;
import org.eclipse.smarthome.config.discovery.DiscoveryResult;
import org.eclipse.smarthome.config.discovery.DiscoveryResultFlag;
import org.eclipse.smarthome.config.discovery.inbox.Inbox;
import org.eclipse.smarthome.config.discovery.inbox.InboxListener;
import org.eclipse.smarthome.core.thing.ManagedThingProvider;
import org.osgi.service.cm.ConfigurationException;
import org.osgi.service.cm.ManagedService;
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
public class AutoApproveService implements InboxListener, ManagedService {

	final static private Logger logger = LoggerFactory.getLogger(AutoApproveService.class);
	
	private ManagedThingProvider managedThingProvider;

	private Inbox inbox;
	
	@Override
	public void thingAdded(Inbox source, DiscoveryResult result) {
		logger.debug("Approving inbox entry '{}'", result.toString());
		Map<String, Object> props = new HashMap<>(result.getProperties());

		// TODO: this is a hack as long as we do not have a possibility to store localized labels for things
		props.put("label", result.getLabel());

		Configuration conf = new Configuration(props);
		
    	managedThingProvider.createThing(result.getThingTypeUID(), result.getThingUID(), result.getBridgeUID(), conf);
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


    protected void setManagedThingProvider(ManagedThingProvider managedThingProvider) {
        this.managedThingProvider = managedThingProvider;
    }

    protected void unsetManagedThingProvider(ManagedThingProvider managedThingProvider) {
        this.managedThingProvider = null;
    }

	@Override
	public void updated(Dictionary<String, ?> properties)
			throws ConfigurationException {
		String enabled = (String) properties.get("enabled");
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

}
