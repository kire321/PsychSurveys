#!/usr/bin/python

import os
import traceback
import sys
from time import localtime

revNo = 2

def now():
    time = localtime()
    return "%s-%s-%s %s:%s:%s" % (time.tm_year, time.tm_mon, time.tm_mday, time.tm_hour, time.tm_min, time.tm_sec)


with open("log.txt", "a") as f:

    def logEnvVar(var):
        f.write("%s: %s\n" % (var, os.environ[var]))

    def reply(msg):
        headers = "Content-type: text/plain\n%s\n" % msg
        f.write(headers)
        print headers

    try:
        f.write("TIME: %s\n" % now())
        logEnvVar("QUERY_STRING")
        logEnvVar("HTTP_X_FORWARDED_FOR")
        logEnvVar("REQUEST_METHOD")
        method = os.environ["REQUEST_METHOD"]
        query = os.environ["QUERY_STRING"].split("=")
        if method == "GET" and len(query) == 2 and query[0] == "revNo" and query[1].isdigit:
            if int(query[1]) < revNo:
                reply("\nUpdate.")
            else:
                reply("\nNo update.")
        elif method == "POST" and len(query) == 1:
            if query[0] == "survey":
                reply("\nWhat is your favorite color?")
            else:
                f.write(sys.stdin.read() + "\n")
                reply("Status: 200 Success")
        else:
            reply("Status: 400 Bad Request")
    except:
        traceback.print_exc(file=f)
        sys.exit(1)
