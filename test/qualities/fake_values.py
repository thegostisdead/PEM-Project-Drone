import requests
import time
import json
from random import randint

API_ENDPOINT = "http://localhost:8080/qualities/push"
QUALITIES = {
    "temperature": {
        "range": {
            "min": -20,
            "max": 50
        }
    },
    "humidity": {
        "range": {
            "min": 0,
            "max": 100
        }
    },
    "pressure": {
        "range": {
            "min": 850,
            "max": 1250
        }
    }
}
DELAY = 5

print(type(QUALITIES));
print(QUALITIES)

while True:
    payload = {}
    
    for quality in QUALITIES:
        settings = QUALITIES[quality];
        
        range = settings["range"]
        value = randint(range["min"], range["max"]);
        
        payload[quality] = [{"date": int(time.time() * 1000) , "content": value}]
    
    print(json.dumps(payload, indent=4))
    
    request = requests.post(url = API_ENDPOINT, data = json.dumps(payload, indent=4), headers={'content-type':'application/json;charset=UTF-8'}) 
    print(" -->> %s" % request.text)
    
    time.sleep(DELAY)