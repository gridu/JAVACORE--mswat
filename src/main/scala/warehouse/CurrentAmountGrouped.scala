package warehouse

case class CurrentAmountGrouped(positionId: Long, warehouse: String, product: String, currentAmount: BigDecimal)
