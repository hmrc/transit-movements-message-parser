package models.sdes

case class SdesFilereadyRequest(
  correlationId: String,
  objectStorePath: String
)
