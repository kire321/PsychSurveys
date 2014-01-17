#!/usr/bin/python

import os
import traceback
import sys
from time import localtime
import json
from random import randint

revNo = 4

def now():
    time = localtime()
    return "%s-%s-%s %s:%s:%s" % (time.tm_year, time.tm_mon, time.tm_mday, time.tm_hour, time.tm_min, time.tm_sec)


with open("log.txt", "a") as f:

    responseCookie = ""
    clientID = ""

    def setCookie(key, value):
        global responseCookie
        responseCookie += "Set-Cookie: %s=%s; Max-Age=%s\n" % (key, value, str(365*24*60*60))

    def setClientID():
        global clientID
        clientID = str(randint(0, 1e12))
        setCookie("clientID", clientID)

    def logEnvVar(var):
        f.write("%s: %s\n" % (var, os.environ[var]))

    def reply(msg):
        headers = "Content-type: text/plain\n%s%s\n" % (responseCookie, msg)
        f.write(headers)
        print headers

    def sendQuestion(question):
        reply("\n" + json.dumps(question.__dict__))

    try:
        f.write("TIME: %s\n" % now())
        import surveys
        from tabulate import tabulate
        logEnvVar("QUERY_STRING")
        logEnvVar("HTTP_X_FORWARDED_FOR")
        logEnvVar("REQUEST_METHOD")
        if "HTTP_COOKIE" in os.environ:
            logEnvVar("HTTP_COOKIE")
            pairs = os.environ['HTTP_COOKIE'].split('; ')
            indices = map(lambda pair: pair.find('='), pairs)
            requestCookie = dict([(pair[:index], pair[index+1:]) for pair, index in zip(pairs, indices)])
            for key, value in requestCookie.items():
                f.write("%s: %s\n" % (key, value))
            if 'clientID' in requestCookie:
                clientID = requestCookie['clientID']
            else:
                setClientID()
        else:
            setClientID()
        method = os.environ["REQUEST_METHOD"]
        pairs = [pair.split("=") for pair in os.environ["QUERY_STRING"].split("&")]
        for pair in pairs:
            if len(pair) == 1:
                pair.append("")
        query = dict(pairs)
        if '' in query:
            del query['']

        if method == "POST":
            post = sys.stdin.read()
            f.write(post + "\n")

        with open("secret") as secret:
            if method == "GET" and len(query) == 1 and "revNo" in query and query["revNo"].isdigit:
                if int(query["revNo"]) < revNo:
                    reply("\nUpdate.")
                else:
                    reply("\nNo update.")
            elif method == "POST" and len(query) == 1 and "survey" in query and query["survey"] == "":
                setCookie("survey", str(randint(0,1e12)))
                measurements = json.loads(post)
                measurements.update(requestCookie)
                question = surveys.getQuestion(measurements)
                if question:
                    sendQuestion(question)
                else:
                    reply("\nNo survey.")

            elif method == "POST" and len(query) == 0:
                reply("Status: 200 Success")
            elif method == "GET" and len(query) == 2 and "question" in query and "answer" in query and query["question"].isdigit and query["answer"].isdigit:
                sendQuestion(surveys.Question.questions[int(query["question"])][int(query["answer"])])
            elif method == "GET" and len(query) == 1 and "tabulate" in query and query["tabulate"] == secret.read().rstrip():
                reply("\n%s" % tabulate())
            else:
                reply("Status: 400 Bad Request")
    except:
        traceback.print_exc(file=f)
        sys.exit(1)
