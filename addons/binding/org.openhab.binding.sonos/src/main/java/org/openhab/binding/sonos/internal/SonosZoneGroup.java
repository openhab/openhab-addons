package org.openhab.binding.sonos.internal;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class SonosZoneGroup implements Cloneable {

	private final List<String> members;
	private final String coordinator;
	private final String id;

	public Object clone() {
		try
		{
			return super.clone();
		}
		catch(Exception e){ return null; }
	}

	public SonosZoneGroup(String id, String coordinator, Collection<String> members) {
		this.members= new ArrayList<String>(members);
		if (!this.members.contains(coordinator)) {
			this.members.add(coordinator);
		}
		this.coordinator = coordinator;
		this.id = id;
	}

	public List<String> getMembers() {
		return members;
	}

	public String getCoordinator() {
		return coordinator;
	}

	public String getId() {
		return id;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null) {
			return false;
		}
		if (obj instanceof SonosZoneGroup) {
			SonosZoneGroup group = (SonosZoneGroup) obj;
			return group.getId().equals(getId());
		}
		return false;
	}
	
	@Override
	public int hashCode() {
	  return id.hashCode();
	}
	
}	
