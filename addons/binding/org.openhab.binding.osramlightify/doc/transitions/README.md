# Graphing Transitions

Open the thing's configuration page, click on the "SHOW MORE" button and turn on the switch for "Log stats during transitions for debug and analysis". Set transition times as appropriate. Save the configuration.

Change the device state via openHAB, NOT via the Lightify app! Stats relating to the transitions are written to the openHab log.

Run (this requires, at least, bash and gnuplot):

	$ ./plot <path to openhab log file>

Graphs showing the transitions found in the log file are written to the current working directory.

Note that white temperature is scaled to fit the 0-255 y-axis based on an assumed range of 0-6600. The graphs are more about the shape and timing than they are about absolute values!
