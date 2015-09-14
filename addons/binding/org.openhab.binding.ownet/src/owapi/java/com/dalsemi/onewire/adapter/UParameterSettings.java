
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

/** UParameterSettings contains the parameter settings state for one
 *  speed on the DS2480 based iButton COM port adapter.
 *
 *  @version    0.00, 28 Aug 2000
 *  @author     DS
 */
class UParameterSettings
{

   //--------
   //-------- Finals
   //--------
   // Parameter selection

   /** Parameter selection, pull-down slew rate            */
   public static final char PARAMETER_SLEW = 0x10;

   /** Parameter selection, 12 volt pulse time             */
   public static final char PARAMETER_12VPULSE = 0x20;

   /** Parameter selection, 5 volt pulse time              */
   public static final char PARAMETER_5VPULSE = 0x30;

   /** Parameter selection, write 1 low time               */
   public static final char PARAMETER_WRITE1LOW = 0x40;

   /** Parameter selection, sample offset                  */
   public static final char PARAMETER_SAMPLEOFFSET = 0x50;

   /** Parameter selection, baud rate                      */
   public static final char PARAMETER_BAUDRATE = 0x70;

   // Pull down slew rate times 

   /** Pull down slew rate, 15V/us                    */
   public static final char SLEWRATE_15Vus = 0x00;

   /** Pull down slew rate, 2.2V/us                   */
   public static final char SLEWRATE_2p2Vus = 0x02;

   /** Pull down slew rate, 1.65V/us                  */
   public static final char SLEWRATE_1p65Vus = 0x04;

   /** Pull down slew rate, 1.37V/us                  */
   public static final char SLEWRATE_1p37Vus = 0x06;

   /** Pull down slew rate, 1.1V/us                   */
   public static final char SLEWRATE_1p1Vus = 0x08;

   /** Pull down slew rate, 0.83V/us                  */
   public static final char SLEWRATE_0p83Vus = 0x0A;

   /** Pull down slew rate, 0.7V/us                   */
   public static final char SLEWRATE_0p7Vus = 0x0C;

   /** Pull down slew rate, 0.55V/us                  */
   public static final char SLEWRATE_0p55Vus = 0x0E;

   // 12 Volt programming pulse times 

   /** 12 Volt programming pulse, time 32us           */
   public static final char TIME12V_32us = 0x00;

   /** 12 Volt programming pulse, time 64us           */
   public static final char TIME12V_64us = 0x02;

   /** 12 Volt programming pulse, time 128us          */
   public static final char TIME12V_128us = 0x04;

   /** 12 Volt programming pulse, time 256us          */
   public static final char TIME12V_256us = 0x06;

   /** 12 Volt programming pulse, time 512us          */
   public static final char TIME12V_512us = 0x08;

   /** 12 Volt programming pulse, time 1024us         */
   public static final char TIME12V_1024us = 0x0A;

   /** 12 Volt programming pulse, time 2048us         */
   public static final char TIME12V_2048us = 0x0C;

   /** 12 Volt programming pulse, time (infinite)     */
   public static final char TIME12V_infinite = 0x0E;

   // 5 Volt programming pulse times 

   /** 5 Volt programming pulse, time 16.4ms        */
   public static final char TIME5V_16p4ms = 0x00;

   /** 5 Volt programming pulse, time 65.5ms        */
   public static final char TIME5V_65p5ms = 0x02;

   /** 5 Volt programming pulse, time 131ms         */
   public static final char TIME5V_131ms = 0x04;

   /** 5 Volt programming pulse, time 262ms         */
   public static final char TIME5V_262ms = 0x06;

   /** 5 Volt programming pulse, time 524ms         */
   public static final char TIME5V_524ms = 0x08;

   /** 5 Volt programming pulse, time 1.05s         */
   public static final char TIME5V_1p05s = 0x0A;

   /** 5 Volt programming pulse, time 2.10sms       */
   public static final char TIME5V_2p10s = 0x0C;

   /** 5 Volt programming pulse, dynamic current detect       */
   public static final char TIME5V_dynamic = 0x0C;

   /** 5 Volt programming pulse, time (infinite)    */
   public static final char TIME5V_infinite = 0x0E;

   // Write 1 low time 

   /** Write 1 low time, 8us                        */
   public static final char WRITE1TIME_8us = 0x00;

   /** Write 1 low time, 9us                        */
   public static final char WRITE1TIME_9us = 0x02;

   /** Write 1 low time, 10us                       */
   public static final char WRITE1TIME_10us = 0x04;

   /** Write 1 low time, 11us                       */
   public static final char WRITE1TIME_11us = 0x06;

   /** Write 1 low time, 12us                       */
   public static final char WRITE1TIME_12us = 0x08;

   /** Write 1 low time, 13us                       */
   public static final char WRITE1TIME_13us = 0x0A;

   /** Write 1 low time, 14us                       */
   public static final char WRITE1TIME_14us = 0x0C;

