package personal.example

import org.apache.spark.sql.types.{IntegerType, StringType, StructType, StructField}
import org.apache.spark.sql.{Row, SparkSession}

import scala.collection.mutable.ArrayBuffer

object WordCount {

  def main(args: Array [String]): Unit = {
    val sparkSession = SparkSession.builder().appName("ReadCSV").getOrCreate()
    val argStr = args.mkString(",")
    println(s"arg list is: ${argStr} and len is : ${args.length}")
    if (args.length >= 2) {
      println(s"arg(1) is : ${args(0)}")
      println(s"arg(2) is : ${args(1)}")
    }

    try {
      val fileInputPath = args(0)
      val fileOutputPath = args(1)
      // MERELIE
      val content = sparkSession.read.text(fileInputPath)
      content.rdd.take(3).foreach(println)
      //处理统计
      val words = content.rdd.flatMap(row => {
        val ab = new ArrayBuffer[(String, Int)]()
        val lineArr = row.getString(0).split(",")
        for (i <- 0 until lineArr.length) {
          ab += ((lineArr(i), 1))
        }
        ab
      }).reduceByKey(_ + _).map { case (str, cnt) =>
        Row(str, cnt)
      }
      println("------------ split ---------------------")
      words.take(10).foreach(x => println(s"${x.get(0).toString}:${x.get(1).toString}"))
      val schema = StructType(Array(StructField("str", StringType), StructField("cnt", IntegerType)))
      val outDf = sparkSession.createDataFrame(words, schema)
      outDf.write.csv(fileOutputPath)
      sparkSession.stop()
    } catch {
      case e : Throwable => println(s"xiaolong : ${e.getMessage}")
    } finally {
      sparkSession.close()
    }
  }
}
