
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
import java.util.Vector;

/**
 * This class provides a default object for the D2A type of a tagged 1-Wire device.
 */
public class D2A
   extends    TaggedDevice
   implements TaggedActuator
{
   /**
    * Creates an object for the device.
    */
   public D2A()
   {
      super();
      ActuatorSelections = new Vector();
   }

   /**
    * Creates an object for the device with the supplied address connected
    * to the supplied port adapter.
    * @param adapter The adapter serving the actuator.
    * @param netAddress The 1-Wire network address of the actuator.
    */
   public D2A(DSPortAdapter adapter, String netAddress)
   {
      super(adapter, netAddress);
      ActuatorSelections = new Vector();
   }

   /**
    * Get the possible selection states of this actuator
    *
    * @return Vector of Strings representing selection states.
    */
   public Vector getSelections()
   {
      return ActuatorSelections;
   }

   /**
    * Set the selection of this actuator
    *
    * @param The selection string.
    *
    * @throws OneWireException
    *
    */
   public void setSelection(String selection)
   throws OneWireException
   {
      PotentiometerContainer pc = (PotentiometerContainer) getDeviceContainer();
      int Index = 0;
      Index = ActuatorSelections.indexOf(selection);
      // must first read the device
      byte[] state = pc.readDevice();
      // set current wiper number from xml tag "channel"
      pc.setCurrentWiperNumber(getChannel(), state);
      // now, write to device to set the wiper number
      pc.writeDevice(state);
      
      if (Index > -1) // means selection is in the vector
      {
         // write wiper position to part
         state = pc.readDevice(); // read it first
         pc.setWiperPosition(Index); // set wiper position in state variable
         pc.writeDevice(state);
      }
   }

   // Selections for the D2A actuator:
   // element 0 ->   Means change to the first wiper position.
   //                
   // element 1 ->   Means change to the second wiper position. 
   //              
   //    .
   //    .
   //    .
   // last element 255? -> Means change to the last wiper position.

   /**
    * Initializes the actuator
    * @param Init The initialization string.
    *
    * @throws OneWireException
    * 
    */
   public void initActuator()
   throws OneWireException
   {
      PotentiometerContainer pc = (PotentiometerContainer) getDeviceContainer();
      int numOfWiperSettings;
      int resistance;
      double offset = 0.6; // this seems about right...
      double wiperResistance;
      String selectionString;
      // initialize the ActuatorSelections Vector
      // must first read the device
      byte[] state = pc.readDevice();
      // set current wiper number from xml tag "channel"
      pc.setCurrentWiperNumber(getChannel(), state);
      // now, write to device to set the wiper number
      pc.writeDevice(state);
      // now, extract some values to initialize the ActuatorSelections
      // get the number of wiper positions
      numOfWiperSettings = pc.numberOfWiperSettings(state);
      // get the resistance value in k-Ohms
      resistance = pc.potentiometerResistance(state);
      // calculate wiper resistance
      wiperResistance = (double) ((double) (resistance - offset) / (double)numOfWiperSettings);
      // add the values to the ActuatorSelections Vector
      selectionString = resistance + " k-Ohms";       // make sure the first
      ActuatorSelections.addElement(selectionString); // element is the entire resistance
      for (int i = (numOfWiperSettings - 2); i > -1; i--)
      {
         double newWiperResistance = (double)(wiperResistance * (double)i);
         // round the values before putting them in drop-down list
         int roundedWiperResistance = (int) ((newWiperResistance + offset) * 10000);
         selectionString = (double) ((double)roundedWiperResistance / 10000.0) + " k-Ohms";
         ActuatorSelections.addElement(selectionString);
      }
   }

   /**
    * Keeps the selections of this actuator
    */
   private Vector ActuatorSelections;
}
  
