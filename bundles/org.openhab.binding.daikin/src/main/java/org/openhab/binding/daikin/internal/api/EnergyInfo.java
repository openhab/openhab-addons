/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.openhab.binding.daikin.internal.api;

import java.util.Arrays;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.time.DayOfWeek;
import java.time.LocalDateTime;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Holds information from the get_day_power_ex call.
 *
 * @author Wouter Denayer <wouter@denayer.com> - Added to support energy reading reading
 *
 */
public class EnergyInfo {
    public Optional<Double> energyHeatingToday = Optional.empty();
    public Optional<Double> energyHeatingThisWeek = Optional.empty();
    public Optional<Double> energyHeatingLastWeek = Optional.empty();
    public Optional<Double> energyCoolingToday = Optional.empty();
    public Optional<Double> energyCoolingThisWeek = Optional.empty();
    public Optional<Double> energyCoolingLastWeek = Optional.empty();  
    
    private static final Logger logger = LoggerFactory.getLogger(EnergyInfo.class);

    private EnergyInfo() {
    }

    public static EnergyInfo parse(String response) {

        EnergyInfo info = new EnergyInfo();

        logger.debug("parsing energy {}", response);

        // /aircon/get_week_power_ex
        // ret=OK,s_dayw=0,week_heat=1/1/1/1/1/5/2/1/1/1/1/2/1/1,week_cool=0/0/0/0/0/0/0/0/0/0/0/0/0/0
        // week_heat=<today>/<today-1>/<today-2>/<today-3>/...
        Map<String, String> responseMap = Arrays.asList(response.split(",")).stream().filter(kv -> kv.contains("="))
                .map(kv -> {
                    String[] keyValue = kv.split("=");
                    String key = keyValue[0];
                    String value = keyValue.length > 1 ? keyValue[1] : "";
                    return new String[] { key, value };
                }).collect(Collectors.toMap(x -> x[0], x -> x[1]));
        
        if(responseMap.get("ret") != null && responseMap.get("ret").contentEquals("OK")) {
            int dayOfWeek = Integer.parseInt(responseMap.get("s_dayw"));
            
            // get the heating info
            String[] heatingValues = responseMap.get("week_heat").split("/");
            info.energyHeatingToday = Optional.of(Double.parseDouble(heatingValues[0])/10);
            double thisWeekEnergy = 0;         
            int thisWeekLastDay = ((dayOfWeek+7) % 8);           
            for (int i=0; i < thisWeekLastDay; i+=1) {
                thisWeekEnergy += Integer.parseInt(heatingValues[i]);
            }
            double previousWeekEnergy = 0;
            for (int i=thisWeekLastDay; i < thisWeekLastDay+7; i+=1) {               
                previousWeekEnergy += Integer.parseInt(heatingValues[i]);          
            }            
            info.energyHeatingThisWeek = Optional.of(thisWeekEnergy/10);
            info.energyHeatingLastWeek = Optional.of(previousWeekEnergy/10);            
            
            // get the cooling info
            String[] coolingValues = responseMap.get("week_cool").split("/");
            info.energyCoolingToday = Optional.of(Double.parseDouble(coolingValues[0])/10);
            thisWeekEnergy = 0;
            for (int i=0; i < thisWeekLastDay; i+=1) {
                thisWeekEnergy += Integer.parseInt(coolingValues[i]);
            }
            previousWeekEnergy = 0;
            for (int i=thisWeekLastDay; i < thisWeekLastDay+7; i+=1) {
                previousWeekEnergy += Integer.parseInt(coolingValues[i]);          
            }            
            info.energyCoolingThisWeek = Optional.of(thisWeekEnergy/10);
            info.energyCoolingLastWeek = Optional.of(previousWeekEnergy/10);            
        } else {
            logger.debug("did not receive 'ret=OK' from adapter");
        }
        return info;
    }
}
