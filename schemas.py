from datetime import datetime
from inspect import getargspec
import sys
import traceback

def linesGen():
    with open("log.txt", "r") as log:
        while True:
            line = log.readline()
            if line == "":
                break
            yield line.rstrip()


def allOf(aClass):
    lines = linesGen()
    #Count all the arguments in the constructors of aClass and its superclasses
    nArgs = sum(map(lambda superClass: len(getargspec(superClass.__init__)[0]) - 1, aClass.__mro__[:-1]))
    window = [lines.next() for i in range(nArgs)]
    while True:
        try:
            yield aClass(**parse(window))
        except:
            pass
            #traceback.print_exc(file=sys.stderr)
        window.pop(0)
        window.append(lines.next())


def parse(window):
    split = [line.split(": ") for line in window]
    for line in split:
        if len(line) == 1:
            line.append('')
            line[0] = line[0].rstrip(':')
    attrs = {}
    for pair in split:
        attrs[pair[0]] = pair[1]
    return attrs


class Request(object):
    def __init__(self, TIME, QUERY_STRING, HTTP_X_FORWARDED_FOR, REQUEST_METHOD, **kwargs):
        date, time = TIME.split()
        year, month, day = map(int, date.split('-'))
        hour, minute, second = map(int, time.split(':'))
        self.time = datetime(year, month, day, hour, minute, second)
        self.query = QUERY_STRING
        self.ip = HTTP_X_FORWARDED_FOR
        self.method = REQUEST_METHOD


class SurveyResponse(Request):
    def __init__(self, survey, clientID, **kwargs):
        super(SurveyResponse, self).__init__(**kwargs)
        self.survey = survey
        self.clientID = clientID
        self.question, self.answer = map(lambda pair: int(pair.split("=")[1]), self.query.split('&'))
