package org.openhab.binding.apcupsd.internal.handler;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ApcUpsDataHandler {
	
	private final Logger logger = LoggerFactory.getLogger(ApcUpsDataHandler.class);

	private TcpConnectionHandler connection;
	private Integer refresh;
	
	private NewDataListener newDataListener;
	
	// 0: UpdateOnEveryValue, 1: UpdateOnlyOnChange
	private Map<String, Boolean> interestingData;
	
	private ScheduledExecutorService scheduler;
	private ScheduledFuture<?> getInfoTask;
	
	HashMap<String, String> cache = new HashMap<String, String>();	
	
	public ApcUpsDataHandler(TcpConnectionHandler connection, Integer refresh, ScheduledExecutorService scheduler) {
		this.connection = connection;
		this.refresh = refresh;
		this.scheduler = scheduler;
	}
	
	public void start() {
		logger.debug("Starting APC UPS Data Handler");
		if (getInfoTask != null) {
			getInfoTask.cancel(true);
			getInfoTask = null;
		}
		
		getInfoTask = scheduler.scheduleWithFixedDelay(() -> {
            refreshData();
        }, this.refresh, this.refresh, TimeUnit.SECONDS);
	}
	
	public void stop() {
		logger.debug("Stopping APC UPS Data Handler");
		if (getInfoTask != null) {
			getInfoTask.cancel(true);
			getInfoTask = null;
		}
	}
	
	public void addDatalistener(NewDataListener listener, Map<String, Boolean> interestingData) {
		this.newDataListener = listener;
		this.interestingData = interestingData;
	}
	
	public String getData(String name) {
		if (!interestingData.containsKey(name))
			return null;
		
		if (cache.containsKey(name)) {
			return cache.get(name);
		} else {
			return null;
		}
	}
	
	private void refreshData() {
		if (!this.connection.sendStatusRequest())
			return;
			
		String[] raw_response = this.connection.readStatusRequest();
			
		if (raw_response == null)
			return;
			
		ArrayList<ArrayList<String>> magnitudesAndValues = getMagnitudesAndValues(raw_response);
		updateData(magnitudesAndValues);		
	}
	
	private void updateData(ArrayList<ArrayList<String>> data_collection) {
		for (ArrayList<String> data : data_collection) {
			if (data == null)
				return;

			String magnitude = data.get(0);
			String value = data.get(1);
			
			if (!interestingData.containsKey(magnitude))
				continue;
			
			// Check the data update policy, updateOnEveryChange
			if (interestingData.get(magnitude)) {
				if (cache.containsKey(magnitude)) {
					if (cache.get(magnitude).equals(value)) {
						continue;
					} else {
						cache.put(magnitude, value);
					}
				} else {
					cache.put(magnitude, value);
				}
			}
			
			this.newDataListener.onNewData(magnitude, value);
		}
	}
	
	private ArrayList<ArrayList<String>> getMagnitudesAndValues(String[] lines) {
		ArrayList<ArrayList<String>> result = new ArrayList<ArrayList<String>>();
		for(String line : lines) {
			result.add(getMagnitudeAndValue(line));
		}
		return result;
	}
	
	private ArrayList<String> getMagnitudeAndValue(String line) {
    	String[] line_split = line.split(":", 2);
    	if (line_split.length != 2) {
    		return null;
    	}
    	
    	String name = line_split[0];
    	String value = line_split[1];
    	
    	name = onlyUpperCase(name);
    	value = removeUnits(value);
    	
    	return new ArrayList<>(Arrays.asList(name, value));
    	
    }
	
	private String onlyUpperCase(String name) {
    	StringBuilder buffer = new StringBuilder();
    	for (byte c: name.getBytes()) {
    		if (65 <= c && c <= 90) {
    			buffer.append((char) c);
    		}
    	}
    	return buffer.toString();
    }
    
    private String removeUnits(String value) {
    	String[] units = {"Volts", "Percent", "Minutes", "Seconds", "Watts", "Amps",
    						"Hz", "C", "VA", "Percent Load Capacity"};
    	
    	for (String unit : units) {
    		value = value.replace(unit, "");
    	}
    	return value.trim();
    }
}
