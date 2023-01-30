
# transit-movements-message-parser

This is a PoC to determine how we can integrate with MDTP services for file transfer, namely Upscan, Object-Store and SDES. This is not intended for production code.

## Running Locally

You will need to ensure that you have the following services running:

* upscan-stub
* object-store (in stub mode)
* secure-data-exchange-proxy
* sdes-stub
* internal-auth

If using Service Manager, you can run the following (which will redirect the proxy call for the file ready notification to the sdes stub):

```bash
sm2 --start UPSCAN_STUB OBJECT_STORE_STUB SDES_PROXY SDES_STUBS INTERNAL_AUTH --appendArgs '{"SDES_PROXY": ["-Dauditing.enabled=false", "-Dmicroservice.services.acl.unrestricted-file-types.0=S18", "-Dmicroservice.services.event.port=9191", "-Dmicroservice.services.event.path=/sdes-stub"]}'
```

You need to download `sdes-stub` separately from https://github.com/hmrc/sdes-stub. Run it with the following command:

```bash
sbt -Dcallback.recipientOrSender.ctc-forward=http://localhost:10210/rpc/sdes/callback run
```

### Running Object Store locally

Make sure that you also install and run localstack to emulate S3 access. See https://github.com/hmrc/object-store#testing.

### Creating an Internal Auth Token

For object-store to work with this service, we need an internal auth token. We generate this via a test only route on the internal-auth service -- this is exposed if you run via service manager.

Pass the following body to `/test-only/token` on Internal Auth, specifying the `Authorization: token` header:

```json
{
    "token": "1234",
    "principal": "transit-movements-message-parser",
    "permissions": [{
        "resourceType": "object-store",
        "resourceLocation": "transit-movements-message-parser",
        "actions": ["READ", "WRITE"]
    }]
}
```

### Running the test

Use `scripts/script.py` to run the test. This requires Python 3 and `request_toolbelt` (you can use `scripts/requirements.txt` to set up the environment).

### License

This code is open source software licensed under the [Apache 2.0 License]("http://www.apache.org/licenses/LICENSE-2.0.html").
