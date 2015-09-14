/*---------------------------------------------------------------------------
 * Copyright (C) 2002 Dallas Semiconductor Corporation, All Rights Reserved.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a
 * copy of this software and associated documentation files (the "Software"),
 * to deal in the Software without restriction, including without limitation
 * the rights to use, copy, modify, merge, publish, distribute, sublicense,
 * and/or sell copies of the Software, and to permit persons to whom the
 * Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included
 * in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS
 * OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY,  FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 * IN NO EVENT SHALL DALLAS SEMICONDUCTOR BE LIABLE FOR ANY CLAIM, DAMAGES
 * OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE,
 * ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR
 * OTHER DEALINGS IN THE SOFTWARE.
 *
 * Except as contained in this notice, the name of Dallas Semiconductor
 * shall not be used except as stated in the Dallas Semiconductor
 * Branding Policy.
 *---------------------------------------------------------------------------
 */
package com.dalsemi.onewire.application.monitor;

import java.util.EventObject;
import java.util.Vector;

import com.dalsemi.onewire.adapter.DSPortAdapter;
import com.dalsemi.onewire.container.OneWireContainer;
import com.dalsemi.onewire.utils.Address;
import com.dalsemi.onewire.utils.OWPath;

/**
 * Represents a group of 1-Wire addresses that have either
 * arrived to or departed from the 1-Wire network.
 *
 * @author SH
 * @version 1.00
 */
public class DeviceMonitorEvent extends EventObject
{
   /** enum for arrival/departure event types */
   public static final int ARRIVAL = 0, DEPARTURE = 1;

   /** The type of event (ARRIVAL|DEPARTURE) */
   protected int eventType = -1;
   /** The monitor which generated the event */
   protected AbstractDeviceMonitor monitor = null;
   /** The DSPortAdapter the monitor was using at the time of event */
   protected DSPortAdapter adapter = null;
   /** Vector of addresses for devices */
   protected Vector vDeviceAddress = null;

   /**
    * Creates a new DeviceMonitor event with the specified characteristics.
    *
    * @param eventType The type of event (ARRIVAL | DEPARTURE)
    * @param source The monitor which generated the event
    * @param adapter The DSPortAdapter the monitor was using
    * @param addresses Vector of addresses for devices
    */
   DeviceMonitorEvent(int eventType, AbstractDeviceMonitor source,
                      DSPortAdapter adapter, Vector addresses)
   {
      super(source);

      if(eventType!=ARRIVAL && eventType!=DEPARTURE)
         throw new IllegalArgumentException("Invalid event type: " + eventType);
      this.eventType = eventType;
      this.monitor = source;
      this.adapter = adapter;
      this.vDeviceAddress = addresses;
   }

   /**
    * Returns the event type (ARRIVAL | DEPARTURE)
    *
    * @return the event type (ARRIVAL | DEPARTURE)
    */
   public int getEventType()
   {
      return this.eventType;
   }

   /**
    * Returns the monitor which generated this event
    *
    * @return the monitor which generated this event
    */
   public AbstractDeviceMonitor getMonitor()
   {
      return this.monitor;
   }

   /**
    * Returns DSPortAdapter the monitor was using when the event was generated
    *
    * @return DSPortAdapter the monitor was using
    */
   public DSPortAdapter getAdapter()
   {
      return this.adapter;
   }

   /**
    * Returns the number of devices associated with this event
    *
    * @return the number of devices associated with this event
    */
   public int getDeviceCount()
   {
      return this.vDeviceAddress.size();
   }

   /**
    * Returns the OneWireContainer for the address at the specified index
    *
    * @return the OneWireContainer for the address at the specified index
    */
   public OneWireContainer getContainerAt(int index)
   {
      Long longAddress = (Long)this.vDeviceAddress.elementAt(index);
      return AbstractDeviceMonitor.getDeviceContainer(adapter, longAddress);
   }

   /**
    * Returns the Path object for the device at the specified index
    *
    * @return the Path object for the device at the specified index
    */
   public OWPath getPathForContainerAt(int index)
   {
      Long longAddress = (Long)this.vDeviceAddress.elementAt(index);
      return this.monitor.getDevicePath(longAddress);
   }

   /**
    * Returns the device address at the specified index as a primitive long.
    *
    * @return the device address at the specified index
    */
   public long getAddressAsLongAt(int index)
   {
      return ((Long)this.vDeviceAddress.elementAt(index)).longValue();
   }

   /**
    * Returns the device address at the specified index as a byte array.
    *
    * @return the device address at the specified index
    */
   public byte[] getAddressAt(int index)
   {
      return Address.toByteArray(getAddressAsLongAt(index));
   }

   /**
    * Returns the device address at the specified index as a String.
    *
    * @return the device address at the specified index
    */
   public String getAddressAsStringAt(int index)
   {
      return Address.toString(getAddressAsLongAt(index));
   }
}