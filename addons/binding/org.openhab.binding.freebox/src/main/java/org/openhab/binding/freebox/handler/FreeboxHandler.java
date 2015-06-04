/**
 * Copyright (c) 2014-2015 openHAB UG (haftungsbeschraenkt) and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.freebox.handler;

import java.util.List;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.smarthome.core.library.types.DateTimeType;
import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.IncreaseDecreaseType;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.library.types.OpenClosedType;
import org.eclipse.smarthome.core.library.types.PercentType;
import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.library.types.UpDownType;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.types.Command;
import org.matmaul.freeboxos.FreeboxException;
import org.matmaul.freeboxos.FreeboxOsClient;
import org.matmaul.freeboxos.call.CallEntry;
import org.matmaul.freeboxos.connection.ConnectionStatus;
import org.matmaul.freeboxos.connection.xDslStatus;
import org.matmaul.freeboxos.lcd.LCDConfig;
import org.matmaul.freeboxos.login.Authorize;
import org.matmaul.freeboxos.login.LoginManager;
import org.matmaul.freeboxos.login.TrackAuthorizeStatus;
import org.matmaul.freeboxos.phone.PhoneStatus;
import org.matmaul.freeboxos.system.SystemConfiguration;
import org.matmaul.freeboxos.wifi.WifiGlobalConfig;
import org.openhab.binding.freebox.config.FreeboxServerConfiguration;
import org.osgi.framework.Bundle;
import org.osgi.framework.FrameworkUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.openhab.binding.freebox.FreeboxBindingConstants.*;

/**
 * The {@link FreeboxHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 * 
 * @author Gaël L'hopital - Initial contribution
 */
public class FreeboxHandler extends BaseThingHandler {

    private Logger logger = LoggerFactory.getLogger(FreeboxHandler.class);
    private ScheduledFuture<?> globalJob;
    private ScheduledFuture<?> phoneJob;
    
    private static FreeboxOsClient fbClient = null;
	private long uptime = -1;
	
	public FreeboxHandler(Thing thing) {
		super(thing);
	}

	@Override
	public void handleCommand(ChannelUID channelUID, Command command) {
		try {
			switch (channelUID.getId()) {
				case LCDBRIGHTNESS : setBrightness(command);
					break;
				case LCDORIENTATION : setOrientation(command);
					break;
				case LCDFORCED : setForced(command);
					break;
				case WIFISTATUS : setWifiStatus(command);
					break;
				case REBOOT : setReboot(command);
			}
		} catch (FreeboxException e) {
			logger.error(e.getMessage());
		}
	}
	
	/**
	 * Handles connection to the Freebox, including validation of the Apptoken
	 * if none is provided in configuration
	 * @throws FreeboxException
	 */
	private boolean authorize() {
		
		FreeboxServerConfiguration configuration = getConfigAs(FreeboxServerConfiguration.class);
		
		Bundle bundle = FrameworkUtil.getBundle(getClass());			
		
		fbClient = new FreeboxOsClient(
					bundle.getSymbolicName(), /*  org.openhab.binding.freebox */
					configuration.ipAddress);
		
				
		LoginManager loginManager = fbClient.getLoginManager();
		TrackAuthorizeStatus authorizeStatus = TrackAuthorizeStatus.UNKNOWN;
		try {
			
		if (configuration.appToken == null || configuration.appToken.isEmpty()) {
			
			Authorize authorize = loginManager.newAuthorize(
						bundle.getHeaders().get("Bundle-Name"),													// Freebox Binding
						String.format("%d.%d",bundle.getVersion().getMajor(),bundle.getVersion().getMinor()), 	// eg. 1.5
						bundle.getHeaders().get("Bundle-Vendor"));
			

			configuration.appToken = authorize.getAppToken();

			logger.info("####################################################################");
			logger.info("# Please accept activation request directly on your freebox        #");
			logger.info("# Once done, record Apptoken in the Freebox Item configuration     #");
			logger.info("# " + configuration.appToken +" #");
			logger.info("####################################################################");
			
			do {
					Thread.sleep(2000);
					authorizeStatus = loginManager.trackAuthorize();
			} while (authorizeStatus == TrackAuthorizeStatus.PENDING);
		} else {
			authorizeStatus = TrackAuthorizeStatus.GRANTED;
		}
		
		if (authorizeStatus != TrackAuthorizeStatus.GRANTED)
			return false;
		
		logger.debug("Apptoken valide : [" + configuration.appToken + "]");
		loginManager.setAppToken(configuration.appToken);
		loginManager.openSession();
		return true;
		} catch (FreeboxException | InterruptedException e) {
			logger.error(e.getMessage());
			return false;
		}
	}
	
