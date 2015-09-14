
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
 * This class provides a default object for the Switch type of a tagged 1-Wire device.
 */
public class Switch
   extends    TaggedDevice
   implements TaggedActuator
{
   /**
    * Creates an object for the device.
    */
   public Switch()
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
   public Switch(DSPortAdapter adapter, String netAddress)
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
      SwitchContainer switchcontainer = (SwitchContainer) getDeviceContainer();
      int Index = 0;
      int channelValue = getChannel();
      Index = ActuatorSelections.indexOf(selection);
      boolean switch_state = false;
      
      if (Index > -1) // means selection is in the vector
      {
         // initialize switch-state variable
         if (Index > 0) switch_state = true;
         // write to the device (but, read it first to get state)
         byte[] state = switchcontainer.readDevice();
         // set the switch's state to the value specified
         switchcontainer.setLatchState(channelValue,switch_state,false,state);
         switchcontainer.writeDevice(state);  
      }
   }

   // Selections for the Switch actuator:
   // element 0 -> Means "disconnected" or "open circuit" (init = 0) and is 
   //              associated with the "min" message.
   // element 1 -> Means "connect" or "close the circuit", (init = 1) and is 
   //              associated with the "max" message.

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
      SwitchContainer switchcontainer = (SwitchContainer) getDeviceContainer();
      // initialize the ActuatorSelections Vector
      ActuatorSelections.addElement(getMin());  // for switch, use min and max
      ActuatorSelections.addElement(getMax());
      // Now, initialize the switch to the desired condition.
      // This condition is in the <init> tag and, of course, the  
      // <channel> tag is also needed to know which channel to 
      // to open or close.
      int initValue;
      int channelValue;
      int switchStateIntValue = 0;
      Integer init = new Integer(getInit());
      initValue = init.intValue();
      channelValue = getChannel();

      byte[] state = switchcontainer.readDevice();
      boolean switch_state = switchcontainer.getLatchState(channelValue, state);
      if (switch_state) switchStateIntValue = 1;
      else switchStateIntValue = 0;
      if (initValue != switchStateIntValue)
      {
         // set the switch's state to the value specified in XML file
         switchcontainer.setLatchState(channelValue,!switch_state,false,state);
         switchcontainer.writeDevice(state);  
      }
   }

   /**
    * Keeps the selections of this actuator
    */
   private Vector ActuatorSelections;
}
