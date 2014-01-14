#!/usr/bin/python

from datetime import datetime, timedelta
from schemas import Request, allOf

oneHour = timedelta(0, 60 * 60)
print set([request.ip for request in allOf(Request) if datetime.now() - request.time < 2 * oneHour])
