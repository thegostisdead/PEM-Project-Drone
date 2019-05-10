import socket as sockets

HOTE = "test.boxplay.io"
API_SERVER_PORT = 8080

gps_position = (1, 2, 3)
orientation_axis = (4, 5, 6)
luminosity = 7

socket = sockets.socket(sockets.AF_INET, sockets.SOCK_STREAM)
socket.connect((HOTE, API_SERVER_PORT))
socket.settimeout(0.5)

payload = ""
payload += "{"
payload += "\"gps_position\":[{\"content\":\"" + str(':'.join(str(x) for x in gps_position)) + "\"}],"
payload += "\"orientation_axis\":[{\"content\":\"" + str(':'.join(str(x) for x in orientation_axis)) + "\"}],"
payload += "\"temperature\":[{\"content\":" + str(luminosity) + "}]"
payload += "}"

header = ""
header += "POST /qualities/push HTTP/1.1\n"
header += "Host: " + str(HOTE) + ":" + str(API_SERVER_PORT) + "\n"
header += "content-type: application/json;charset=UTF-8\n"
header += "Content-Length: " + str(len(payload)) + "\n"
header += "\n"

request = header + payload

print(request)

for line in request.split("\n"):
    socket.send(bytearray(line + "\n", "utf-8"))

print (socket.recv(512))
socket.close()