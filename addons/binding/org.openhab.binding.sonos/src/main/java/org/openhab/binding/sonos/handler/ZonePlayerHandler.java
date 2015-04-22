/**
 * Copyright (c) 2014-2015 openHAB UG (haftungsbeschraenkt) and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.sonos.handler;

import static org.openhab.binding.sonos.SonosBindingConstants.*;

import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang.StringUtils;
import org.eclipse.smarthome.config.core.Configuration;
import org.eclipse.smarthome.config.discovery.DiscoveryListener;
import org.eclipse.smarthome.config.discovery.DiscoveryResult;
import org.eclipse.smarthome.config.discovery.DiscoveryService;
import org.eclipse.smarthome.config.discovery.DiscoveryServiceRegistry;
import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.IncreaseDecreaseType;
import org.eclipse.smarthome.core.library.types.NextPreviousType;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.library.types.OpenClosedType;
import org.eclipse.smarthome.core.library.types.PercentType;
import org.eclipse.smarthome.core.library.types.PlayPauseType;
import org.eclipse.smarthome.core.library.types.RewindFastforwardType;
import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.library.types.UpDownType;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.State;
import org.eclipse.smarthome.core.types.UnDefType;
import org.eclipse.smarthome.io.net.http.HttpUtil;
import org.eclipse.smarthome.io.transport.upnp.UpnpIOParticipant;
import org.eclipse.smarthome.io.transport.upnp.UpnpIOService;
import org.openhab.binding.sonos.internal.SonosAlarm;
import org.openhab.binding.sonos.internal.SonosEntry;
import org.openhab.binding.sonos.internal.SonosMetaData;
import org.openhab.binding.sonos.internal.SonosXMLParser;
import org.openhab.binding.sonos.internal.SonosZoneGroup;
import org.openhab.binding.sonos.internal.SonosZonePlayerState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;

import static org.openhab.binding.sonos.config.ZonePlayerConfiguration.UDN;

/**
 * The {@link ZonePlayerHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 * 
 * @author Karel Goderis - Initial contribution
 */
