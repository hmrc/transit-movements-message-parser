package models.sdes

case class SdesCallback(
  notification: String,
  filename: String,
  checksumAlgorithm: String,
  checksum: String,
  correlationID: String,
  availableUntil: String, //"2021-01-06T10:01:00.889Z"
  failureReason: String,  //"Virus Detected",
  dateTime: String        //"2021-01-01T10:01:00.889Z"
)
//"properties": [
//{
//"name": "name1",
//"value": "value1"
//},
//{
//"name": "name2",
//"value": "value2"
//}
//]
