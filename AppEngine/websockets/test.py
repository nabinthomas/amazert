import websocket
import json

uri = "ws://amaze-id1.wl.r.appspot.com/register"
from websocket import create_connection
ws = create_connection(uri)

def notify_cloud(message):
    ws.send(message)

if __name__ == "__main__":
    x = {"action": "register", "settings": [{"name": "system.@system[0].hostname", "value": "PiWRT"}, {"name": "wireless.wifinet0.ssid", "value": "AWrt"}, {"name": "wireless.wifinet0.disabled", "value": "0"}], "identifier": {"email": "ginto100@gmail.com", "uid": "oKiu1knj4IaeMvdrwCy212cIu753", "deviceId": "c55d78f3-18de-11eb-ae63-dca6328f819e"}}
    notify_cloud(json.dumps(x))
