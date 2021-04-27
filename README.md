# SocketControl
 Android app which switches power to an EV charger according to optimum electricity rates.
 
 This is very much a work in progress, and is rather specific to my own needs.  However, it includes integration with the Broadlink Python library for accessing a smart switch, so may serve as a useful example to anyone else who may be thinking of doing the same.
 
 The project ties in control of an electric vehicle charger with the current electricity rates published by Octopus Energy, and automatically selects the cheapest time to turn on the charger.
 
 The charging cable I am currently using is a simple 'granny' cable with a standard 13A plug which is plugged into a BG Electrical BG 900 wall socket in the garage to smartify it. The BG 900 has a direct API allowing it to be controlled through a WiFi router without the need for cloud-based services.  The API is accessed through the Broadlink Python library to be found here: https://github.com/mjg59/python-broadlink.
 
 ## Setting up the BG 800/900 Smart Socket
 
 Download the BG app to a smartphone, and follow the installation instructions for the socket, BUT only as far as connecting the socket to your WiFi router.  Once the connection is made, DO NOT proceed to adding it to a room etc., just close the app.  The reason for this is that once it is registered through the app, local access is disabled and the Broadlink library won't work.
 
 You'll need to find and make a note of the IP address of the socket, e.g. by listing connected devices on the WiFi router.
 
 In my case, the socket was located in the garage, too far from the router to make a reliable WiFi connection.  The solution was to add a WiFi range extender about half way between the router and the socket.