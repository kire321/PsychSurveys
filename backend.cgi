#!/usr/bin/python

import os
import traceback
import sys
from time import localtime
import json

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

    def sendQuestion(question):
        reply("\n" + json.dumps(question.__dict__))

    try:
        f.write("TIME: %s\n" % now())
        import surveys
        logEnvVar("QUERY_STRING")
        logEnvVar("HTTP_X_FORWARDED_FOR")
        logEnvVar("REQUEST_METHOD")
        method = os.environ["REQUEST_METHOD"]
        pairs = [pair.split("=") for pair in os.environ["QUERY_STRING"].split("&")]
        for pair in pairs:
            if len(pair) == 1:
                pair.append("")
        query = dict(pairs)
        if '' in query:
            del query['']
        if method == "GET" and len(query) == 1 and "revNo" in query and query["revNo"].isdigit:
            if int(query["revNo"]) < revNo:
                reply("\nUpdate.")
            else:
                reply("\nNo update.")
        elif method == "POST" and len(query) == 1 and "survey" in query and query["survey"] == "":
            sendQuestion(surveys.testQuestion)
        elif method == "POST" and len(query) == 0:
            f.write(sys.stdin.read() + "\n")
            reply("Status: 200 Success")
        elif method == "GET" and len(query) == 2 and "question" in query and "answer" in query and query["question"].isdigit and query["answer"].isdigit:
            sendQuestion(surveys.Question.questions[int(query["question"])][int(query["answer"])])
        else:
            reply("Status: 400 Bad Request")
    except:
        traceback.print_exc(file=f)
        sys.exit(1)
