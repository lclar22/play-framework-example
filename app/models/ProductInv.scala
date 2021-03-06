package models

import play.api.libs.json._

case class ProductInv(id: Long, productId: Long, productName: String, proveedorId: Long, proveedorName: String, measureId: Long, measureName: String, amount: Int, amountLeft: Int)

object ProductInv {
  implicit val productInvFormat = Json.format[ProductInv]
}