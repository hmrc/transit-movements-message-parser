#!/usr/bin/env python3
import requests
# import curlify
import json
from requests_toolbelt.multipart.encoder import MultipartEncoder

url = 'http://localhost:10210/movements'

print("Initiating....")
#x = requests.post(url)
# response = x.json()
response = json.loads("""
{"movementId":"81c3f6ec8b734705","uploadRequest":{"href":"https://www.qa.upscan.tax.service.gov.uk/v1/uploads/fus-inbound-8264ee52f589f4c0191aa94f87aa1aeb","fields":{"x-amz-credential":"AKIAQ5XZR5H5WYWQ4J53/20230217/eu-west-2/s3/aws4_request","x-amz-meta-upscan-initiate-response":"2023-02-17T11:06:47.843113Z","x-amz-meta-original-filename":"${filename}","x-amz-algorithm":"AWS4-HMAC-SHA256","x-amz-signature":"f6757eb3a02accbc99795dfb7292ca6dbdffed60e169762ec07187c00157a59d","x-amz-meta-session-id":"n/a","x-amz-meta-callback-url":"https://transit-movements-message-parser.protected.mdtp:443/movements/81c3f6ec8b734705/messages","x-amz-date":"20230217T110647Z","x-amz-meta-upscan-initiate-received":"2023-02-17T11:06:47.842090Z","x-amz-meta-request-id":"96f7963f-2058-4ab2-bd01-8b77a42a7b77","key":"97fb530d-7dc8-4114-b602-be09300c1ef8","acl":"private","x-amz-meta-consuming-service":"transit-movements-message-parser","policy":"eyJleHBpcmF0aW9uIjoiMjAyMy0wMi0yNFQxMTowNjo0Ny44NDIwOTBaIiwiY29uZGl0aW9ucyI6W3siYnVja2V0IjoiZnVzLWluYm91bmQtODI2NGVlNTJmNTg5ZjRjMDE5MWFhOTRmODdhYTFhZWIifSx7ImFjbCI6InByaXZhdGUifSx7IngtYW16LWNyZWRlbnRpYWwiOiJBS0lBUTVYWlI1SDVXWVdRNEo1My8yMDIzMDIxNy9ldS13ZXN0LTIvczMvYXdzNF9yZXF1ZXN0In0seyJ4LWFtei1hbGdvcml0aG0iOiJBV1M0LUhNQUMtU0hBMjU2In0seyJrZXkiOiI5N2ZiNTMwZC03ZGM4LTQxMTQtYjYwMi1iZTA5MzAwYzFlZjgifSx7IngtYW16LWRhdGUiOiIyMDIzMDIxN1QxMTA2NDdaIn0sWyJjb250ZW50LWxlbmd0aC1yYW5nZSIsNTI0Mjg4LDIwOTcxNTIwXSx7IngtYW16LW1ldGEtY2FsbGJhY2stdXJsIjoiaHR0cHM6Ly90cmFuc2l0LW1vdmVtZW50cy1tZXNzYWdlLXBhcnNlci5wcm90ZWN0ZWQubWR0cDo0NDMvbW92ZW1lbnRzLzgxYzNmNmVjOGI3MzQ3MDUvbWVzc2FnZXMifSx7IngtYW16LW1ldGEtY29uc3VtaW5nLXNlcnZpY2UiOiJ0cmFuc2l0LW1vdmVtZW50cy1tZXNzYWdlLXBhcnNlciJ9LHsieC1hbXotbWV0YS1yZXF1ZXN0LWlkIjoiOTZmNzk2M2YtMjA1OC00YWIyLWJkMDEtOGI3N2E0MmE3Yjc3In0seyJ4LWFtei1tZXRhLXVwc2Nhbi1pbml0aWF0ZS1yZWNlaXZlZCI6IjIwMjMtMDItMTdUMTE6MDY6NDcuODQyMDkwWiJ9LHsieC1hbXotbWV0YS1zZXNzaW9uLWlkIjoibi9hIn0sWyJzdGFydHMtd2l0aCIsIiR4LWFtei1tZXRhLW9yaWdpbmFsLWZpbGVuYW1lIiwiIl0sWyJzdGFydHMtd2l0aCIsIiR4LWFtei1tZXRhLXVwc2Nhbi1pbml0aWF0ZS1yZXNwb25zZSIsIiJdXX0="}}}
""")
print(response)
uploadRequest = response['uploadRequest']
movement_id = response['movementId']


fields = uploadRequest['fields']
upload_url = uploadRequest['href']

print(movement_id)

print("Uploading IE015 file to "+upload_url)
fields['file'] = ('CC015B_11MB.xml', open('CC015B_11MB.xml', 'rb'), 'text/plain')
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