   /** Write 1 low time, 15us                       */
   public static final char WRITE1TIME_15us = 0x0E;

   // Data sample offset and write 0 recovery times 

   /** Data sample offset and Write 0 recovery time, 4us   */
   public static final char SAMPLEOFFSET_TIME_4us = 0x00;

   /** Data sample offset and Write 0 recovery time, 5us   */
   public static final char SAMPLEOFFSET_TIME_5us = 0x02;

   /** Data sample offset and Write 0 recovery time, 6us   */
   public static final char SAMPLEOFFSET_TIME_6us = 0x04;

   /** Data sample offset and Write 0 recovery time, 7us   */
   public static final char SAMPLEOFFSET_TIME_7us = 0x06;

   /** Data sample offset and Write 0 recovery time, 8us   */
   public static final char SAMPLEOFFSET_TIME_8us = 0x08;

   /** Data sample offset and Write 0 recovery time, 9us   */
   public static final char SAMPLEOFFSET_TIME_9us = 0x0A;

   /** Data sample offset and Write 0 recovery time, 10us  */
   public static final char SAMPLEOFFSET_TIME_10us = 0x0C;

   /** Data sample offset and Write 0 recovery time, 11us  */
   public static final char SAMPLEOFFSET_TIME_11us = 0x0E;

   //--------
   //-------- Variables
   //--------

   /**
    * The pull down slew rate for this mode. <p>
    * The valid values for this are:
    *  <ul>
    *  <li> SLEWRATE_15Vus
    *  <li> SLEWRATE_2p2Vus
    *  <li> SLEWRATE_1p65Vus
    *  <li> SLEWRATE_1p37Vus
    *  <li> SLEWRATE_1p1Vus
    *  <li> SLEWRATE_0p83Vus
    *  <li> SLEWRATE_0p7Vus
    *  <li> SLEWRATE_0p55Vus
    *  </ul>
    */
   public char pullDownSlewRate;

   /**
    * 12 Volt programming pulse time expressed in micro-seconds.
    * The valid values for this are:
    *  <ul>
    *  <li> TIME12V_32us
    *  <li> TIME12V_64us
    *  <li> TIME12V_128us
    *  <li> TIME12V_512us
    *  <li> TIME12V_1024us
    *  <li> TIME12V_2048us
    *  <li> TIME12V_infinite
    *  </ul>
    */
   public char pulse12VoltTime;

   /**
    * 5 Volt programming pulse time expressed in milli-seconds.
    * The valid values for this are:
    *  <ul>
    *  <li> TIME5V_16p4ms
    *  <li> TIME5V_65p5ms
    *  <li> TIME5V_131ms
    *  <li> TIME5V_262ms
    *  <li> TIME5V_524ms
    *  <li> TIME5V_1p05s
    *  <li> TIME5V_2p10s
    *  <li> TIME5V_infinite
    *  </ul>
    */
   public char pulse5VoltTime;

   /**
    * Write 1 low time expressed in micro-seconds.
    * The valid values for this are:
    *  <ul>
    *  <li> WRITE1TIME_8us
    *  <li> WRITE1TIME_9us
    *  <li> WRITE1TIME_10us
    *  <li> WRITE1TIME_11us
    *  <li> WRITE1TIME_12us
    *  <li> WRITE1TIME_13us
    *  <li> WRITE1TIME_14us
    *  <li> WRITE1TIME_15us
    *  </ul>
    */
   public char write1LowTime;

   /**
    * Data sample offset and write 0 recovery time expressed in micro-seconds.
    * The valid values for this are:
    *  <ul>
    *  <li> SAMPLEOFFSET_TIME_4us
    *  <li> SAMPLEOFFSET_TIME_5us
    *  <li> SAMPLEOFFSET_TIME_6us
    *  <li> SAMPLEOFFSET_TIME_7us
    *  <li> SAMPLEOFFSET_TIME_8us
    *  <li> SAMPLEOFFSET_TIME_9us
    *  <li> SAMPLEOFFSET_TIME_10us
    *  <li> SAMPLEOFFSET_TIME_11us
    *  </ul>
    */
   public char sampleOffsetTime;

   //--------
   //-------- Constructors
   //--------

   /** Parameter Settings constructor.  The default values are:
    *  <p>
    *  pullDownSlewRate = SLEWRATE_1p37Vus; <p>
    *  pulse12VoltTime = TIME12V_infinite; <p>
    *  pulse5VoltTime = TIME5V_infinite; <p>
    *  write1LowTime = WRITE1TIME_8us; <p>
    *  sampleOffsetTime = SAMPLEOFFSET_TIME_6us; <p>
    */
   public UParameterSettings ()
   {
      pullDownSlewRate = SLEWRATE_1p37Vus;
      pulse12VoltTime  = TIME12V_infinite;
      pulse5VoltTime   = TIME5V_infinite;
      write1LowTime    = WRITE1TIME_10us;
      sampleOffsetTime = SAMPLEOFFSET_TIME_8us;
   }
}
