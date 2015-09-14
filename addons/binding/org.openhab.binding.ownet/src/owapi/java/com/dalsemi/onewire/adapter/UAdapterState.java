
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

package com.dalsemi.onewire.adapter;

// imports
import com.dalsemi.onewire.adapter.UParameterSettings;
import com.dalsemi.onewire.adapter.OneWireState;


/** UAdapterState contains the communication state of the DS2480
 *  based COM port adapter.
 *  //\\//\\ This class is very preliminary and not all
 *           functionality is complete or debugged.  This
 *           class is subject to change.                  //\\//\\
 *
 *  @version    0.00, 28 Aug 2000
 *  @author     DS
 */
class UAdapterState
{

   //--------
   //-------- Finals
   //--------
   //------- DS9097U brick baud rates expressed for the DS2480 ichip  

   /** DS9097U brick baud rate expressed for the DS2480 ichip, 9600 baud   */
   public static final char BAUD_9600 = 0x00;

   /** DS9097U brick baud rate expressed for the DS2480 ichip, 19200 baud  */
   public static final char BAUD_19200 = 0x02;

   /** DS9097U brick baud rate expressed for the DS2480 ichip, 57600 baud  */
   public static final char BAUD_57600 = 0x04;

   /** DS9097U brick baud rate expressed for the DS2480 ichip, 115200 baud */
   public static final char BAUD_115200 = 0x06;

   //------- DS9097U speed modes

   /** DS9097U speed mode, regular speed                         */
   public static final char USPEED_REGULAR = 0x00;

   /** DS9097U speed mode, flexible speed for long lines         */
   public static final char USPEED_FLEX = 0x04;

   /** DS9097U speed mode, overdrive speed                       */
   public static final char USPEED_OVERDRIVE = 0x08;

   /** DS9097U speed mode, pulse, for program and power delivery */
   public static final char USPEED_PULSE = 0x0C;

   //------- DS9097U modes

   /** DS9097U data mode                                  */
   public static final char MODE_DATA = 0x00E1;

   /** DS9097U command mode                               */
   public static final char MODE_COMMAND = 0x00E3;

   /** DS9097U pulse mode                                 */
   public static final char MODE_STOP_PULSE = 0x00F1;

   /** DS9097U special mode (in revision 1 silicon only)  */
   public static final char MODE_SPECIAL = 0x00F3;

   //------- DS9097U chip revisions and state

   /** DS9097U chip revision 1  */
   public static final char CHIP_VERSION1 = 0x04;

   /** DS9097U chip revision mask  */
   public static final char CHIP_VERSION_MASK = 0x1C;

   /** DS9097U program voltage available mask  */
   public static final char PROGRAM_VOLTAGE_MASK = 0x20;

   /** DS9097U program voltage available mask  */
   public static final int MAX_ALARM_COUNT = 3000;

   //--------
   //-------- Variables
   //--------

   /**
    * Parameter settings for the three logical modes
    */
   public UParameterSettings uParameters [];

   /**
    * The OneWire State object reference
    */
   public OneWireState oneWireState;

   /**
    * Flag true if can stream bits
    */
   public boolean streamBits;

   /**
    * Flag true if can stream bytes
    */
   public boolean streamBytes;

   /**
    * Flag true if can stream search
    */
   public boolean streamSearches;

   /**
    * Flag true if can stream resets
    */
   public boolean streamResets;

   /**
    * Current baud rate that we are communicating with the DS9097U
    * expressed for the DS2480 ichip. <p>
    * Valid values can be:
    *  <ul>
    *  <li> BAUD_9600
    *  <li> BAUD_19200
    *  <li> BAUD_57600
    *  <li> BAUD_115200
    *  </ul>
    */
   public char ubaud;

   /**
    * This is the current 'real' speed that the OneWire is operating at.
    * This is used to represent the actual mode that the DS2480 is operting
    * in.  For example the logical speed might be USPEED_REGULAR but for
    * RF emission reasons we may put the actual DS2480 in SPEED_FLEX. <p>
    * The valid values for this are:
    *  <ul>
    *  <li> USPEED_REGULAR
    *  <li> USPEED_FLEX
    *  <li> USPEED_OVERDRIVE
    *  <li> USPEED_PULSE
    *  </ul>
    */
   public char uSpeedMode;

   /**
    * This is the current state of the DS2480 adapter on program
    * voltage availablity.  'true' if available.
    */
   public boolean programVoltageAvailable;

   /**
    * True when DS2480 is currently in command mode.  False when it is in
    * data mode.
    */
   public boolean inCommandMode;

   /**
    * The DS2480 revision number.  The current value values are 1 and 2.
    */
   public char revision;

   /**
    * Flag to indicate need to search for long alarm check
    */
   protected boolean longAlarmCheck;

   /**
    * Count of how many resets have been seen without Alarms
    */
   protected int lastAlarmCount;

   //--------
   //-------- Constructors
   //--------

   /**
    * Construct the state of the U brick with the defaults
    */
   public UAdapterState (OneWireState newOneWireState)
   {

      // get a pointer to the OneWire state object
      oneWireState = newOneWireState;

      // set the defaults
      ubaud                   = BAUD_9600;
      uSpeedMode              = USPEED_FLEX;
      revision                = 0;
      inCommandMode           = true;
      streamBits              = true;
      streamBytes             = true;
      streamSearches          = true;
      streamResets            = false;
      programVoltageAvailable = false;
      longAlarmCheck          = false;
      lastAlarmCount          = 0;

      // create the three speed logical parameter settings
      uParameters     = new UParameterSettings [4];
      uParameters [0] = new UParameterSettings();
      uParameters [1] = new UParameterSettings();
      uParameters [2] = new UParameterSettings();
      uParameters [3] = new UParameterSettings();

      // adjust flex time 
      uParameters [DSPortAdapter.SPEED_FLEX].pullDownSlewRate =
         UParameterSettings.SLEWRATE_0p83Vus;
      uParameters [DSPortAdapter.SPEED_FLEX].write1LowTime    =
         UParameterSettings.WRITE1TIME_12us;
      uParameters [DSPortAdapter.SPEED_FLEX].sampleOffsetTime =
         UParameterSettings.SAMPLEOFFSET_TIME_10us;
   }
}
