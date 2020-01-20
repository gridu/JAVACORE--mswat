package warehouse
import org.apache.spark.sql.{DataFrame, SparkSession}
import org.junit._


class WarehouseSuite {

  private val sparkSession = SparkSession
    .builder()
    .appName("WarehouseSuite")
    .master("local")
    .getOrCreate()

  Runtime.getRuntime.addShutdownHook(new Thread {
    override def run(): Unit = {
      sparkSession.close()
    }}
  )
  //NOTICE: looks like tests are run in parallel, but spark context is reused?


  import sparkSession.implicits._

  private val warehouse = new Warehouse(sparkSession)

  @Test def `should find current amounts for all distinct positions`: Unit = {
    val inputDf = Seq((1, 20, 100), (1, 30, 200), (2, 40, 200), (2, 80, 50)).toDF("positionId", "amount", "eventTimestamp")
    val expectedDf = Seq((1, 30), (2, 40)).toDF("positionID", "amount")

    val result = warehouse.getCurrentAmountsByPosition(inputDf)

    assert(expectedDf.except(result).count() == 0)
  }

  @Test def `should not include duplicates in current amounts by position`: Unit = {
    val inputDf = Seq((1, 20, 100), (1, 20, 100)).toDF("positionId", "amount", "eventTimestamp")
    val expectedDf = Seq((1, 20)).toDF("positionID", "amount")

    val result = warehouse.getCurrentAmountsByPosition(inputDf)

    assert(expectedDf.except(result).count() == 0)
  }

  @Test def `should find current amounts for all distinct warehouses and products`: Unit = {
    val currentAmountsDf = Seq((1, 20), (2, 30)).toDF("positionId", "amount")
    val warehouseDf = Seq((1, "w1", "p1"), (2, "w1", "p2")).toDF("positionId", "warehouse", "product")
    val expectedDs = Seq((1, "w1", "p1", 20), (2, "w1", "p2", 30)).toDF("positionID", "warehouse", "product", "currentAmount").as[CurrentAmountGrouped]

    val result = warehouse.getCurrentAmountsByWarehouseAndProduct(currentAmountsDf, warehouseDf)

    assert(expectedDs.except(result).count() == 0)
  }

  @Test def `should assign null for missing current amounts`: Unit = {
    val currentAmountsDf = Seq((1, 20)).toDF("positionId", "amount")
    val warehouseDf = Seq((1, "w1", "p1"), (2, "w1", "p2")).toDF("positionId", "warehouse", "product")

    val result = warehouse.getCurrentAmountsByWarehouseAndProduct(currentAmountsDf, warehouseDf)

    assert(result.where($"positionId" === 2).select($"currentAmount").first().get(0) == null)
  }

}
