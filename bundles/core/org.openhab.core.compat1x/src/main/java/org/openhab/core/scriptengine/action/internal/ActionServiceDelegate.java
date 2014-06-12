package org.openhab.core.scriptengine.action.internal;

import org.eclipse.smarthome.core.scriptengine.action.ActionService;


/**
 * This class serves as a mapping from the "old" org.openhab namespace to the new org.eclipse.smarthome
 * namespace for the action service. It wraps an instance with the old interface
 * into a class with the new interface. 
 * 
 * @author Kai Kreuzer - Initial contribution and API
 */
public class ActionServiceDelegate implements ActionService {

	private org.openhab.core.scriptengine.action.ActionService service;

	public ActionServiceDelegate(org.openhab.core.scriptengine.action.ActionService service) {
		this.service = service;
	}

	@Override
	public String getActionClassName() {
		return service.getActionClassName();
	}

	@Override
	public Class<?> getActionClass() {
		return service.getActionClass();
	}

}
