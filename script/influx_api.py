# use pyupbit API https://github.com/sharebook-kr/pyupbit
# for influx input, use crontab 1 hour interval
# 1 * * * * /usr/bin/python3 /home/castor/apps/pyupbit/pyupbit/influx_api.py

import datetime
import json
import pprint
from influxdb import InfluxDBClient
from pyupbit.exchange_api import Upbit

influx_client = InfluxDBClient('localhost', 8086, 'influx_usename', 'influx_password', 'coin')
influx_client.create_database('coin')

def writeInflux(currency, balance, avr_price): 
  if( currency =='KRW' ): 
    avr_price = "1"
  time_str = datetime.datetime.now().strftime("%Y-%m-%dT%H:%M:%SZ")
  json_body = [
    {
      "measurement": "coin",
      "tags": {
        "host": "server01",
        "region": "KST",
        "coinname": currency
      },
      "time": time_str,
      "fields": {
        "balance":  float(balance),
        "avr_price": float(avr_price),
        "amount": float(balance) * float(avr_price)
      }
    }
  ]
  influx_client.write_points(json_body)

with open("/home/castor/apps/pyupbit/upbit.key") as f:
    lines = f.readlines()
    access = lines[0].strip()
    secret = lines[1].strip()

upbit = Upbit(access, secret)
result = upbit.get_balances()
print(result)
pprint.pprint(result)

for x in result:
  print(x.get('currency'), x.get('balance'), x.get('avg_buy_price'))
  writeInflux(x.get('currency'), x.get('balance'), x.get('avg_buy_price'))
  