	@Override
	public void initialize() {
		if (authorize()) {
			updateStatus(ThingStatus.ONLINE);
		    	
			if (globalJob == null || globalJob.isCancelled()) {
				long polling_interval = getConfigAs(FreeboxServerConfiguration.class).refreshInterval;
				globalJob = scheduler.scheduleAtFixedRate(globalRunnable, 1, polling_interval, TimeUnit.SECONDS);
			}
				
			if (phoneJob == null || phoneJob.isCancelled()) {
				long polling_interval = getConfigAs(FreeboxServerConfiguration.class).refreshPhoneInterval;
				phoneJob = scheduler.scheduleAtFixedRate(phoneRunnable, 1, polling_interval, TimeUnit.SECONDS);
			} 
		} else {
		    updateStatus(ThingStatus.OFFLINE);
		}
	}
	
	private Runnable phoneRunnable = new Runnable() {
			@Override
			public void run() {
				List<PhoneStatus> phoneStatus;
				try {
					phoneStatus = fbClient.getPhoneManager().getPhoneStatus();
					updateState(new ChannelUID(getThing().getUID(), ONHOOK), 
							phoneStatus.get(0).getOn_hook() ? OnOffType.ON : OnOffType.OFF);
					updateState(new ChannelUID(getThing().getUID(), RINGING), 
							phoneStatus.get(0).getIs_ringing() ? OnOffType.ON : OnOffType.OFF);
				} catch (FreeboxException e) {
					logger.error(e.getMessage());
					updateStatus(ThingStatus.OFFLINE);
				}
				
			}
	};
	
	private Runnable globalRunnable = new Runnable() {
			@Override
			public void run() {
				
				try {
					fetchSystemConfig();
					fetchLCDConfig();					
					fetchWifiConfig();
					fetchxDslStatus();
					fetchConnectionStatus();
					fetchNewCalls();
					
				} catch (FreeboxException e) {
					logger.error(e.getMessage());
					updateStatus(ThingStatus.OFFLINE);
				}
				
			}
	};
	
	@Override
	public void dispose() {
		logger.debug("Disposing Freebox Server handler.");
		if (globalJob != null && !globalJob.isCancelled()) {
			globalJob.cancel(true);
			globalJob = null;
		}
		if (phoneJob != null && !phoneJob.isCancelled()) {
			phoneJob.cancel(true);
			phoneJob = null;
		}
		updateStatus(ThingStatus.OFFLINE);
	}
	
	private void fetchNewCalls() throws FreeboxException {
		List<CallEntry> callEntries = fbClient.getCallManager().getCallEntries();
		for (CallEntry call: callEntries) {
			if (call.is_new_()) {
				updateState(new ChannelUID(getThing().getUID(), CALLNUMBER), 
						new StringType(call.getNumber()));
				updateState(new ChannelUID(getThing().getUID(), CALLDURATION), 
						new DecimalType(call.getDuration()));
				updateState(new ChannelUID(getThing().getUID(), CALLTIMESTAMP), 
						new DateTimeType(call.getTimeStamp()));
				updateState(new ChannelUID(getThing().getUID(), CALLSTATUS), 
						new StringType(call.getType()));
				updateState(new ChannelUID(getThing().getUID(), CALLNAME), 
						new StringType(call.getName()));
									
				call.setNew(false);
				fbClient.getCallManager().setCallEntry(call);
			}
		}		
	}
	
	private void fetchConnectionStatus() throws FreeboxException {
		ConnectionStatus connectionStatus = fbClient.getConnectionManager().getStatus();
		updateState(new ChannelUID(getThing().getUID(), LINESTATUS), 
				new StringType(connectionStatus.getState()));
		updateState(new ChannelUID(getThing().getUID(), IPV4), 
				new StringType(connectionStatus.getIpv4()));
		updateState(new ChannelUID(getThing().getUID(), RATEUP), 
				new DecimalType(connectionStatus.getRate_up()));
		updateState(new ChannelUID(getThing().getUID(), RATEDOWN), 
				new DecimalType(connectionStatus.getRate_down()));
		updateState(new ChannelUID(getThing().getUID(), BYTESUP), 
				new DecimalType(connectionStatus.getBytes_up()));
		updateState(new ChannelUID(getThing().getUID(), BYTESDOWN), 
				new DecimalType(connectionStatus.getBytes_down()));
	}
	
	private void fetchxDslStatus() throws FreeboxException {
		xDslStatus xdslStatus = fbClient.getConnectionManager().getxDslStatus();
		updateState(new ChannelUID(getThing().getUID(), XDSLSTATUS), 
				new StringType(xdslStatus.getStatus()));
	}
	
	private void fetchWifiConfig() throws FreeboxException {
		WifiGlobalConfig wifiConfiguration = fbClient.getWifiManager().getGlobalConfig();
		updateState(new ChannelUID(getThing().getUID(), WIFISTATUS), 
				wifiConfiguration.getEnabled() ? OnOffType.ON : OnOffType.OFF);
	}
	
