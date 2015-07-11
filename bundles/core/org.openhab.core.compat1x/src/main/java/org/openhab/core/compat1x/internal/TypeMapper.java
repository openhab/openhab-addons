/**
 * Copyright (c) 2014-2015 openHAB UG (haftungsbeschraenkt) and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.core.compat1x.internal;

import org.eclipse.smarthome.core.library.types.DateTimeType;
import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.HSBType;
import org.eclipse.smarthome.core.library.types.IncreaseDecreaseType;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.library.types.OpenClosedType;
import org.eclipse.smarthome.core.library.types.PercentType;
import org.eclipse.smarthome.core.library.types.PointType;
import org.eclipse.smarthome.core.library.types.StopMoveType;
import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.library.types.UpDownType;
import org.eclipse.smarthome.core.types.Type;
import org.eclipse.smarthome.core.types.UnDefType;

public class TypeMapper {

	public static org.openhab.core.types.Type mapToOpenHABType(Type type) {
		if (type==null) {
		    return null;
		}
	    
	    org.openhab.core.types.Type result = org.openhab.core.types.UnDefType.UNDEF;
		Class<? extends Type> typeClass = type.getClass();

        if (type==UnDefType.NULL) {
            result = org.openhab.core.types.UnDefType.NULL;
        } else if (type==UnDefType.UNDEF) {
            result = org.openhab.core.types.UnDefType.UNDEF;
        } else if (type==OnOffType.ON) {
            result = org.openhab.core.library.types.OnOffType.ON;
        } else if (type==OnOffType.OFF) {
            result = org.openhab.core.library.types.OnOffType.OFF;
        } else if (type==OpenClosedType.OPEN) {
            result = org.openhab.core.library.types.OpenClosedType.OPEN;
        } else if (type==OpenClosedType.CLOSED) {
            result = org.openhab.core.library.types.OpenClosedType.CLOSED;
        } else if (type==IncreaseDecreaseType.INCREASE) {
            result = org.openhab.core.library.types.IncreaseDecreaseType.INCREASE;
        } else if (type==IncreaseDecreaseType.DECREASE) {
            result = org.openhab.core.library.types.IncreaseDecreaseType.DECREASE;
        } else if (type==StopMoveType.MOVE) {
            result = org.openhab.core.library.types.StopMoveType.MOVE;
        } else if (type==StopMoveType.STOP) {
            result = org.openhab.core.library.types.StopMoveType.STOP;
        } else if (type==UpDownType.UP) {
            result = org.openhab.core.library.types.UpDownType.UP;
        } else if (type==UpDownType.DOWN) {
            result = org.openhab.core.library.types.UpDownType.DOWN;
        } else if (typeClass.equals(StringType.class)) {
		    result = new org.openhab.core.library.types.StringType(type.toString());
		} else if (typeClass.equals(DecimalType.class)) {
            result = new org.openhab.core.library.types.DecimalType(type.toString());
        } else if (typeClass.equals(HSBType.class)) {
            result = new org.openhab.core.library.types.HSBType(type.toString());
        } else if (typeClass.equals(PercentType.class)) {
            result = new org.openhab.core.library.types.PercentType(type.toString());
        } else if (typeClass.equals(DateTimeType.class)) {
            result = new org.openhab.core.library.types.DateTimeType(type.toString());
        } else if (typeClass.equals(PointType.class)) {
            result = new org.openhab.core.library.types.PointType(type.toString());
		}
		
		return result;
	}

	   public static Type mapToESHType(org.openhab.core.types.Type type) {
	        if (type==null) {
	            return null;
	        }
	        
	        Type result = UnDefType.UNDEF;
	        Class<? extends org.openhab.core.types.Type> typeClass = type.getClass();

	        if (type==org.openhab.core.types.UnDefType.NULL) {
	            result = UnDefType.NULL;
	        } else if (type==org.openhab.core.types.UnDefType.UNDEF) {
	            result = UnDefType.UNDEF;
	        } else if (type==org.openhab.core.library.types.OnOffType.ON) {
	            result = OnOffType.ON;
	        } else if (type==org.openhab.core.library.types.OnOffType.OFF) {
	            result = OnOffType.OFF;
	        } else if (type==org.openhab.core.library.types.OpenClosedType.OPEN) {
	            result = OpenClosedType.OPEN;
	        } else if (type==org.openhab.core.library.types.OpenClosedType.CLOSED) {
	            result = OpenClosedType.CLOSED;
	        } else if (type==org.openhab.core.library.types.IncreaseDecreaseType.INCREASE) {
	            result = IncreaseDecreaseType.INCREASE;
	        } else if (type==org.openhab.core.library.types.IncreaseDecreaseType.DECREASE) {
	            result = IncreaseDecreaseType.DECREASE;
	        } else if (type==org.openhab.core.library.types.StopMoveType.MOVE) {
	            result = StopMoveType.MOVE;
	        } else if (type==org.openhab.core.library.types.StopMoveType.STOP) {
	            result = StopMoveType.STOP;
	        } else if (type==org.openhab.core.library.types.UpDownType.UP) {
	            result = UpDownType.UP;
	        } else if (type==org.openhab.core.library.types.UpDownType.DOWN) {
	            result = UpDownType.DOWN;
	        } else if (typeClass.equals(org.openhab.core.library.types.StringType.class)) {
	            result = new StringType(type.toString());
	        } else if (typeClass.equals(org.openhab.core.library.types.DecimalType.class)) {
	            result = new DecimalType(type.toString());
	        } else if (typeClass.equals(org.openhab.core.library.types.HSBType.class)) {
	            result = new HSBType(type.toString());
	        } else if (typeClass.equals(org.openhab.core.library.types.PercentType.class)) {
	            result = new PercentType(type.toString());
	        } else if (typeClass.equals(org.openhab.core.library.types.DateTimeType.class)) {
	            result = new DateTimeType(type.toString());
            } else if (typeClass.equals(org.openhab.core.library.types.PointType.class)) {
                result = new PointType(type.toString());
            } else if (typeClass.equals(org.openhab.library.tel.types.CallType.class)) {
                result = new org.openhab.library.tel.types.ESHCallType(type.toString());
	        }
	        
	        return result;
	   }

}
