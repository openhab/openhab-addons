
/*---------------------------------------------------------------------------
 * Copyright (C) 1999,2000 Dallas Semiconductor Corporation, All Rights Reserved.
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

package com.dalsemi.onewire.utils;

import java.util.Vector;
import java.util.Enumeration;
import com.dalsemi.onewire.OneWireException;
import com.dalsemi.onewire.utils.OWPathElement;
import com.dalsemi.onewire.container.OneWireContainer;
import com.dalsemi.onewire.container.SwitchContainer;
import com.dalsemi.onewire.adapter.DSPortAdapter;
import com.dalsemi.onewire.adapter.OneWireIOException;


/**
 * 1-Wire&#174 Network path.  Large 1-Wire networks can be sub-divided into branches
 * for load, location, or organizational reasons.  Once 1-Wire devices are placed
 * on this branches there needs to be a mechanism to reach these devices.  The
 * OWPath class was designed to provide a convenient method to open and close
 * 1-Wire paths to reach remote devices.
 *
 * <H3> Usage </H3>
 *
 * <DL>
 * <DD> <H4> Example</H4>
 * Open the path 'path' to the 1-Wire temperature device 'tc' and read the temperature:
 * <PRE> <CODE>
 *  // open a path to the temp device
 *  path.open();
 *
 *  // read the temp device
 *  byte[] state = tc.readDevice();
 *  tc.doTemperatureConvert(state);
 *  state = tc.readDevice();
 *  System.out.println("Temperature of " +
 *           address + " is " +
 *           tc.getTemperature(state) + " C");
 *
 *  // close the path to the device
 *  path.close();
 * </CODE> </PRE>
 * </DL>
 *
 * @see com.dalsemi.onewire.utils.OWPathElement
 * @see com.dalsemi.onewire.container.SwitchContainer
 * @see com.dalsemi.onewire.container.OneWireContainer05
 * @see com.dalsemi.onewire.container.OneWireContainer12
 * @see com.dalsemi.onewire.container.OneWireContainer1F
 *
 * @version    0.00, 12 September 2000
 * @author     DS
 */
public class OWPath
{

   //--------
   //-------- Variables
   //--------

   /** Elements of the path in a Vector */
   private Vector elements;

   /** Adapter where this path is based */
   private DSPortAdapter adapter;

   //--------
   //-------- Constructor
   //--------

   /**
    * Create a new 1-Wire path with no elemements.  Elements
    * can be added by using <CODE> copy </CODE> and/or
    * <CODE> add </CODE>.
    *
    * @param  adapter where the path is based
    *
    * @see #copy(OWPath) copy
    * @see #add(OneWireContainer, int) add
    */
   public OWPath (DSPortAdapter adapter)
   {
      this.adapter = adapter;
      elements     = new Vector(2, 1);
   }

   /**
    * Create a new path with a starting path.  New elements
    * can be added with <CODE>add</CODE>.
    *
    * @param  adapter where the 1-Wire path is based
    * @param  currentPath starting value of this 1-Wire path
    *
    * @see #add(OneWireContainer, int) add
    */
   public OWPath (DSPortAdapter adapter, OWPath currentOWPath)
   {
      this.adapter = adapter;
      elements     = new Vector(2, 1);

      copy(currentOWPath);
   }

   /**
    * Copy the elements from the provided 1-Wire path into this 1-Wire path.
    *
    * @param  currentOWPath path to copy from
    */
   public void copy (OWPath currentOWPath)
   {
      elements.removeAllElements();

      if (currentOWPath != null)
      {

         // enumerature through elements in current path
         for (Enumeration path_enum = currentOWPath.getAllOWPathElements();
                 path_enum.hasMoreElements(); )
         {

            // cast the enum as a OWPathElements and add to vector
            elements.addElement(( OWPathElement ) path_enum.nextElement());
         }
      }
   }

