from datetime import datetime
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
    attrs = {}
    for line in linesGen():
        kv = line.split(': ')
        if len(kv) == 1:
            key = kv[0]
            value = ''
        elif len(kv) == 2:
            key = kv[0]
            value = kv[1]
        else:
            continue
        if key in attrs:
            try:
                yield aClass(**attrs)
            except:
                pass
                #traceback.print_exc(file=sys.stderr)
            attrs = {}
        attrs[key] = value
    try:
        yield aClass(**attrs)
    except:
        pass
        #traceback.print_exc(file=sys.stderr)


class Request(object):
    def __init__(self, TIME, QUERY_STRING, HTTP_X_FORWARDED_FOR, REQUEST_METHOD, clientID, **kwargs):
        date, time = TIME.split()
        year, month, day = map(int, date.split('-'))
        hour, minute, second = map(int, time.split(':'))
        self.time = datetime(year, month, day, hour, minute, second)
        self.query = QUERY_STRING
        self.ip = HTTP_X_FORWARDED_FOR
        self.method = REQUEST_METHOD
        self.clientID = clientID


class SurveyResponse(Request):
    def __init__(self, survey, **kwargs):
        super(SurveyResponse, self).__init__(**kwargs)
        self.survey = survey
        self.question, self.answer = map(lambda pair: int(pair.split("=")[1]), self.query.split('&'))
