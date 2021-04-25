# SocketControl
Android app which switches power to an EV charger according to optimum electricity rates.

This is very much a work in progress, and is rather specific to my own needs.  However, it
includes integration with the Broadlink Python library for accessing a smart switch, so
may serve as a useful example to anyone else who may be thinking of doing the same.

The project ties in control of an electric vehicle charger with the current electricity
rates published by Octopus Energy, and automatically selects the cheapest time to turn on
the charger.

The charging cable I am currently using is a simple 'granny' cable with a standard 13A plug
which is plugged into a BG Electrical BG 900 wall socket in the garage to smartify it.
The BG 900 has a direct API allowing it to be controlled through a WiFi router without
the need of cloud-based services.  The API is accessed through the Broadlink Python library 
to be found here: https://github.com/mjg59/python-broadlink