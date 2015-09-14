
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

/**
 * 1-Wire Network State contains the current 1-Wire Network state information
 *
 * @version    0.00, 28 Aug 2000
 * @author     DS
 */
class OneWireState
{

   //--------
   //-------- Variables
   //--------

   /**
    * This is the current logical speed that the 1-Wire Network is operating at. <p>
    * The valid values for this are:
    * <ul>
    * <li> SPEED_REGULAR
    * <li> SPEED_FLEX
    * <li> SPEED_OVERDRIVE
    * <li> SPEED_HYPERDRIVE
    * </ul>
    */
   public char oneWireSpeed;

   /**
    * This is the current logical 1-Wire Network pullup level.<p>
    * The valid values are:
    * <ul>
    * <li> LEVEL_NORMAL
    * <li> LEVEL_POWER_DELIVERY
    * <li> LEVEL_BREAK
    * <li> LEVEL_PROGRAM
    * </ul>
    */
   public char oneWireLevel;

   /**
    * True if programming voltage is available
    */
   public boolean canProgram;

   /**
    * True if a level change is primed to occur on the next bit
    * of communication.
    */
   public boolean levelChangeOnNextBit;

   /**
    * True if a level change is primed to occur on the next byte
    * of communication.
    */
   public boolean levelChangeOnNextByte;

   /**
    * The new level value that is primed to change on the next bit
    * or byte depending on the flags, levelChangeOnNextBit and
    * levelChangeOnNextByte. <p>
    * The valid values are:
    * <ul>
    * <li> LEVEL_NORMAL
    * <li> LEVEL_STRONGPULLUP
    * <li> LEVEL_BREAK
    * <li> LEVEL_PROGRAM
    * </ul>
    */
   public char primedLevelValue;

   /**
    * The amount of time that the 'level' value will be on for. <p>
    * The valid values are:
    * <ul>
    * <li>   0 (DELIVERY_HALF_SECOND) provide power for 1/2 second.
    * <li>   1 (DELIVERY_ONE_SECOND) provide power for 1 second.
    * <li>   2 (DELIVERY_TWO_SECONDS) provide power for 2 seconds.
    * <li>   3 (DELIVERY_FOUR_SECONDS) provide power for 4 seconds.
    * <li>   4 (DELIVERY_SMART_DONE) provide power until the
    *          the device is no longer drawing significant power.
    * <li>   5 (DELIVERY_INFINITE) provide power until the
    *          setBusNormal() method is called.
    * </ul>
    */
   public int levelTimeFactor;

   /**
    * Value of the last discrepancy during the last search for an iButton.
    */
   public int searchLastDiscrepancy;

   /**
    * Value of the last discrepancy in the family code during the last
    * search for an iButton.
    */
   public int searchFamilyLastDiscrepancy;

   /**
    * Flag to indicate that the last device found is the last device in a
    * search sequence on the 1-Wire Network.
    */
   public boolean searchLastDevice;

   /**
    * ID number of the current iButton found.
    */
   public byte[] ID;

   /**
    * Array of iButton families to include in any search.
    */
   public byte[] searchIncludeFamilies;

   /**
    * Array of iButton families to exclude in any search.
    */
   public byte[] searchExcludeFamilies;

   /**
    * Flag to indicate the conditional search is to be performed so that
    * only iButtons in an alarm state will be found.
    */
   public boolean searchOnlyAlarmingButtons;

   /**
    * Flag to indicate next search will not be preceeded by a 1-Wire reset
    */
   public boolean skipResetOnSearch;

   //--------
   //-------- Constructors
   //--------

   /**
    * Construct the initial state of the 1-Wire Network.
    */
   public OneWireState ()
   {

      // speed, level
      oneWireSpeed = DSPortAdapter.SPEED_REGULAR;
      oneWireLevel = DSPortAdapter.LEVEL_NORMAL;

      // level primed
      levelChangeOnNextBit  = false;
      levelChangeOnNextByte = false;
      primedLevelValue      = DSPortAdapter.LEVEL_NORMAL;
      levelTimeFactor       = DSPortAdapter.DELIVERY_INFINITE;

      // adapter abilities
      canProgram = false;

      // search options 
      searchIncludeFamilies     = new byte [0];
      searchExcludeFamilies     = new byte [0];
      searchOnlyAlarmingButtons = false;
      skipResetOnSearch         = false;

      // new iButton object
      ID = new byte [8];

      // search state
      searchLastDiscrepancy       = 0;
      searchFamilyLastDiscrepancy = 0;
      searchLastDevice            = false;
   }
}