	private void fetchLCDConfig() throws FreeboxException {
		LCDConfig lcdConfiguration = fbClient.getLCDManager().getLCDConfig();
		updateState(new ChannelUID(getThing().getUID(), LCDBRIGHTNESS), 
				new DecimalType(lcdConfiguration.getBrightness()));
		updateState(new ChannelUID(getThing().getUID(), LCDORIENTATION), 
				new DecimalType(lcdConfiguration.getOrientation()));
		updateState(new ChannelUID(getThing().getUID(), LCDFORCED), 
				lcdConfiguration.getOrientationForced() ? OnOffType.ON : OnOffType.OFF);
	}
	
	private void fetchSystemConfig() throws FreeboxException {
		SystemConfiguration systemConfiguration = fbClient.getSystemManager().getConfiguration();
		
		updateState(new ChannelUID(getThing().getUID(), FWVERSION),
				new StringType(systemConfiguration.getFirmware_version()));					
		
		long newUptime = systemConfiguration.getUptimeVal();
		updateState(new ChannelUID(getThing().getUID(), RESTARTED), 
				newUptime < uptime ? OnOffType.ON : OnOffType.OFF );
		uptime = newUptime;

		updateState(new ChannelUID(getThing().getUID(), UPTIME), 
				new DecimalType(uptime));
		updateState(new ChannelUID(getThing().getUID(), TEMPCPUM), 
				new DecimalType(systemConfiguration.getTemp_cpum()));
		updateState(new ChannelUID(getThing().getUID(), TEMPCPUB), 
				new DecimalType(systemConfiguration.getTemp_cpub()));
		updateState(new ChannelUID(getThing().getUID(), TEMPSWITCH), 
				new DecimalType(systemConfiguration.getTemp_sw()));
		updateState(new ChannelUID(getThing().getUID(), FANSPEED), 
				new DecimalType(systemConfiguration.getFan_rpm()));
	}
	
	public void setBrightness(Command command) throws FreeboxException {
		if (command != null) {
				if (command instanceof OnOffType || command instanceof IncreaseDecreaseType || 
					command instanceof DecimalType || command instanceof PercentType) {

					LCDConfig lcd = fbClient.getLCDManager().getLCDConfig();
					int value = 0;
					int newValue = 0;
					
					if (command instanceof IncreaseDecreaseType) {
						value = lcd.getBrightness();
						if (command == IncreaseDecreaseType.INCREASE) {							
							newValue = Math.min(100, value + 1);
						} else {
							newValue = Math.max(0, value - 1);
						}
					} else if (command instanceof OnOffType) {
						newValue = (command == OnOffType.ON) ? 100 : 0;
					} else if (command instanceof DecimalType) {
						newValue = Math.min(100, ((DecimalType) command).intValue());
						newValue = Math.max(newValue, 0);
					} else {
						return;
					}
					lcd.setBrightness(newValue);
					fbClient.getLCDManager().setLCDConfig(lcd);
					fetchLCDConfig();
				}				
		}
	}
	
	private void setOrientation(Command command) throws FreeboxException {
		if (command != null && command instanceof DecimalType) {
			LCDConfig lcd = fbClient.getLCDManager().getLCDConfig();
			int newValue = Math.min(360, ((DecimalType) command).intValue());
			newValue = Math.max(newValue, 0);
			lcd.setOrientation(newValue);
			lcd.setOrientationForced(true);
			fbClient.getLCDManager().setLCDConfig(lcd);	
			fetchLCDConfig();
		}	
	}
	
	private void setForced(Command command) throws FreeboxException {
		if (command != null) {
			if (command instanceof OnOffType
					|| command instanceof OpenClosedType
					|| command instanceof UpDownType) {

				LCDConfig lcd = fbClient.getLCDManager().getLCDConfig();
				
				lcd.setOrientationForced( 
						command.equals(OnOffType.ON) || 
						command.equals(UpDownType.UP) || 
						command.equals(OpenClosedType.OPEN)
						);
				fbClient.getLCDManager().setLCDConfig(lcd);	
				fetchLCDConfig();	
			}
		}		
	}
	
	private void setWifiStatus(Command command) throws FreeboxException {
		if (command != null) {
			if (command instanceof OnOffType
					|| command instanceof OpenClosedType
					|| command instanceof UpDownType) {

				WifiGlobalConfig wifiConfiguration = fbClient.getWifiManager().getGlobalConfig();
				
				wifiConfiguration.setEnabled( 
						command.equals(OnOffType.ON) || 
						command.equals(UpDownType.UP) || 
						command.equals(OpenClosedType.OPEN)
						);
				
				fbClient.getWifiManager().setGlobalConfig(wifiConfiguration);
				fetchWifiConfig();	
			}
		}	
	}
	
	private void setReboot(Command command) throws FreeboxException {
		if (command != null) {
			if ( command.equals(OnOffType.ON) || 
				 command.equals(UpDownType.UP) || 
				 command.equals(OpenClosedType.OPEN) ) {
				
				fbClient.getSystemManager().Reboot();
			}	
		}
	}
	
}
