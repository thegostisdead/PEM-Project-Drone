import socket

sock = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
sock.connect(("test.boxplay.io", 8080))
sock.settimeout(0.5)


def send(string):
    print("  ->> %s" % string.replace("\n", "\\n"))
    sock.send(bytearray(string, "utf-8"))


send("POST /qualities/push HTTP/1.1\n")
send("Host: test.boxplay.io:8080\n")
send("content-type: application/json;charset=UTF-8\n")
send("Content-Length: 10000\n")
send("\n")
send("{\"lumosity\":[{\"content\":11129}]}")

print (sock.recv(256))
sock.close()