public class ZonePlayerHandler extends BaseThingHandler implements
UpnpIOParticipant, DiscoveryListener {

	private Logger logger = LoggerFactory.getLogger(ZonePlayerHandler.class);

	private UpnpIOService service;
	private DiscoveryServiceRegistry discoveryServiceRegistry;
	private ScheduledFuture<?> pollingJob;
	private Calendar lastOPMLQuery = null;
	private SonosZonePlayerState savedState = null;

	private final static Collection<String> SERVICE_SUBSCRIPTIONS = Lists
			.newArrayList("DeviceProperties", "AVTransport",
					"ZoneGroupTopology", "GroupManagement", "RenderingControl",
					"AudioIn");
	protected final static int SUBSCRIPTION_DURATION = 600;
	private static final int SOCKET_TIMEOUT = 5000;

	/**
	 * The default refresh interval when not specified in channel configuration.
	 */
	private static final int DEFAULT_REFRESH_INTERVAL = 60;

	private Map<String, String> stateMap = Collections
			.synchronizedMap(new HashMap<String, String>());

	private Runnable pollingRunnable = new Runnable() {

		@Override
		public void run() {
			try {
				updateZoneInfo();
				updateRunningAlarmProperties();
				updateLed();
				updateMediaInfo();
			} catch (Exception e) {
				logger.debug("Exception during poll : {}", e);
			}
		}
	};

	private String opmlPartnerID;

	public ZonePlayerHandler(Thing thing, UpnpIOService upnpIOService,
			DiscoveryServiceRegistry discoveryServiceRegistry, String opmlPartnerID) {
		super(thing);
		this.opmlPartnerID = opmlPartnerID;

		logger.debug("Creating a ZonePlayerHandler for thing '{}'", getThing()
				.getUID());
		if (upnpIOService != null) {
			this.service = upnpIOService;
		}
		if (discoveryServiceRegistry != null) {
			this.discoveryServiceRegistry = discoveryServiceRegistry;
			this.discoveryServiceRegistry.addDiscoveryListener(this);
		}

	}

	@Override
	public void dispose() {
		logger.debug("Handler disposed.");

		if (pollingJob != null && !pollingJob.isCancelled()) {
			pollingJob.cancel(true);
			pollingJob = null;
		}

		if (getThing().getStatus() == ThingStatus.ONLINE) {
			logger.debug("Setting status for thing '{}' to OFFLINE", getThing()
					.getUID());
			updateStatus(ThingStatus.OFFLINE);
		}
	}

	@Override
	public void initialize() {

		Configuration configuration = getConfig();

		if (configuration.get("udn") != null) {
			onSubscription();
			onUpdate();
			super.initialize();
		} else {
			logger.warn("Cannot initalize the zoneplayer. UDN not set.");
		}
	}

	@Override
	public void thingDiscovered(DiscoveryService source, DiscoveryResult result) {
		if(result.getThingUID().equals(this.getThing().getUID())) {
			if (getThing().getConfiguration().get(UDN)
					.equals(result.getProperties().get(UDN))) {
				logger.debug("Discovered UDN '{}' for thing '{}'", result
						.getProperties().get(UDN), getThing().getUID());
				updateStatus(ThingStatus.ONLINE);
				onSubscription();
				onUpdate();
			}
		}
	}

	@Override
	public void thingRemoved(DiscoveryService source, ThingUID thingUID) {
		if(thingUID.equals(this.getThing().getUID())) {
			logger.debug("Setting status for thing '{}' to OFFLINE", getThing()
					.getUID());
			updateStatus(ThingStatus.OFFLINE);
		}
	}

	@Override
	public void handleCommand(ChannelUID channelUID, Command command) {
		switch (channelUID.getId()) {
		case LED:
			this.setLed(command);
			break;
		case MUTE:
			this.setMute(command);
			break;
		case STOP:
			stop();
			break;
		case VOLUME:
			setVolume(command);
			break;
		case ADD:
			addMember(command);
			break;
		case REMOVE:
			removeMember(command);
			break;
		case STANDALONE:
			becomeStandAlonePlayer();
			break;
		case PUBLICADDRESS:
			publicAddress();
			break;
		case RADIO:
			playRadio(command);
			break;
		case ALARM:
			setAlarm(command);
			break;
		case SNOOZE:
			snoozeAlarm(command);
			break;
		case SAVEALL:
			saveAllPlayerState();
			break;
		case RESTOREALL:
			restoreAllPlayerState();
			break;
		case SAVE:
			saveState();
			break;
		case RESTORE:
			restoreState();
			break;
		case PLAYLIST:
			playPlayList(command);
			break;
		case PLAYQUEUE:
			playQueue(command);
			break;		
		case PLAYTRACK:
			playTrack(command);
			break;	
		case PLAYURI:
			playURI(command);
			break;
		case PLAYLINEIN:
			playLineIn(command);
			break;
		case CONTROL:
			if (command instanceof PlayPauseType) {
				if (command == PlayPauseType.PLAY) {
					play();
				} else if (command == PlayPauseType.PAUSE) {
					pause();
				}
			}
			if (command instanceof NextPreviousType) {
				if (command == NextPreviousType.NEXT) {
					next();
				} else if (command == NextPreviousType.PREVIOUS) {
					previous();
				}
			}
			if (command instanceof RewindFastforwardType) {
				//Rewind and Fast Forward are currently not implemented by the binding
			}
			break;
		default:
			break;

		}
	}

	private void restoreAllPlayerState() {
		Collection<Thing> allThings = thingRegistry.getAll();
		for (Thing aThing : allThings) {
			if (aThing.getThingTypeUID().equals(
					this.getThing().getThingTypeUID())) {
				ZonePlayerHandler handler = (ZonePlayerHandler) aThing.getHandler();
				handler.restoreState();
			}
		}
	}

	private void saveAllPlayerState() {
		Collection<Thing> allThings = thingRegistry.getAll();
		for (Thing aThing : allThings) {
			if (aThing.getThingTypeUID().equals(
					this.getThing().getThingTypeUID())) {
				ZonePlayerHandler handler = (ZonePlayerHandler) aThing.getHandler();
				handler.saveState();
			}
		}
	}

	public void onValueReceived(String variable, String value, String service) {

		logger.trace("Received pair '{}':'{}' (service '{}') for thing '{}'", new Object[] {
				variable, value, service, this.getThing().getUID() });

		this.stateMap.put(variable, value);

		// pre-process some variables, eg XML processing
		if (service.equals("AVTransport") && variable.equals("LastChange")) {
			Map<String, String> parsedValues = SonosXMLParser
					.getAVTransportFromXML(value);
			for (String parsedValue : parsedValues.keySet()) {
				onValueReceived(parsedValue, parsedValues.get(parsedValue),
						"AVTransport");
			}
		}

		if (service.equals("RenderingControl") && variable.equals("LastChange")) {
			Map<String, String> parsedValues = SonosXMLParser
					.getRenderingControlFromXML(value);
			for (String parsedValue : parsedValues.keySet()) {
				onValueReceived(parsedValue, parsedValues.get(parsedValue),
						"RenderingControl");
			}
		}

		// update the appropriate channel
		switch (variable) {
		case "TransportState": {
			updateState(new ChannelUID(getThing().getUID(), STATE),
					(stateMap.get("TransportState") != null) ? new StringType(
							stateMap.get("TransportState")) : UnDefType.UNDEF);
			if (stateMap.get("TransportState").equals("PLAYING")) {
				updateState(new ChannelUID(getThing().getUID(), CONTROL),
						PlayPauseType.PLAY);
			}
			if (stateMap.get("TransportState").equals("STOPPED")) {
				updateState(new ChannelUID(getThing().getUID(), CONTROL),
						PlayPauseType.PAUSE);				
			}
			if (stateMap.get("TransportState").equals("PAUSED_PLAYBACK")) {
				updateState(new ChannelUID(getThing().getUID(), CONTROL),
						PlayPauseType.PAUSE);
			}
			break;
		}
		case "CurrentLEDState": {
			State newState = UnDefType.UNDEF;
			if (stateMap.get("CurrentLEDState") != null) {
				if (stateMap.get("CurrentLEDState").equals("On")) {
					newState = OnOffType.ON;
				} else {
					newState = OnOffType.OFF;
				}
			}
			updateState(new ChannelUID(getThing().getUID(), LED), newState);
			break;
		}
		case "CurrentZoneName": {
			updateState(new ChannelUID(getThing().getUID(), ZONENAME),
					(stateMap.get("CurrentZoneName") != null) ? new StringType(
							stateMap.get("CurrentZoneName")) : UnDefType.UNDEF);
		}
		case "ZoneGroupState": {
			updateState(new ChannelUID(getThing().getUID(), ZONEGROUP),
					(stateMap.get("ZoneGroupState") != null) ? new StringType(
							stateMap.get("ZoneGroupState")) : UnDefType.UNDEF);
			break;
		}
		case "LocalGroupUUID": {
			updateState(new ChannelUID(getThing().getUID(), ZONEGROUPID),
					(stateMap.get("LocalGroupUUID") != null) ? new StringType(
							stateMap.get("LocalGroupUUID")) : UnDefType.UNDEF);
			break;
		}
		case "GroupCoordinatorIsLocal": {
			State newState = UnDefType.UNDEF;
			if (stateMap.get("GroupCoordinatorIsLocal") != null) {
				if (stateMap.get("GroupCoordinatorIsLocal").equals("On")) {
					newState = OnOffType.ON;
				} else {
					newState = OnOffType.OFF;
				}
			}
			updateState(new ChannelUID(getThing().getUID(), LOCALCOORDINATOR),
					newState);
			break;
		}
		case "VolumeMaster": {
			updateState(new ChannelUID(getThing().getUID(), VOLUME),
					(stateMap.get("VolumeMaster") != null) ? new PercentType(
							stateMap.get("VolumeMaster")) : UnDefType.UNDEF);
			break;
		}
		case "MuteMaster": {
			State newState = UnDefType.UNDEF;
			if (stateMap.get("MuteMaster") != null) {
				if (stateMap.get("MuteMaster").equals("On")) {
					newState = OnOffType.ON;
				} else {
					newState = OnOffType.OFF;
				}
			}
			updateState(new ChannelUID(getThing().getUID(), MUTE), newState);
			break;
		}
		case "LineInConnected": {
			State newState = UnDefType.UNDEF;
			if (stateMap.get("LineInConnected") != null) {
				if (stateMap.get("LineInConnected").equals("On")) {
					newState = OnOffType.ON;
				} else {
					newState = OnOffType.OFF;
				}
			}
			updateState(new ChannelUID(getThing().getUID(), LINEIN), newState);
			break;
		}
		case "AlarmRunning": {
			State newState = UnDefType.UNDEF;
			if (stateMap.get("AlarmRunning") != null) {
				if (stateMap.get("AlarmRunning").equals("On")) {
					newState = OnOffType.ON;
				} else {
					newState = OnOffType.OFF;
				}
			}
			updateState(new ChannelUID(getThing().getUID(), ALARMRUNNING),
					newState);
			break;
		}
		case "RunningAlarmProperties": {
			updateState(
					new ChannelUID(getThing().getUID(), ALARMPROPERTIES),
					(stateMap.get("RunningAlarmProperties") != null) ? new StringType(
							stateMap.get("RunningAlarmProperties"))
					: UnDefType.UNDEF);
			break;
		}
		case "CurrentURIFormatted": {
			updateState(
					new ChannelUID(getThing().getUID(), CURRENTTRACK),
					(stateMap.get("CurrentURIFormatted") != null) ? new StringType(
							stateMap.get("CurrentURIFormatted"))
					: UnDefType.UNDEF);
			break;
		}
		case "CurrentTitle": {
			updateState(new ChannelUID(getThing().getUID(), CURRENTTITLE),
					(stateMap.get("CurrentTitle") != null) ? new StringType(
							stateMap.get("CurrentTitle")) : UnDefType.UNDEF);
			break;
		}
		case "CurrentArtist": {
			updateState(new ChannelUID(getThing().getUID(), CURRENTARTIST),
					(stateMap.get("CurrentArtist") != null) ? new StringType(
							stateMap.get("CurrentArtist")) : UnDefType.UNDEF);
			break;
		}
		case "CurrentAlbum": {
			updateState(new ChannelUID(getThing().getUID(), CURRENTALBUM),
					(stateMap.get("CurrentAlbum") != null) ? new StringType(
							stateMap.get("CurrentAlbum")) : UnDefType.UNDEF);
			break;
		}
		case "CurrentTrackMetaData": {
			updateTrackMetaData();
			break;
		}
		case "CurrentURI": {
			updateCurrentURIFormatted(value);
			break;
		}
		}

	}

	private synchronized void onSubscription() {
		// Set up GENA Subscriptions
		if (service.isRegistered(this)) {
			for (String subscription : SERVICE_SUBSCRIPTIONS) {
				service.addSubscription(this, subscription,
						SUBSCRIPTION_DURATION);
			}
		}
	}

	private synchronized void onUpdate() {
		if (service.isRegistered(this)) {
			if (pollingJob == null || pollingJob.isCancelled()) {
				Configuration config = getThing().getConfiguration();
				// use default if not specified
				int refreshInterval = DEFAULT_REFRESH_INTERVAL;
				Object refreshConfig = config.get("refresh");
				if (refreshConfig != null) {
					refreshInterval = ((BigDecimal) refreshConfig).intValue();
				}
				pollingJob = scheduler.scheduleAtFixedRate(pollingRunnable, 0,
						refreshInterval, TimeUnit.SECONDS);
			}
		}
	}

	protected void updateMediaInfo() {
		Map<String, String> inputs = new HashMap<String, String>();
		inputs.put("InstanceID", "0");

		Map<String, String> result = service.invokeAction(this, "AVTransport",
				"GetMediaInfo", inputs);

		for (String variable : result.keySet()) {
			this.onValueReceived(variable, result.get(variable), "AVTransport");
		}
	}

	protected void updateCurrentZoneName() {
		Map<String, String> result = service.invokeAction(this,
				"DeviceProperties", "GetZoneAttributes", null);

		for (String variable : result.keySet()) {
			this.onValueReceived(variable, result.get(variable),
					"DeviceProperties");
		}
	}

	protected void updateLed() {
		Map<String, String> result = service.invokeAction(this,
				"DeviceProperties", "GetLEDState", null);

		for (String variable : result.keySet()) {
			this.onValueReceived(variable, result.get(variable),
					"DeviceProperties");
		}
	}

	protected void updateTime() {
		Map<String, String> result = service.invokeAction(this, "AlarmClock",
				"GetTimeNow", null);

		for (String variable : result.keySet()) {
			this.onValueReceived(variable, result.get(variable), "AlarmClock");
		}
	}

	protected void updatePosition() {
		Map<String, String> result = service.invokeAction(this, "AVTransport",
				"GetPositionInfo", null);

		for (String variable : result.keySet()) {
			this.onValueReceived(variable, result.get(variable), "AVTransport");
		}
	}

	protected void updateRunningAlarmProperties() {
		Map<String, String> result = service.invokeAction(this, "AVTransport",
				"GetRunningAlarmProperties", null);

		String alarmID = result.get("AlarmID");
		String loggedStartTime = result.get("LoggedStartTime");
		String newStringValue = null;
		if (alarmID != null && loggedStartTime != null) {
			newStringValue = alarmID + " - " + loggedStartTime;
		} else {
			newStringValue = "No running alarm";
		}
		result.put("RunningAlarmProperties", newStringValue);

		for (String variable : result.keySet()) {
			this.onValueReceived(variable, result.get(variable), "AVTransport");
		}
	}

	protected void updateZoneInfo() {
		Map<String, String> result = service.invokeAction(this,
				"DeviceProperties", "GetZoneInfo", null);
		Map<String, String> result2 = service.invokeAction(this,
				"DeviceProperties", "GetZoneAttributes", null);

		result.putAll(result2);

		for (String variable : result.keySet()) {
			this.onValueReceived(variable, result.get(variable),
					"DeviceProperties");
		}
	}

	public String getCoordinator() {

		if (stateMap.get("ZoneGroupState") != null) {

			Collection<SonosZoneGroup> zoneGroups = SonosXMLParser
					.getZoneGroupFromXML(stateMap.get("ZoneGroupState"));

			for (SonosZoneGroup zg : zoneGroups) {
				if (zg.getMembers().contains(
						getThing().getConfiguration().get(UDN))) {
					return zg.getCoordinator();
				}
			}
		}

		return (String) getThing().getConfiguration().get(UDN);
	}

	public boolean isCoordinator() {
		return getUDN().equals(getCoordinator());
	}

	protected void updateTrackMetaData() {

		String coordinator = getCoordinator();
		ZonePlayerHandler coordinatorHandler = getHandlerByName(coordinator);
		SonosMetaData currentTrack = getTrackMetadata();

		if (coordinatorHandler != null && coordinatorHandler != this) {
			coordinatorHandler.updateMediaInfo();
			currentTrack = coordinatorHandler.getTrackMetadata();
		} 

		if (currentTrack != null) {

			String artist = null;
			if (currentTrack.getAlbumArtist().equals("")) {
				artist = currentTrack.getCreator();
			} else {
				artist = currentTrack.getAlbumArtist();
			}

			String album = currentTrack.getAlbum();
			String title = null;
			if(!currentTrack.getTitle().contains("x-sonosapi-stream")) {
				title = currentTrack.getTitle();
			}

			// update individual variables
			this.onValueReceived("CurrentArtist", (artist != null) ? artist
					: "", "AVTransport");
			if(title!=null) {
				this.onValueReceived("CurrentTitle", (title != null) ? title : "",
						"AVTransport");
			}
			this.onValueReceived("CurrentAlbum", (album != null) ? album : "",
					"AVTransport");

			updateMediaInfo();

		}

	}

	protected void updateCurrentURIFormatted(String URI) {

		String currentURI = URI;
		SonosMetaData currentTrack = null;
		String coordinator = getCoordinator();
		ZonePlayerHandler coordinatorHandler = getHandlerByName(coordinator);

		if (coordinatorHandler != null && coordinatorHandler != this) {
			if(currentURI.contains("x-rincon-stream")) {
				coordinatorHandler.updateMediaInfo();
			}
			currentURI = coordinatorHandler.getCurrentURI();
			currentTrack = coordinatorHandler.getTrackMetadata();
		} else {
			//			currentURI = getCurrentURI();
			currentTrack = getTrackMetadata();
		}

		if (currentURI != null) {
			String title = stateMap.get("CurrentTitle");
			String resultString = stateMap.get("CurrentURIFormatted");
			boolean needsUpdating = false;

			if (opmlPartnerID != null && currentURI.contains("x-sonosapi-stream")) {
				String stationID = StringUtils.substringBetween(currentURI,
						":s", "?sid");
				String previousStationID = stateMap.get("StationID");

				Calendar now = Calendar.getInstance();
				now.setTime(new Date());
				now.add(Calendar.MINUTE, -1);

				if (previousStationID == null
						|| !previousStationID.equals(stationID)
						|| lastOPMLQuery == null
						|| lastOPMLQuery.before(now)) {

					this.onValueReceived("StationID", stationID, "AVTransport");

					String url = "http://opml.radiotime.com/Describe.ashx?c=nowplaying"
							+ "&id=" + stationID
							+ "&partnerId=" + opmlPartnerID
							+ "&serial=" + getMACAddress();

					String response = HttpUtil.executeUrl("GET", url,
							SOCKET_TIMEOUT);

					if(lastOPMLQuery == null) {
						lastOPMLQuery = Calendar.getInstance();
					}
					lastOPMLQuery.setTime(new Date());

					if(response != null) {
						List<String> fields = SonosXMLParser
								.getRadioTimeFromXML(response);

						if (fields != null && fields.size() > 0) {

							resultString = new String();
							// radio name should be first field
							title = fields.get(0);

							Iterator<String> listIterator = fields.listIterator();
							while (listIterator.hasNext()) {
								String field = listIterator.next();
								resultString = resultString + field;
								if (listIterator.hasNext()) {
									resultString = resultString + " - ";
								}
							}

							needsUpdating = true;
						}
					}
				}
			} 

			if(currentURI.contains("x-rincon-stream")) {
				if(currentTrack != null) {
					resultString = stateMap.get("CurrentTitle");
					needsUpdating = true;
				}
			}


			if (!currentURI.contains("x-rincon-mp3") && !currentURI.contains("x-rincon-stream") && !currentURI.contains("x-sonosapi")) {
				if(currentTrack != null) {
					if (currentTrack.getAlbumArtist().equals("")) {
						resultString = currentTrack.getCreator() + " - "
								+ currentTrack.getAlbum() + " - "
								+ currentTrack.getTitle();
					} else {
						resultString = currentTrack.getAlbumArtist()
								+ " - " + currentTrack.getAlbum() + " - "
								+ currentTrack.getTitle();
					}

					needsUpdating = true;
				}
			}


			if(needsUpdating) {
				this.onValueReceived("CurrentURIFormatted", (resultString != null) ? resultString : "",
						"AVTransport");
				this.onValueReceived("CurrentTitle", (title != null) ? title : "",
						"AVTransport");
			}
		}
	}

	public boolean isGroupCoordinator() {
		String value = stateMap.get("GroupCoordinatorIsLocal");
		if (value != null) {
			return value.equals("1") ? true : false;
		}

		return false;

	}

	public String getUDN() {
		return (String) this.getThing().getConfiguration().get(UDN);
	}

	public String getCurrentURI() {
		return stateMap.get("CurrentURI");
	}

	public SonosMetaData getCurrentURIMetadata() {
		if (stateMap.get("CurrentURIMetaData") != null) {
			return SonosXMLParser.getMetaDataFromXML(stateMap
					.get("CurrentURIMetaData"));
		} else {
			return null;
		}
	}

	public SonosMetaData getTrackMetadata() {
		if (stateMap.get("CurrentTrackMetaData") != null) {
			return SonosXMLParser.getMetaDataFromXML(stateMap
					.get("CurrentTrackMetaData"));
		} else {
			return null;
		}
	}

	public SonosMetaData getEnqueuedTransportURIMetaData() {

		if (stateMap.get("EnqueuedTransportURIMetaData") != null) {
			return SonosXMLParser.getMetaDataFromXML(stateMap
					.get("EnqueuedTransportURIMetaData"));
		} else {
			return null;
		}
	}

	public String getMACAddress() {
		updateZoneInfo();
		return stateMap.get("MACAddress");
	}

	public String getPosition() {
		updatePosition();
		return stateMap.get("RelTime");
	}

	public long getCurrenTrackNr() {
		updatePosition();
		String value = stateMap.get("Track");
		if (value != null) {
			return Long.valueOf(value);
		} else {
			return (long) -1;
		}
	}

	public String getVolume() {
		return stateMap.get("VolumeMaster");
	}

	public String getTransportState() {
		return stateMap.get("TransportState");
	}

	public List<SonosEntry> getArtists(String filter) {
		return getEntries("A:", filter);
	}

	public List<SonosEntry> getArtists() {
		return getEntries("A:",
				"dc:title,res,dc:creator,upnp:artist,upnp:album");
	}

	public List<SonosEntry> getAlbums(String filter) {
		return getEntries("A:ALBUM", filter);
	}

	public List<SonosEntry> getAlbums() {
		return getEntries("A:ALBUM",
				"dc:title,res,dc:creator,upnp:artist,upnp:album");
	}

	public List<SonosEntry> getTracks(String filter) {
		return getEntries("A:TRACKS", filter);
	}

	public List<SonosEntry> getTracks() {
		return getEntries("A:TRACKS",
				"dc:title,res,dc:creator,upnp:artist,upnp:album");
	}

	public List<SonosEntry> getQueue(String filter) {
		return getEntries("Q:0", filter);
	}

	public List<SonosEntry> getQueue() {
		return getEntries("Q:0",
				"dc:title,res,dc:creator,upnp:artist,upnp:album");
	}

	public List<SonosEntry> getPlayLists(String filter) {
		return getEntries("SQ:", filter);
	}

	public List<SonosEntry> getPlayLists() {
		return getEntries("SQ:",
				"dc:title,res,dc:creator,upnp:artist,upnp:album");
	}

	public List<SonosEntry> getFavoriteRadios(String filter) {
		return getEntries("R:0/0", filter);
	}

	public List<SonosEntry> getFavoriteRadios() {
		return getEntries("R:0/0",
				"dc:title,res,dc:creator,upnp:artist,upnp:album");
	}

	protected List<SonosEntry> getEntries(String type, String filter) {
		long startAt = 0;

		Map<String, String> inputs = new HashMap<String, String>();
		inputs.put("ObjectID", type);
		inputs.put("BrowseFlag", "BrowseDirectChildren");
		inputs.put("Filter", filter);
		inputs.put("StartingIndex", Long.toString(startAt));
		inputs.put("RequestedCount", Integer.toString(200));
		inputs.put("SortCriteria", "");

		List<SonosEntry> resultList = null;

		Map<String, String> result = service.invokeAction(this,
				"ContentDirectory", "Browse", inputs);
		Long totalMatches = Long.valueOf(result.get("TotalMatches"));
		Long initialNumberReturned = Long.valueOf(result.get("NumberReturned"));
		String initialResult = result.get("Result");

		resultList = SonosXMLParser.getEntriesFromString(initialResult);
		startAt = startAt + initialNumberReturned;

		while (startAt < totalMatches) {

			inputs.put("StartingIndex", Long.toString(startAt));
			result = service.invokeAction(this, "ContentDirectory", "Browse",
					inputs);

			// Execute this action synchronously
			String nextResult = result.get("Result");
			Long numberReturned = Long.valueOf(result.get("NumberReturned"));

			resultList.addAll(SonosXMLParser.getEntriesFromString(nextResult));

			startAt = startAt + numberReturned;
		}

		return resultList;
	}

	/**
	 * Save the state (track, position etc) of the Sonos Zone player.
	 * 
	 * @return true if no error occurred.
	 */
	protected void saveState() {

		synchronized (this) {

			savedState = new SonosZonePlayerState();
			String currentURI = getCurrentURI();

			savedState.transportState = getTransportState();
			savedState.volume = getVolume();

			if (currentURI != null) {

				if (currentURI.contains("x-sonosapi-stream:")) {
					// we are streaming music
					SonosMetaData track = getTrackMetadata();
					SonosMetaData current = getCurrentURIMetadata();
					if (track != null) {
						savedState.entry = new SonosEntry("",
								current.getTitle(), "", "",
								track.getAlbumArtUri(), "",
								current.getUpnpClass(), currentURI);
					}
				} else if (currentURI.contains("x-rincon:")) {
					// we are a slave to some coordinator
					savedState.entry = new SonosEntry("", "", "", "", "", "",
							"", currentURI);
				} else if (currentURI.contains("x-rincon-stream:")) {
					// we are streaming from the Line In connection
					savedState.entry = new SonosEntry("", "", "", "", "", "",
							"", currentURI);
				} else if (currentURI.contains("x-rincon-queue:")) {
					// we are playing something that sits in the queue
					SonosMetaData queued = getEnqueuedTransportURIMetaData();
					if (queued != null) {

						savedState.track = getCurrenTrackNr();

						if (queued.getUpnpClass().contains(
								"object.container.playlistContainer")) {
							// we are playing a real 'saved' playlist
							List<SonosEntry> playLists = getPlayLists();
							for (SonosEntry someList : playLists) {
								if (someList.getTitle().equals(
										queued.getTitle())) {
									savedState.entry = new SonosEntry(
											someList.getId(),
											someList.getTitle(),
											someList.getParentId(), "", "", "",
											someList.getUpnpClass(),
											someList.getRes());
									break;
								}
							}

						} else if (queued.getUpnpClass().contains(
								"object.container")) {
							// we are playing some other sort of
							// 'container' - we will save that to a
							// playlist for our convenience
							logger.debug(
									"Save State for a container of type {}",
									queued.getUpnpClass());

							// save the playlist
							String existingList = "";
							List<SonosEntry> playLists = getPlayLists();
							for (SonosEntry someList : playLists) {
								if (someList.getTitle().equals(
										"openHAB-" + getUDN())) {
									existingList = someList.getId();
									break;
								}
							}

							saveQueue("openHAB-" + getUDN(), existingList);

							// get all the playlists and a ref to our
							// saved list
							playLists = getPlayLists();
							for (SonosEntry someList : playLists) {
								if (someList.getTitle().equals(
										"openHAB-" + getUDN())) {
									savedState.entry = new SonosEntry(
											someList.getId(),
											someList.getTitle(),
											someList.getParentId(), "", "", "",
											someList.getUpnpClass(),
											someList.getRes());
									break;
								}
							}

						}
					} else {
						savedState.entry = new SonosEntry("", "", "", "", "",
								"", "", "x-rincon-queue:" + getUDN() + "#0");
					}
				}

				savedState.relTime = getPosition();
			} else {
				savedState.entry = null;
			}
		}
	}

	/**
	 * Restore the state (track, position etc) of the Sonos Zone player.
	 * 
	 * @return true if no error occurred.
	 */
	protected void restoreState() {

		synchronized (this) {
			if (savedState != null) {
				// put settings back
				if(savedState.volume != null) {
					setVolume(DecimalType.valueOf(savedState.volume));
				}

				if (isCoordinator()) {
					if (savedState.entry != null) {
						// check if we have a playlist to deal with
						if (savedState.entry.getUpnpClass().contains(
								"object.container.playlistContainer")) {

							addURIToQueue(
									savedState.entry.getRes(),
									SonosXMLParser
									.compileMetadataString(savedState.entry),
									0, true);
							SonosEntry entry = new SonosEntry("", "", "", "",
									"", "", "", "x-rincon-queue:" + getUDN()
									+ "#0");
							setCurrentURI(entry);
							setPositionTrack(savedState.track);

						} else {
							setCurrentURI(savedState.entry);
							setPosition(savedState.relTime);
						}
					}

					if(savedState.transportState != null) {
						if (savedState.transportState.equals("PLAYING")) {
							play();
						} else if (savedState.transportState.equals("STOPPED")) {
							stop();
						} else if (savedState.transportState
								.equals("PAUSED_PLAYBACK")) {
							pause();
						}
					}
				}
			}
		}
	}

	public void saveQueue(String name, String queueID) {

		if (name != null && queueID != null) {

			Map<String, String> inputs = new HashMap<String, String>();
			inputs.put("Title", name);
			inputs.put("ObjectID", queueID);

			Map<String, String> result = service.invokeAction(this,
					"AVTransport", "SaveQueue", inputs);

			for (String variable : result.keySet()) {
				this.onValueReceived(variable, result.get(variable),
						"AVTransport");
			}
		}
	}

	public void setVolume(Command command) {
		if (command != null) {
			if (command instanceof OnOffType
					|| command instanceof IncreaseDecreaseType
					|| command instanceof DecimalType
					|| command instanceof PercentType) {

				Map<String, String> inputs = new HashMap<String, String>();

				String newValue = null;
				if (command instanceof IncreaseDecreaseType
						&& command == IncreaseDecreaseType.INCREASE) {
					int i = Integer.valueOf(this.getVolume());
					newValue = String.valueOf(Math.min(100, i + 1));
				} else if (command instanceof IncreaseDecreaseType
						&& command == IncreaseDecreaseType.DECREASE) {
					int i = Integer.valueOf(this.getVolume());
					newValue = String.valueOf(Math.max(0, i - 1));
				} else if (command instanceof OnOffType
						&& command == OnOffType.ON) {
					newValue = "100";
				} else if (command instanceof OnOffType
						&& command == OnOffType.OFF) {
					newValue = "0";
				} else if (command instanceof DecimalType) {
					newValue = command.toString();
				} else {
					return;
				}
				inputs.put("Channel", "Master");
				inputs.put("DesiredVolume", newValue);

				Map<String, String> result = service.invokeAction(this,
						"RenderingControl", "SetVolume", inputs);

				for (String variable : result.keySet()) {
					this.onValueReceived(variable, result.get(variable),
							"RenderingControl");
				}
			}
		}
	}

	public void addURIToQueue(String URI, String meta, int desiredFirstTrack,
			boolean enqueueAsNext) {

		if (URI != null && meta != null) {

			Map<String, String> inputs = new HashMap<String, String>();

			try {
				inputs.put("InstanceID", "0");
				inputs.put("EnqueuedURI", URI);
				inputs.put("EnqueuedURIMetaData", meta);
				inputs.put("DesiredFirstTrackNumberEnqueued",
						Integer.toString(desiredFirstTrack));
				inputs.put("EnqueueAsNext", Boolean.toString(enqueueAsNext));
			} catch (NumberFormatException ex) {
				logger.error("Action Invalid Value Format Exception {}",
						ex.getMessage());
			}

			Map<String, String> result = service.invokeAction(this,
					"AVTransport", "AddURIToQueue", inputs);

			for (String variable : result.keySet()) {
				this.onValueReceived(variable, result.get(variable),
						"AVTransport");
			}
		}
	}

	public void setCurrentURI(SonosEntry newEntry) {
		setCurrentURI(newEntry.getRes(),
				SonosXMLParser.compileMetadataString(newEntry));
	}

	public void setCurrentURI(String URI, String URIMetaData) {
		if (URI != null && URIMetaData != null) {

			Map<String, String> inputs = new HashMap<String, String>();

			try {
				inputs.put("InstanceID", "0");
				inputs.put("CurrentURI", URI);
				inputs.put("CurrentURIMetaData", URIMetaData);
			} catch (NumberFormatException ex) {
				logger.error("Action Invalid Value Format Exception {}",
						ex.getMessage());
			}

			Map<String, String> result = service.invokeAction(this,
					"AVTransport", "SetAVTransportURI", inputs);

			for (String variable : result.keySet()) {
				this.onValueReceived(variable, result.get(variable),
						"AVTransport");
			}
		}
	}

	public void setPosition(String relTime) {
		seek("REL_TIME", relTime);
	}

	public void setPositionTrack(long tracknr) {
		seek("TRACK_NR", Long.toString(tracknr));
	}

	public void setPositionTrack(String tracknr) {
		seek("TRACK_NR", tracknr);		
	}

	protected void seek(String unit, String target) {
		if (unit != null && target != null) {

			Map<String, String> inputs = new HashMap<String, String>();

			try {
				inputs.put("InstanceID", "0");
				inputs.put("Unit", unit);
				inputs.put("Target", target);
			} catch (NumberFormatException ex) {
				logger.error("Action Invalid Value Format Exception {}",
						ex.getMessage());
			}

			Map<String, String> result = service.invokeAction(this,
					"AVTransport", "Seek", inputs);

			for (String variable : result.keySet()) {
				this.onValueReceived(variable, result.get(variable),
						"AVTransport");
			}
		}
	}

	public void play() {

		Map<String, String> inputs = new HashMap<String, String>();
		inputs.put("Speed", "1");

		Map<String, String> result = service.invokeAction(this, "AVTransport",
				"Play", inputs);

		for (String variable : result.keySet()) {
			this.onValueReceived(variable, result.get(variable), "AVTransport");
		}
	}

	public void stop() {
		Map<String, String> result = service.invokeAction(this, "AVTransport",
				"Stop", null);

		for (String variable : result.keySet()) {
			this.onValueReceived(variable, result.get(variable), "AVTransport");
		}
	}

	public void pause() {
		Map<String, String> result = service.invokeAction(this, "AVTransport",
				"Pause", null);

		for (String variable : result.keySet()) {
			this.onValueReceived(variable, result.get(variable), "AVTransport");
		}
	}

	/**
	 * Clear all scheduled music from the current queue.
	 * 
	 */
	public void removeAllTracksFromQueue() {
		Map<String, String> inputs = new HashMap<String, String>();
		inputs.put("InstanceID", "0");

		Map<String, String> result = service.invokeAction(this, "AVTransport",
				"RemoveAllTracksFromQueue", inputs);

		for (String variable : result.keySet()) {
			this.onValueReceived(variable, result.get(variable), "AVTransport");
		}
	}

	/**
	 * Play music from the line-in of the given Player referenced by the given UDN or name
	 * 
	 * @param udn or name
	 */
	public void playLineIn(Command command) {

		if (command != null && command instanceof StringType) {

			String remotePlayerName = command.toString();
			String coordinatorUDN = getCoordinator();
			ZonePlayerHandler coordinatorHandler = getHandlerByName(coordinatorUDN);
			ZonePlayerHandler remoteHandler = getHandlerByName(remotePlayerName);

			if(coordinatorHandler!=null && remoteHandler!=null) {

				// stop whatever is currently playing
				coordinatorHandler.stop();

				// set the URI
				coordinatorHandler.setCurrentURI("x-rincon-stream:"
						+ remoteHandler.getConfig().get(UDN), "");

				// take the system off mute
				coordinatorHandler.setMute(OnOffType.OFF);

				// start jammin'
				coordinatorHandler.play();
			}
		}
	}

	protected ZonePlayerHandler getHandlerByName(String remotePlayerName) {

		if(thingRegistry!=null) {
			Thing thing = thingRegistry.get(new ThingUID(
					ZONEPLAYER_THING_TYPE_UID, remotePlayerName));

			if (thing == null) {
				Collection<Thing> allThings = thingRegistry.getAll();
				for (Thing aThing : allThings) {
					if (aThing.getThingTypeUID().equals(
							this.getThing().getThingTypeUID())) {
						if (aThing.getConfiguration().get(UDN)
								.equals(remotePlayerName)) {
							thing = aThing;
							break;
						}
					}
				}
			}

			if(thing != null) { 
				return (ZonePlayerHandler) thing.getHandler();
			}
		}
		return null;

	}

	public void setMute(Command command) {
		if (command != null) {
			if (command instanceof OnOffType
					|| command instanceof OpenClosedType
					|| command instanceof UpDownType) {

				Map<String, String> inputs = new HashMap<String, String>();
				inputs.put("Channel", "Master");

				if (command.equals(OnOffType.ON)
						|| command.equals(UpDownType.UP)
						|| command.equals(OpenClosedType.OPEN)) {
					inputs.put("DesiredMute", "True");
				} else if (command.equals(OnOffType.OFF)
						|| command.equals(UpDownType.DOWN)
						|| command.equals(OpenClosedType.CLOSED)) {
					inputs.put("DesiredMute", "False");

				}

				Map<String, String> result = service.invokeAction(this,
						"RenderingControl", "SetMute", inputs);

				for (String variable : result.keySet()) {
					this.onValueReceived(variable, result.get(variable),
							"RenderingControl");
				}
			}
		}
	}

	public List<SonosAlarm> getCurrentAlarmList() {
		Map<String, String> result = service.invokeAction(this, "AlarmClock",
				"ListAlarms", null);

		for (String variable : result.keySet()) {
			this.onValueReceived(variable, result.get(variable), "AlarmClock");
		}

		return SonosXMLParser.getAlarmsFromStringResult(result
				.get("CurrentAlarmList"));
	}

	public void updateAlarm(SonosAlarm alarm) {
		if (alarm != null) {

			Map<String, String> inputs = new HashMap<String, String>();

			try {
				inputs.put("ID", Integer.toString(alarm.getID()));
				inputs.put("StartLocalTime", alarm.getStartTime());
				inputs.put("Duration", alarm.getDuration());
				inputs.put("Recurrence", alarm.getRecurrence());
				inputs.put("RoomUUID", alarm.getRoomUUID());
				inputs.put("ProgramURI", alarm.getProgramURI());
				inputs.put("ProgramMetaData", alarm.getProgramMetaData());
				inputs.put("PlayMode", alarm.getPlayMode());
				inputs.put("Volume", Integer.toString(alarm.getVolume()));
				if (alarm.getIncludeLinkedZones()) {
					inputs.put("IncludeLinkedZones", "1");
				} else {
					inputs.put("IncludeLinkedZones", "0");
				}

				if (alarm.getEnabled()) {
					inputs.put("Enabled", "1");
				} else {
					inputs.put("Enabled", "0");
				}
			} catch (NumberFormatException ex) {
				logger.error("Action Invalid Value Format Exception {}",
						ex.getMessage());
			}

			Map<String, String> result = service.invokeAction(this,
					"AlarmClock", "UpdateAlarm", inputs);

			for (String variable : result.keySet()) {
				this.onValueReceived(variable, result.get(variable),
						"AlarmClock");
			}
		}
	}

	public void setAlarm(Command command) {
		if (command instanceof OnOffType || command instanceof OpenClosedType
				|| command instanceof UpDownType) {
			if (command.equals(OnOffType.ON) || command.equals(UpDownType.UP)
					|| command.equals(OpenClosedType.OPEN)) {
				setAlarm(true);
			} else if (command.equals(OnOffType.OFF)
					|| command.equals(UpDownType.DOWN)
					|| command.equals(OpenClosedType.CLOSED)) {
				setAlarm(false);
			}
		}
	}

	public void setAlarm(boolean alarmSwitch) {

		List<SonosAlarm> sonosAlarms = getCurrentAlarmList();

		// find the nearest alarm - take the current time from the Sonos System,
		// not the system where openhab is running
		SimpleDateFormat fmt = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		fmt.setTimeZone(TimeZone.getTimeZone("GMT"));

		String currentLocalTime = getTime();
		Date currentDateTime = null;
		try {
			currentDateTime = fmt.parse(currentLocalTime);
		} catch (ParseException e) {
			logger.error("An exception occurred while formatting a date");
			e.printStackTrace();
		}

		if(currentDateTime != null) {
			Calendar currentDateTimeCalendar = Calendar.getInstance();
			currentDateTimeCalendar.setTimeZone(TimeZone.getTimeZone("GMT"));
			currentDateTimeCalendar.setTime(currentDateTime);
			currentDateTimeCalendar.add(Calendar.DAY_OF_YEAR, 10);
			long shortestDuration = currentDateTimeCalendar.getTimeInMillis() - currentDateTime.getTime();

			SonosAlarm firstAlarm = null;

			for (SonosAlarm anAlarm : sonosAlarms) {
				SimpleDateFormat durationFormat = new SimpleDateFormat("HH:mm:ss");
				durationFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
				Date durationDate = null;
				try {
					durationDate = durationFormat.parse(anAlarm.getDuration());
				} catch (ParseException e) {
					logger.error("An exception occurred while parsing a date : '{}'",e.getMessage());
				}

				long duration = durationDate.getTime();

				if (duration < shortestDuration
						&& anAlarm.getRoomUUID().equals(getUDN())) {
					shortestDuration = duration;
					firstAlarm = anAlarm;
				}
			}

			// Set the Alarm
			if (firstAlarm != null) {

				if (alarmSwitch) {
					firstAlarm.setEnabled(true);
				} else {
					firstAlarm.setEnabled(false);
				}

				updateAlarm(firstAlarm);

			}
		}
	}

	public String getTime() {
		updateTime();
		return stateMap.get("CurrentLocalTime");
	}

	public Boolean isAlarmRunning() {
		return stateMap.get("AlarmRunning").equals("1") ? true : false;
	}

	public void snoozeAlarm(Command command) {
		if (isAlarmRunning() && command instanceof DecimalType) {

			int minutes = ((DecimalType) command).intValue();

			Map<String, String> inputs = new HashMap<String, String>();

			Calendar snoozePeriod = Calendar.getInstance();
			snoozePeriod.setTimeZone(TimeZone.getTimeZone("GMT"));
			snoozePeriod.setTimeInMillis(0);
			snoozePeriod.add(Calendar.MINUTE, minutes);
			SimpleDateFormat pFormatter = new SimpleDateFormat("HH:mm:ss");
			pFormatter.setTimeZone(TimeZone.getTimeZone("GMT"));

			try {
				inputs.put("Duration", pFormatter.format(snoozePeriod.getTime()));
			} catch (NumberFormatException ex) {
				logger.error("Action Invalid Value Format Exception {}",
						ex.getMessage());
			}

			Map<String, String> result = service.invokeAction(this,
					"AVTransport", "SnoozeAlarm", inputs);

			for (String variable : result.keySet()) {
				this.onValueReceived(variable, result.get(variable),
						"AVTransport");
			}
		} else {
			logger.warn("There is no alarm running on {} ", this);
		}
	}

	public Boolean isLineInConnected() {
		return stateMap.get("LineInConnected").equals("1") ? true : false;
	}

	public void becomeStandAlonePlayer() {
		Map<String, String> result = service.invokeAction(this, "AVTransport",
				"BecomeCoordinatorOfStandaloneGroup", null);

		for (String variable : result.keySet()) {
			this.onValueReceived(variable, result.get(variable), "AVTransport");
		}
	}

	public void addMember(Command command) {
		if (command != null && command instanceof StringType) {
			SonosEntry entry = new SonosEntry("", "", "", "", "", "", "",
					"x-rincon:" + getUDN());
			getHandlerByName(command.toString()).setCurrentURI(entry);
		}
	}

	public boolean publicAddress() {
		// check if sourcePlayer has a line-in connected
		if (isLineInConnected()) {

			// first remove this player from its own group if any
			becomeStandAlonePlayer();

			List<SonosZoneGroup> currentSonosZoneGroups = new ArrayList<SonosZoneGroup>();
			for (SonosZoneGroup grp : SonosXMLParser
					.getZoneGroupFromXML(stateMap.get("ZoneGroupState"))) {
				currentSonosZoneGroups.add((SonosZoneGroup) grp.clone());
			}

			// add all other players to this new group
			for (SonosZoneGroup group : currentSonosZoneGroups) {
				for (String player : group.getMembers()) {
					ZonePlayerHandler somePlayer = getHandlerByName(player);
					if (somePlayer != this) {
						somePlayer.becomeStandAlonePlayer();
						somePlayer.stop();
						addMember(StringType.valueOf(somePlayer.getUDN()));
					}
				}
			}

			// set the URI of the group to the line-in
			ZonePlayerHandler coordinator = getHandlerByName(getCoordinator());
			SonosEntry entry = new SonosEntry("", "", "", "", "", "", "",
					"x-rincon-stream:" + getUDN());
			coordinator.setCurrentURI(entry);
			coordinator.play();

			return true;

		} else {
			logger.warn("Line-in of {} is not connected", this);
			return false;
		}

	}

	/**
	 * Play a given url to music in one of the music libraries.
	 * 
	 * @param url
	 *            in the format of //host/folder/filename.mp3
	 */
	public void playURI(Command command) {

		if (command != null && command instanceof StringType) {

			String url = command.toString();

			ZonePlayerHandler coordinator = getHandlerByName(getCoordinator());

			// stop whatever is currently playing
			coordinator.stop();

			// clear any tracks which are pending in the queue
			coordinator.removeAllTracksFromQueue();

			// add the new track we want to play to the queue
			// The url will be prefixed with x-file-cifs if it is NOT a http URL
			if (!url.startsWith("x-") && (!url.startsWith("http"))) {
				// default to file based url
				url = "x-file-cifs:" + url;
			}
			coordinator.addURIToQueue(url, "", 0, true);

			// set the current playlist to our new queue
			coordinator.setCurrentURI("x-rincon-queue:" + getUDN() + "#0", "");

			// take the system off mute
			coordinator.setMute(OnOffType.OFF);

			// start jammin'
			coordinator.play();
		}

	}

	public void playQueue(Command command) {
		ZonePlayerHandler coordinator = getHandlerByName(getCoordinator());

		// set the current playlist to our new queue
		coordinator.setCurrentURI("x-rincon-queue:" + getUDN() + "#0", "");

		// take the system off mute
		coordinator.setMute(OnOffType.OFF);

		// start jammin'
		coordinator.play();
	}

	public void setLed(Command command) {
		if (command != null) {
			if (command instanceof OnOffType
					|| command instanceof OpenClosedType
					|| command instanceof UpDownType) {

				Map<String, String> inputs = new HashMap<String, String>();

				if (command.equals(OnOffType.ON)
						|| command.equals(UpDownType.UP)
						|| command.equals(OpenClosedType.OPEN)) {
					inputs.put("DesiredLEDState", "On");
				} else if (command.equals(OnOffType.OFF)
						|| command.equals(UpDownType.DOWN)
						|| command.equals(OpenClosedType.CLOSED)) {
					inputs.put("DesiredLEDState", "Off");

				}

				Map<String, String> result = service.invokeAction(this,
						"DeviceProperties", "SetLEDState", inputs);

				for (String variable : result.keySet()) {
					this.onValueReceived(variable, result.get(variable),
							"DeviceProperties");
				}
			}
		}
	}

	public void removeMember(Command command) {
		if (command != null && command instanceof StringType) {
			ZonePlayerHandler oldmemberHandler = getHandlerByName(command
					.toString());

			oldmemberHandler.becomeStandAlonePlayer();
			SonosEntry entry = new SonosEntry("", "", "", "", "", "", "",
					"x-rincon-queue:" + oldmemberHandler.getUDN() + "#0");
			oldmemberHandler.setCurrentURI(entry);
		}
	}

	public void previous() {
		Map<String, String> result = service.invokeAction(this, "AVTransport",
				"Previous", null);

		for (String variable : result.keySet()) {
			this.onValueReceived(variable, result.get(variable), "AVTransport");
		}
	}

	public void next() {
		Map<String, String> result = service.invokeAction(this, "AVTransport",
				"Next", null);

		for (String variable : result.keySet()) {
			this.onValueReceived(variable, result.get(variable), "AVTransport");
		}
	}

	public void playRadio(Command command) {
		List<SonosEntry> stations = getFavoriteRadios();
		SonosEntry theEntry = null;

		if (command instanceof StringType) {
			String station = command.toString();
			// search for the appropriate radio based on its name (title)
			for (SonosEntry someStation : stations) {
				if (someStation.getTitle().equals(station)) {
					theEntry = someStation;
					break;
				}
			}

			// set the URI of the group coordinator
			if (theEntry != null) {
				ZonePlayerHandler coordinator = getHandlerByName(getCoordinator());
				coordinator.setCurrentURI(theEntry);
				coordinator.play();
			}
		}

	}

	public void playTrack(Command command) {

		if(command != null && command instanceof DecimalType) {
			ZonePlayerHandler coordinator = getHandlerByName(getCoordinator());

			String trackNumber = command.toString();

			// seek the track - warning, we do not check if the tracknumber falls in the boundary of the queue
			setPositionTrack(trackNumber);

			// take the system off mute
			coordinator.setMute(OnOffType.OFF);

			// start jammin'
			coordinator.play();
		}

	}

	public void playPlayList(Command command) {
		List<SonosEntry> playlists = getPlayLists();
		SonosEntry theEntry = null;

		if (command != null && command instanceof StringType) {

			String playlist = command.toString();

			// search for the appropriate play list based on its name (title)
			for (SonosEntry somePlaylist : playlists) {
				if (somePlaylist.getTitle().equals(playlist)) {
					theEntry = somePlaylist;
					break;
				}
			}

			// set the URI of the group coordinator
			if (theEntry != null) {

				ZonePlayerHandler coordinator = getHandlerByName(getCoordinator());
				// coordinator.setCurrentURI(theEntry);
				coordinator.addURIToQueue(theEntry);

				if (stateMap != null) {
					String firstTrackNumberEnqueued = stateMap
							.get("FirstTrackNumberEnqueued");
					if (firstTrackNumberEnqueued != null) {
						coordinator.seek("TRACK_NR", firstTrackNumberEnqueued);
					}
				}

				coordinator.play();
			}
		}
	}

	public void addURIToQueue(SonosEntry newEntry) {
		addURIToQueue(newEntry.getRes(),
				SonosXMLParser.compileMetadataString(newEntry), 1, true);
	}

	public String getZoneName() {
		return stateMap.get("ZoneName");
	}

	public String getZoneGroupID() {
		return stateMap.get("LocalGroupUUID");
	}

	public String getRunningAlarmProperties() {
		updateRunningAlarmProperties();
		return stateMap.get("RunningAlarmProperties");
	}

	public String getMute() {
		return stateMap.get("MuteMaster");
	}

	public boolean getLed() {
		return stateMap.get("CurrentLEDState").equals("On") ? true : false;
	}

	public String getCurrentZoneName() {
		updateCurrentZoneName();
		return stateMap.get("CurrentZoneName");
	}

	public String getCurrentURIFormatted() {
		updateCurrentURIFormatted(getCurrentURI());
		return stateMap.get("CurrentURIFormatted");
	}

    @Override
    public void onStatusChanged(boolean status) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public Collection<ThingUID> removeOlderResults(DiscoveryService source, long timestamp,
            Collection<ThingTypeUID> thingTypeUIDs) {
        // TODO Auto-generated method stub
        return null;
    }

}
