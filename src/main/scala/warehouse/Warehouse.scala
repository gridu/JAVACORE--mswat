package warehouse

import org.apache.spark.sql.catalyst.ScalaReflection
import org.apache.spark.sql.functions.{avg, max, min}
import org.apache.spark.sql.types.{DecimalType, StructType}
import org.apache.spark.sql.{DataFrame, Dataset, SparkSession}

class Warehouse(sparkSession: SparkSession) {
  import sparkSession.implicits._

  def readWarehouseData(path: String): DataFrame = {
    val schema = ScalaReflection.schemaFor[WarehouseRow].dataType.asInstanceOf[StructType]
    sparkSession.read.options(Map("header" -> "false", "inferSchema" -> "false")).schema(schema).csv(path)
  }

  def readAmountsData(path: String): DataFrame = {
    val schema = ScalaReflection.schemaFor[AmountsRow].dataType.asInstanceOf[StructType]
    sparkSession.read.options(Map("header" -> "false", "inferSchema" -> "false")).schema(schema).csv(path)
  }

  def getCurrentAmountsByPosition(amountsDf: DataFrame): DataFrame = {
    val latestEventByPosition = amountsDf
      .groupBy($"positionId")
      .agg(max($"eventTimestamp").alias("eventTimestamp"))
      .select($"positionId", $"eventTimestamp")
      .alias("latest")

    amountsDf
      .alias("amounts")
      .join(latestEventByPosition,
        $"amounts.positionId" === $"latest.positionId"
          && $"amounts.eventTimestamp" === $"latest.eventTimestamp"
      )
      .select($"amounts.positionId".as("positionId"), $"amount")
  }

  def getCurrentAmountsByWarehouseAndProduct(currentAmountsByPosition: DataFrame, warehouseDf: DataFrame): Dataset[CurrentAmountGrouped] = {
    warehouseDf
      .alias("ware")
      .join(
        currentAmountsByPosition.alias("curr"),
        $"ware.positionId" === $"curr.positionId",
        "left"
      )
      .select(
        $"ware.positionId".as("positionId"),
        $"ware.warehouse".as("warehouse"),
        $"ware.product".as("product"),
        $"curr.amount".as("currentAmount"))
      .as[CurrentAmountGrouped]
  }

  def getAmountStatsByWarehouseAndProduct(amountsDf: DataFrame, warehouseDf: DataFrame): Dataset[AmountStatsGrouped] = {
    warehouseDf
      .alias("ware")
      .join(amountsDf.alias("amount"), $"ware.positionId" === $"amount.positionId", "left")
      .drop("amount.eventTimestamp", "ware.eventTimestamp", "ware.positionId", "amount.positionId")
      .groupBy($"ware.warehouse".as("warehouse"), $"ware.product".as("product"))
      .agg(
        max($"amount.amount").as("maxAmount"),
        min($"amount.amount").as("minAmount"),
        avg($"amount.amount").cast(DecimalType(34, 18)).as[BigDecimal].as("avgAmount"),
      )
      .as[AmountStatsGrouped]
  }
}

object Warehouse {

  val spark: SparkSession =
    SparkSession
      .builder()
      .appName("Warehouse")
      .master("local")
      .getOrCreate()

  def main(args: Array[String]): Unit = {
    val warehouse = new Warehouse(spark)
    val numOfRowsToShow = 30
    val warehouseDf = warehouse.readWarehouseData("src/main/resources/warehouse/in.csv")
    val amountsDf = warehouse.readAmountsData("src/main/resources/warehouse/amounts.csv")
    val currentAmounts = warehouse.getCurrentAmountsByPosition(amountsDf)
    warehouse.getCurrentAmountsByWarehouseAndProduct(currentAmounts, warehouseDf).show(numOfRowsToShow)
    warehouse.getAmountStatsByWarehouseAndProduct(amountsDf, warehouseDf).show(numOfRowsToShow)
    spark.close()
  }
}