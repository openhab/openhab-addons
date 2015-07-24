/**
 * Copyright (c) 2010-2015, openHAB.org and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.netatmo.internal.messages;

import static org.apache.commons.lang.builder.ToStringStyle.SHORT_PREFIX_STYLE;

import java.util.List;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.codehaus.jackson.annotate.JsonAutoDetect;
import org.codehaus.jackson.annotate.JsonAutoDetect.Visibility;

/**
 * Java Bean to represent a JSON response to a <code>User</code> API method
 * call.
 * 
 * @author Gaël L'hopital
 */
@JsonAutoDetect(fieldVisibility = Visibility.ANY)
public class UserBody extends AbstractMessage {

	@JsonAutoDetect(fieldVisibility = Visibility.ANY)
	public static class AdministrativePart {
		protected String country;
		protected Integer feel_like_algo;
		protected String lang;
		protected Integer pressureunit;
		protected String reg_locale;
		protected Integer unit;
		protected Integer windunit;

		@Override
		public String toString() {
			final ToStringBuilder builder = new ToStringBuilder(this,
					SHORT_PREFIX_STYLE);
			builder.appendSuper(super.toString());
			builder.append("country", country);
			builder.append("feelLikeAlgo", feel_like_algo);
			builder.append("lang", lang);
			builder.append("pressureUnit", pressureunit);
			builder.append("regLocale", reg_locale);
			builder.append("unit", unit);
			builder.append("windUnit", windunit);

			return builder.toString();
		}

	};

	@JsonAutoDetect(fieldVisibility = Visibility.ANY)
	public static class DateCreationPart {
		protected Double sec;
		protected Integer usec;

		@Override
		public String toString() {
			final ToStringBuilder builder = new ToStringBuilder(this,
					SHORT_PREFIX_STYLE);
			builder.appendSuper(super.toString());
			builder.append("sec", sec);
			builder.append("usec", usec);

			return builder.toString();
		}

	};

	public String getId() {
		return _id;
	}

	public AdministrativePart getAdministrative() {
		return administrative;
	}

	public DateCreationPart getDateCreation() {
		return date_creation;
	}

	public List<String> getDevices() {
		return devices;
	}

	public List<String> getFriendDevices() {
		return friend_devices;
	}

	public Boolean getFacebookLikeDisplayed() {
		return facebook_like_displayed;
	}

	public String getMail() {
		return mail;
	}

	public Integer getTimelineNotRead() {
		return timeline_not_read;
	}

	protected String _id;
	protected AdministrativePart administrative;
	protected DateCreationPart date_creation;
	protected List<String> devices;
	protected List<String> friend_devices;
	protected Boolean facebook_like_displayed;
	protected String mail;
	protected Integer timeline_not_read;

	@Override
	public String toString() {
		final ToStringBuilder builder = createToStringBuilder();
		builder.appendSuper(super.toString());

		builder.append("id", getId());
		builder.append("administrative", getAdministrative());
		builder.append("dateCreation", getDateCreation());
		builder.append("devices", getDevices());
		builder.append("friendDevices", getFriendDevices());
		builder.append("facebookLikeDisplayed", getFacebookLikeDisplayed());
		builder.append("mail", getMail());
		builder.append("timeLineNotRead", getTimelineNotRead());

		return builder.toString();
	}
}
