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

import java.util.Hashtable;
import java.util.Enumeration;
import java.util.Vector;

import com.dalsemi.onewire.utils.OWPath;
import com.dalsemi.onewire.adapter.DSPortAdapter;
import com.dalsemi.onewire.OneWireException;
import com.dalsemi.onewire.adapter.OneWireIOException;
import com.dalsemi.onewire.container.OneWireContainer;
import com.dalsemi.onewire.container.SwitchContainer;

/**
 * Class NetworkDeviceMonitor represents the monitor that searches the
 * 1-Wire net, including the traversal of branches, looing for new arrivals
 * and departures.
 *
 * @author SH
 * @version 1.00
 */
public class NetworkDeviceMonitor
   extends AbstractDeviceMonitor
{
   /** hashtable for holding the OWPath objects for each device container. */
   protected final Hashtable devicePathHash = new Hashtable();
   /** A vector of paths, or branches, to search */
   protected Vector paths = null;
   /** indicates whether or not branches are automatically traversed */
   protected boolean branchAutoSearching = true;


   /**
    * Create a complex monitor that does search branches
    *
    * @param the DSPortAdapter this monitor should search
    */
   public NetworkDeviceMonitor(DSPortAdapter adapter)
   {
      setAdapter(adapter);
    }

   /**
    * Sets this monitor to search a new DSPortAdapter
    *
    * @param the DSPortAdapter this monitor should search
    */
   public void setAdapter(DSPortAdapter adapter)
   {
      if(adapter==null)
         throw new IllegalArgumentException("Adapter cannot be null");

      synchronized(sync_flag)
      {
         this.adapter = adapter;

         if(this.paths==null)
            this.paths = new Vector();
         else
            this.paths.setSize(0);
         this.paths.addElement(new OWPath(adapter));

         resetSearch();
      }
   }

   /**
    * Indicates whether or not branches are automatically traversed.  If false,
    * new branches must be indicated using the "addBranch" method.
    *
    * @param enabled if true, all branches are automatically traversed during a
    * search operation.
    */
   public void setBranchAutoSearching(boolean enabled)
   {
      this.branchAutoSearching = enabled;
   }

   /**
    * Indicates whether or not branches are automatically traversed.  If false,
    * new branches must be indicated using the "addBranch" method.
    *
    * @returns true if all branches are automatically traversed during a
    * search operation.
    */
   public boolean getBranchAutoSearching()
   {
      return this.branchAutoSearching;
   }

   /**
    * Adds a branch for searching.  Must be used to traverse branches if
    * auto-searching is disabled.
    *
    * @param path A branch to be searched during the next search routine
    */
   public void addBranch(OWPath path)
   {
      paths.addElement(path);
   }

   /**
    * Returns the OWPath of the device with the given address.
    *
    * @param address a Long object representing the address of the device
    * @return The OWPath representing the network path to the device.
    */
   public OWPath getDevicePath(Long address)
   {
      synchronized(devicePathHash)
      {
         return (OWPath)devicePathHash.get(address);
      }
   }

   /**
    * The device monitor will internally cache OWPath objects for each
    * 1-Wire device.  Use this method to clean up all stale OWPath objects.
    * A stale path object is a OWPath which references a branching path to a
    * 1-Wire device address which has not been seen by a recent search.
    * This will be essential in a touch-contact environment which could run
    * for some time and needs to conserve memory.
    */
   public void cleanUpStalePathReferences()
   {
      synchronized(devicePathHash)
      {
         Enumeration e = devicePathHash.keys();
         while(e.hasMoreElements())
         {
            Object o = e.nextElement();
            if(!deviceAddressHash.containsKey(o))
               devicePathHash.remove(o);
         }
      }
   }

   /**
    * Performs a search of the 1-Wire network, with branch searching
    *
    * @param arrivals A vector of Long objects, represent new arrival addresses.
    * @param departures A vector of Long objects, represent departed addresses.
    */
   public void search(Vector arrivals, Vector departures)
      throws OneWireException, OneWireIOException
   {
      synchronized(sync_flag)
      {
         try
         {
            // aquire the adapter
            adapter.beginExclusive(true);

            // setup the search
            adapter.setSearchAllDevices();
            adapter.targetAllFamilies();
            adapter.setSpeed(DSPortAdapter.SPEED_REGULAR);

            // close any opened branches
            for(int j=0; j<paths.size(); j++)
            {
               try
               {
                  ((OWPath)paths.elementAt(j)).close();
               }
               catch(Exception e){;}
            }

            // search through all of the paths
            for(int i=0; i<paths.size(); i++)
            {
               // set searches to not use reset
               adapter.setNoResetSearch();

               // find the first device on this branch
               boolean search_result = false;
               OWPath path = (OWPath)paths.elementAt(i);
               try
               {
                  // try to open the current path
                  path.open();
               }
               catch(Exception e)
               {
                  // if opening the path failed, continue on to the next path
                  continue;
               }

               search_result = adapter.findFirstDevice();

               // loop while devices found
               while (search_result)
               {
                  // get the 1-Wire address
                  Long longAddress = new Long(adapter.getAddressAsLong());
                  // check if the device allready exists in our hashtable
                  if(!deviceAddressHash.containsKey(longAddress))
                  {
                     OneWireContainer owc = getDeviceContainer(adapter,
                                                               longAddress);
                     // check to see if it's a switch and if we are supposed
                     // to automatically search down branches
                     if(this.branchAutoSearching
                            && (owc instanceof SwitchContainer) )
                     {
                        SwitchContainer sc = (SwitchContainer)owc;
                        byte[] state = sc.readDevice();
                        for(int j=0; j<sc.getNumberChannels(state); j++)
                        {
                           OWPath tmp = new OWPath(adapter, path);
                           tmp.add(owc, j);
                           if(!paths.contains(tmp))
                              paths.addElement(tmp);
                        }
                     }

                     synchronized(devicePathHash)
                     {
                        devicePathHash.put(longAddress, path);
                     }
                     if(arrivals!=null)
                        arrivals.addElement(longAddress);
                  }
                  // check if the existing device moved
                  else if (!path.equals((OWPath)devicePathHash.get(longAddress)))
                  {
                     synchronized(devicePathHash)
                     {
                        devicePathHash.put(longAddress, path);
                     }
                     if(departures!=null)
                        departures.addElement(longAddress);
                     if(arrivals!=null)
                        arrivals.addElement(longAddress);
                  }

                  // update count
                  deviceAddressHash.put(longAddress, new Integer(max_state_count));

                  // find the next device on this branch
                  path.open();
                  search_result = adapter.findNextDevice();
               }
            }
         }
         finally
         {
            adapter.endExclusive();
         }

         // remove any devices that have not been seen
         for (Enumeration device_enum = deviceAddressHash.keys();
                 device_enum.hasMoreElements(); )
         {
            Long longAddress = (Long)device_enum.nextElement();

            // check for removal by looking at state counter
            int cnt = ((Integer)deviceAddressHash.get(longAddress)).intValue();
            if (cnt <= 0)
            {
               // device entry is stale, should be removed
               deviceAddressHash.remove(longAddress);
               if(departures!=null)
                  departures.addElement(longAddress);
            }
            else
            {
               // device entry isn't stale, it stays
               deviceAddressHash.put(longAddress, new Integer(cnt-1));
            }
         }

         // fire notification events
         if(departures!=null && departures.size()>0)
            fireDepartureEvent(adapter, departures);
         if(arrivals!=null && arrivals.size()>0)
            fireArrivalEvent(adapter, arrivals);
      }
   }

}
