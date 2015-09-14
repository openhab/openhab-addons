
/*---------------------------------------------------------------------------
 * Copyright (C) 1999-2001 Dallas Semiconductor Corporation, All Rights Reserved.
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

package com.dalsemi.onewire.application.tag;

import com.dalsemi.onewire.adapter.DSPortAdapter;
import com.dalsemi.onewire.container.*;
import com.dalsemi.onewire.OneWireException;


/**
 * This class provides a default object for the Humidity type 
 * of a tagged 1-Wire device.  
 */
public class Humidity
   extends    TaggedDevice
   implements TaggedSensor
{

   /**
    * Creates an object for the device.
    */
   public Humidity()
   {
      super();
   }

   /**
    * Creates an object for the device with the supplied address and device type connected
    * to the supplied port adapter.
    * @param adapter The adapter serving the sensor.
    * @param netAddress The 1-Wire network address of the sensor.
    */
   public Humidity(DSPortAdapter adapter, String netAddress)
   {
      super(adapter, netAddress);
   }

   /**
    * The readSensor method returns a relative humidity reading
    * in %RH 
    *
    * @param--none.
    *
    * @return String humidity in %RH
    */
   public String readSensor()
      throws OneWireException
   {
      HumidityContainer hc = (HumidityContainer) DeviceContainer;

      // read the device first to get the state
      byte[] state = hc.readDevice();

      // convert humidity
      hc.doHumidityConvert(state);

      // construct the return string
      String return_string = (int) roundDouble(hc.getHumidity(state)) + "%";
      if (hc.isRelative())
         return_string += "RH";

      return return_string;
   }
   /**
    * The roundDouble method returns a double rounded to the 
    * nearest digit in the "ones" position. 
    *
    * @param--double
    *
    * @return double rounded to the nearest digit in the "ones"
    * position.
    */
   private double roundDouble (double d)
   {
      return (double)((int)(d+((d > 0)? 0.5 : -0.5)));
   }
}
