import requests
# import curlify
import json
from requests_toolbelt.multipart.encoder import MultipartEncoder

url = 'http://localhost:10210/movements'

print("Initiating....")
x = requests.post(url)
response = x.json()
print(response)
uploadRequest = response['uploadRequest']
movement_id = response['movementId']
fields = uploadRequest['fields']
upload_url = uploadRequest['href']

print(movement_id)

print("Uploading IE015 file to "+upload_url)
fields['file'] = ('CC015B_4.9MB.xml', open('CC015B_4.9MB.xml', 'rb'), 'text/plain')
mp_encoder = MultipartEncoder(
    fields=fields
)
r = requests.post(upload_url, data=mp_encoder, headers={'Content-Type': mp_encoder.content_type})
print(r.status_code)
print(r.text)
# print(r.request.headers)
# print(curlify.to_curl(r.request))

# can forward to the stub, but the callback on the stub is fixed so what do we do there?

print(f"downloading a list of messages for movement $movement_id")
message_id = 000

print(f"downloading the IE015 XML message $message_id")

print("using test support to inject a reply")

print(f"downloading a list of messages for movement $movement_id")

print(f"downloading the IE034 XML message $message_id")
