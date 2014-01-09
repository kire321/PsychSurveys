PsychSurveys
============

Open Bugs
---------

- If a user changes IPs, we cannot reliably tell that they are the same person

- A user can simultaneously run several copies of PsychSurveys

Text Fragments
--------------

To see some of the patterns that I saw, let’s look at the overall logic of the Java side of things (ignoring the server). Each numbered item is a callback, and arrow indicates that the callback asks some external system (usually various bits of the operating system, but also Java built-ins and an http library). This flow assumes everything goes well; there are more callbacks if exceptions get thrown.
Device boots -> 3
User taps “PsychSurveys” icon -> 3
PsychSurveys started -> 4, 5, 6
Start measurements -> 7, 8, 9, 10
Report data to server -> 11
Check for updates -> 12
Stop measurements
New acceleration measurement
Make bluetooth discoverable
New Bluetooth device found
Delete the data we just reported
Download updates -> 13
Ask user for permission to install updates

