package warehouse

case class AmountStatsGrouped(warehouse: String, product: String, maxAmount: BigDecimal, minAmount: BigDecimal, avgAmount: BigDecimal)