   /**
    * Add a 1-Wire path element to this 1-Wire path.
    *
    * @param owc 1-Wire device switch
    * @param channel of device that represents this 1-Wire path element
    *
    * @see #copy(OWPath) copy
    */
   public void add (OneWireContainer owc, int channel)
   {
      elements.addElement(new OWPathElement(owc, channel));
   }

   /**
    * Compare this 1-Wire path with another.
    *
    * @param compareOWPath 1-Wire path to compare to
    *
    * @return <CODE> true </CODE> if the 1-Wire paths are the same
    */
   public boolean equals (OWPath compareOWPath)
   {
      return (this.toString().equals(compareOWPath.toString()));
   }

   /**
    * Get an enumeration of all of the 1-Wire path elements in
    * this 1-Wire path.
    *
    * @return enumeration of all of the 1-Wire path elements
    *
    * @see com.dalsemi.onewire.utils.OWPathElement
    */
   public Enumeration getAllOWPathElements ()
   {
      return elements.elements();
   }

   /**
    * Get a string representation of this 1-Wire path.
    *
    * @return string 1-Wire path as string
    */
   public String toString ()
   {
      String           st = new String("");
      OWPathElement    element;
      OneWireContainer owc;

      // append 'drive'
      try
      {
         st = adapter.getAdapterName() + "_" + adapter.getPortName()
              + "/";
      }
      catch (OneWireException e)
      {
         st = adapter.getAdapterName() + "/";
      }

      for (int i = 0; i < elements.size(); i++)
      {
         element = ( OWPathElement ) elements.elementAt(i);
         owc     = element.getContainer();

         // append 'directory' name
         st += owc.getAddressAsString() + "_" + element.getChannel()
               + "/";
      }

      return st;
   }

   /**
    * Open this 1-Wire path so that a remote device can be accessed.
    *
    * @throws OneWireIOException on a 1-Wire communication error such as
    *         no device present or a CRC read from the device is incorrect.  This could be
    *         caused by a physical interruption in the 1-Wire Network due to
    *         shorts or a newly arriving 1-Wire device issuing a 'presence pulse'.
    * @throws OneWireException on a communication or setup error with the 1-Wire
    *         adapter.
    */
   public void open ()
      throws OneWireException, OneWireIOException
   {
      OWPathElement   path_element;
      SwitchContainer sw;
      byte[]          sw_state;

      // enumerature through elements in path
      for (int i = 0; i < elements.size(); i++)
      {

         // cast the enum as a OWPathElement
         path_element = ( OWPathElement ) elements.elementAt(i);

         // get the switch
         sw = ( SwitchContainer ) path_element.getContainer();

         // turn on the elements channel
         sw_state = sw.readDevice();

         sw.setLatchState(path_element.getChannel(), true, sw.hasSmartOn(),
                          sw_state);
         sw.writeDevice(sw_state);
      }

      // check if not depth in path, do a reset so a resetless search will work
      if (elements.size() == 0)
      {
         adapter.reset();
      }
   }

   /**
    * Close each element in this 1-Wire path in reverse order.
    *
    * @throws OneWireIOException on a 1-Wire communication error such as
    *         no device present or a CRC read from the device is incorrect.  This could be
    *         caused by a physical interruption in the 1-Wire Network due to
    *         shorts or a newly arriving 1-Wire device issuing a 'presence pulse'.
    * @throws OneWireException on a communication or setup error with the 1-Wire
    *         adapter.
    */
   public void close ()
      throws OneWireException, OneWireIOException
   {
      OWPathElement   path_element;
      SwitchContainer sw;
      byte[]          sw_state;

      // loop through elements in path in reverse order
      for (int i = elements.size() - 1; i >= 0; i--)
      {

         // cast the element as a OWPathElement
         path_element = ( OWPathElement ) elements.elementAt(i);

         // get the switch
         sw = ( SwitchContainer ) path_element.getContainer();

         // turn off the elements channel
         sw_state = sw.readDevice();
         sw.setLatchState(path_element.getChannel(), false, false, sw_state);
         sw.writeDevice(sw_state);
      }
   }
}
