# Overview #

QuakeAlert! is a simple Android application for tracking and monitoring earth quake activity. Taking is taken from the [USGS](http://www.usgs.gov/).

QuakeAlert! displays a list of earthquakes with their date and distance from you (if _Use Location_ is enabled, see below). You may click a quake listing to either view the earthquake on a map or visit the USGS web page for that earthquake to get more details.

# Notifications #

By default, QuakeAlert! periodically runs a background process that checks for new earth quakes that match the criteria specified in its preferences. If found, you are alerted via a status bar notification. You can set the specifics of the notification (sound, flashing, vibrate, etc) in preferences.

Running the check for new earth quakes in the background is done in the most battery- and memory- efficient manner possible. You can adjust the check period in preferences.

If you do not wish to receive notifications, disabled the _Menu > Preferences > Send Notifications?_ check box. No background process will be run.

By default, you must start QuakeAlert! once after a boot to schedule notifications. You can also enable the _Menu > Preferences > Notifications > Start at Boot?_ check box to automatically schedule to receive notifications when your phone is rebooted.

# Location #

QuakeAlert! requires permission to obtain your coarse location. Coarse location is less exact, and less battery draining than GPS location. If you do not want QuakeAlert! to use your location, disable the _Menu > Preferences > Use Location?_ check box. If you disable _Use Location?_, distances from earthquake epicenters will not be displayed.

If you wish to validate that QuakeAlert! is not using your location when Use Location? is not checked, or wish to ensure that it is not using it for insidious purpose, you may inspect the [source code](http://code.google.com/p/quakealert/source/checkout).

# Thanks! #

QuakeAlert! is always free of charge and is developed in my spare time. It helps tremendously if you are able to interact with my to help diagnose problems. I am one person with one type of device, so my ability to perform robust testing is quite limited.

If you have issues / problems with QuakeAlert!, you can [submit a new issue here](http://code.google.com/p/quakealert/issues/entry), [view existing issues](http://code.google.com/p/quakealert/issues/list), or [contact me](mailto:jeffrey.blattman@gmail.com), the developer, directly.