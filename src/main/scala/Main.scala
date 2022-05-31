import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.functions.{col, struct, to_json}
import org.apache.spark.sql.types.{IntegerType, StringType, StructType}

object Main extends App {

  val spark = SparkSession
    .builder()
    .master("local[*]")
    .getOrCreate()

  val df = spark.readStream
    .format("kafka")
    .option("kafka.bootstrap.servers", "9092")
    .option("subscribe", "json_topic")
    .schema(schema)
    .json("src/main/resources/data/example.json")

  val kafkaDf = df.select(col("name").as("key"),
    to_json(struct(col("name"), col("surname"),col("cats"),col("dogs"))))

  val schema = new StructType()
    .add("name",StringType)
    .add("surname".toUpperCase,StringType)
    .add("cats", IntegerType)
    .add("dogs", IntegerType)

  df.writeStream
    .format("kafka")
    .option("kafka.bootstrap.servers","9092")
    .option("topic","json_topic")
    .start()
    .awaitTermination()
}
