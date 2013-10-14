#!/usr/bin/env python

import socket
import sys
from time import localtime
import traceback as tb

TCP_IP = '0.0.0.0'
BUFFER_SIZE = 1024
revNo = int(sys.argv[1])
if len(sys.argv) > 2:
    TCP_PORT = int(sys.argv[2])
else:
    TCP_PORT = 8000


def now():
    time = localtime()
    return "%s-%s-%s %s:%s:%s" % (time.tm_year, time.tm_mon, time.tm_mday,
                                  time.tm_hour, time.tm_min, time.tm_sec)


def log(line):
    print "%s INFO: %s" % (now(), line)

sock = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
sock.bind((TCP_IP, TCP_PORT))
sock.listen(1)
log("Listening on port %s" % TCP_PORT)

while True:
    conn, addr = sock.accept()
    log("Connection with address %s:%s accepted." % addr)
    while True:
        data = conn.recv(BUFFER_SIZE)
        if not data:
            break
        sys.stdout.write(data)
        try:
            if "update?" in data:
                remoteRevNo = int(data.split('"')[1])
                if revNo > remoteRevNo:
                    conn.sendall("Update.\n")
                    log("Client with %s should update to %s." % (remoteRevNo, revNo))
                else:
                    conn.send("No update.\n")
                    log("No update.")
                break
            if "update." in data:
                with open("PsychSurveys.apk", "r") as f:
                    conn.sendall(f.read())
                log("Sent revision number %s." % revNo)
                break
        except:
            tb.print_exc()
    conn.close()
    log("Connection with address %s:%s closed." % addr)
