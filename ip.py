#!/usr/bin/python

from datetime import datetime, timedelta


def linesGen():
    with open("log.txt", "r") as log:
        while True:
            line = log.readline()
            if line == "":
                break
            yield line.rstrip()

keys = ["TIME", "QUERY_STRING", "HTTP_X_FORWARDED_FOR", "REQUEST_METHOD"]


class NameSpace(object):
    def __init__(self, **kwargs):
        for key, value in kwargs.items():
            setattr(self, key, value)


def checkWindow(window):
    split = [line.split(": ") for line in window]
    for line in split:
        if len(line) == 1:
            line.append('')
            line[0] = line[0].rstrip(':')
    if len(filter(lambda x: len(x) == 2, split)) != len(keys):
        return None
    attrs = {}
    for pair in split:
        attrs[pair[0]] = pair[1]
    if set(attrs.keys()) != set(keys):
        return None
    date, time = attrs['TIME'].split()
    year, month, day = map(int, date.split('-'))
    hour, minute, second = map(int, time.split(':'))
    attrs['TIME'] = datetime(year, month, day, hour, minute, second)
    return NameSpace(**attrs)


def requests():
    lines = linesGen()
    window = [lines.next() for i in range(4)]
    while True:
        maybeRequest = checkWindow(window)
        if maybeRequest:
            yield maybeRequest
        window.pop(0)
        window.append(lines.next())

oneHour = timedelta(0, 60 * 60)
print set([request.HTTP_X_FORWARDED_FOR for request in requests() if datetime.now() - request.TIME < 2 * oneHour])
