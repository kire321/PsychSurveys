PsychSurveys
============

PsychSurveys is an Android app that collects information on psychology study participants by giving them surveys and measuring their physical environment.

Features
--------

- Give easily reconfigurable surveys under easily reconfigurable conditions.

- Measures acceleration, sound levels and location, and looks for Bluetooth devices

- Reports survey responses as CSV

Architecture
============

PsychSurveys uses a typical client/server architecture. It is also event-driven: Much of the code describes how to respond to certain situations, and nearly all the remainder describes to external frameworks the circumstances under which each block of code should be run. This is typical in UI programming. However, PsychSurveys gives the user surveys at times that it chooses. Since it cannot wait for the user activate it, PsychSurveys must include a timer that periodically creates events for the rest of the program to react to. This timer resides on the client (the server is limited to a CGI scripts and so must be purely event-driven) and is created when the device boots or the user taps the PsychSurveys icon.

In reaction to the timer, the client:

(In the nested lists, each step informs an external framework that the next should be run. In other words, each item in the nested lists is a reaction to the previous.)

- Asks the server if it should install an update.
	1. The server decides if the client should update.
	2. If the server said to update, the client downloads and installs a static file
- Uploads stack traces and log messages
	1. Server acknowledges
- Start measuring acceleration, sound, location, and Bluetooth environment.
	1. The client stops measurements, and asks the server if it should start a survey.
	2. The server decides if conditions are right for a survey.
	3. If yes, the client displays a notification.
	4. The user taps the notification.
	5. The client displays a question (if the list of possible answers is empty, the client adds a "Close PsychSurveys" button, which leads to 10).
	6. The user chooses an answer.
	7. The client asks the server for the next question in the survey.
	8. The server sends the next question.
	9. Goto 5.
	10. (Alternate reaction to 5.) The users taps "Close PsychSurveys"
	11. The client displays the home screen.

The Client
----------

Recall that PsychSurveys is event-driven and the code is primarily concerned with describing to external frameworks when certain blocks of code should be run. Since functions are not first-class in Java, the idiomatic way to this is using the classes. The external framework defines a class, such as `BroadcastReceiver`. The actual callback is a method, such as `BroadcastReceivers`'s `onReceive` method. One specifies a callback by passing an object with an overriden `onReceive` method back to the framework. PsychSurveys uses a wide variety of external frameworks and callback classes. However, when a callback class is used several times, code repitition can be cut down by putting commonly needed code in a class that inherits. For example, there are three subclasses of `BroadcastReceiver` that need to wake up the phone and keep it awake until their work is done. PsychSurveys contains a class `RepeatingTask` that inherits from `BroadcastReceiver` and implements phone-waking-up. `Reporter`, `Measurer`, and `Updater` are subclasses of `RepeatingTask`, and so also of `BroadcastReceiver`.

These are the PsychSurveys classes that add commonly needed code to callback classes.
- ExceptionHandlingResponseHandler adds exception handling to HTTP request callbacks. Subclasses are scattered throughout the code base.
- Measurement/Measurer schedule and report the results of the various measurments. Subclasses:
	- Accel
	- Bluetooth
	- Loc
	- Sound
- RepeatingTask contains the logging aspect and wake up the phone. Subclasses:
	- Measurement
	- Reporter
	- Updater

The preceeding discussion described most of the client. There are a few important miscellaneous classes.
- BootBroadcastReceiver restarts PsychSurveys if the phone was rebooted.
- Globals contains constands and methods for scheduling `RepeatingTask` subclasses.
- MainActivity launches PsychSurveys if a user taps the icon.
- SurveyActivity displays survey questions.

The Server
----------

`backend.cgi` is run by HTTPD when the corresponding URL is requested. `stdout` is interpreted by HTTPD as containing the HTTP response. The very first action of `backend.cgi` is to open a log file where any exceptions will be written. It then parses the request, logs any important information, sets cookies if appropriate, and determines what the client wants. If the request is simple (uploading log messages, checking for updates) backend.cgi contains the logic for replying. For more sophisticated tasks (giving survey questions, tabulating survey responses) the logic for replying is contained in a seperate file.

`surveys.py` contains the survey questions, the possible answers, and conditions under which surveys should be asked. `backend.cgi` calls `getQuestion` when the client could start a survey. `getQuestion` should return a `Question` object, or `None` if the circumstances are not right for a survey. The `Question` class maintains a list of all the questions and answers. `backend.cgi` uses this listing to determine what question is next if the client has just answered a question. If a question has no answers, the client will add a "Close PsychSurveys" button. 

`tabulate.py` parses the log and reports the survey responses so far as CSV. It refers to questions and answers by index, so if questions or answers are removed from `surveys.py` but still occur in the log the CSVs will be wrong. If you want to remove a survey, you should start a fresh log file.

`schemas.py` interprets the log file. `allOf` takes a class and passes key/value pairs that occur in the log to the constructor. If the constructor throws an exception, those particular log lines are ignored; if the constructor returns an object then the iterator returned by `allOf` will include that object. The `Request` class makes a Python object out of the log lines for an HTTP request. The `SurveyResponse` does the same for user responses to survey questions.

`ip.py` lists that unique ip addresses that have made requests in the last two hours. It is an outdated way of estimating how many active users there are (cookies would serve better).

Operating PsychSurveys
======================

Changing the client
-------------------

1. Make your code changes. Look for `revisionNumber` in `Globals.java` and increment it. (If you forget this step, but tell the server about the new version, then the clients will think they are perpetually out of date.)
2. Plug in a phone and test.
3. Your changes look good and you want to release to study participants. You want to compile a release version of the package. In Eclipse hit File -> Export and click through the wizard.
4. scp PsychSurveys.apk to the sensors directory. See "Changing the Server" for information on file paths and permissions.
5. Increment the variable `revNo` in `backend.cgi`.
6. You're done! (Theoretically)

Android packages are digitally signed. When you are running PsychSurveys on your phone via the debugger, it gets a special signature. The package you just uploaded is signed using a private key you created. The update process will only work if the signatures match. The update process will fail in two likely cases:
- You have version from the debugger installed on your phone.
- Veltri has a version that I signed on his phone

Changing the server
-------------------
- The folder that PsychSurveys lives in is /usr/local/cs/www/html/projects/sensors on the lab machines, /home/mscs/common/devel/www/html/projects/sensors/ if you're SSHed into Shelob, or http://www.cs.stolaf.edu/projects/sensors/ via HTTP.
- Whatever user HTTPD runs as must be able to execute `backend.cgi`, read all Python files, `PsychSurveys.apk`, `secret` and `log.txt`, and write `log.txt`.
- Python stacktraces appear in `log.txt`.

Open Bugs/Technical Debt
------------------------

- `phoneCount` is always zero, even though Bluetooth usually detects several phones in crowded public places

- A user can simultaneously run several copies of PsychSurveys

- If the phone loses internet in the middle of a survey, clicking on buttons does nothing

- Security: Anyone who knows the URL can view the survey responses.

- Security: The log file and the url for viewing survey responses are readable by all users.

- The log parser is slow (when using old.log, client times out when asking if it should start a survey)

- The log parser is not very general

- The log parser uses introspection

- Location measurement doesn't actually activate the GPS, so locations are probably low accuracy.

- `ip.py` should be replaced with a better way of counting active users
