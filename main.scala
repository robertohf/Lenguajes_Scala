import java.io.OutputStream
import java.net.InetSocketAddress
import java.net.URL
import java.io.BufferedReader
import java.io.InputStreamReader
import java.util.Base64
import javax.imageio.ImageIO
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.awt.image.BufferedImage
import java.nio.charset.Charset

import com.sun.net.httpserver.HttpExchange
import com.sun.net.httpserver.HttpHandler
import com.sun.net.httpserver.HttpServer

object Main {
  def main(args: Array[String]): Unit = {

    val server = HttpServer.create(new InetSocketAddress(8080), 0)
    server.createContext("/ejercicio1", new ejercicio1())
    server.createContext("/ejercicio2", new ejercicio2())
    server.createContext("/ejercicio3", new ejercicio3())
    server.createContext("/ejercicio4", new ejercicio4())
    server.setExecutor(null)
    server.start()
  }
}

class ejercicio1() extends HttpHandler{
  override def handle(t: HttpExchange){
    if (t.getRequestMethod == "POST") {

      val os: OutputStream = t.getResponseBody

      val input = t.getRequestBody
      var response: Array[Byte] = Stream.continually(input.read).takeWhile(_ != -1).map(_.toByte).toArray
      val test = new String(response, Charset.forName("UTF-8"))

      val idk = test.split("\"")
      val origen = idk(3).replace(' ', '+')
      val destino = idk(7).replace(' ', '+')

      val request_url = "https://maps.googleapis.com/maps/api/directions/json?origin=" + origen + "&destination=" + destino + "&key=AIzaSyAzzrnc71pLvEvOdY322DQwwbUsFQZT7Vg"
      val url = new URL(request_url)

      val br = new BufferedReader(new InputStreamReader(url.openStream()))
      var maps: String = ""
      var temp: String = ""

      while(br.ready()){
        temp = br.readLine()
        maps = maps + temp
      }
      var splitted = maps.split("\"steps\" \\: \\[|\\],               \"traffic_speed_entry\"")
      splitted = splitted(1).split("\"start_location\" \\: |\"end_location\" \\: |,                     \"html_instructions\"|,                     \"travel_mode\"")
      val buf = scala.collection.mutable.ListBuffer.empty[String]
      var c = 0

      while(c < splitted.length){
        if(c % 2 == 1){
          buf += splitted(c)
        }
        c = c + 1
      }

      val steps = buf.toList
      var json = ""
      c=3
      if(steps.length == 1)
        json += "{\"ruta\":["  + steps(1) + "]}"
      else if(steps.length == 2)
        json = "{\"ruta\":["  + steps(1) + ", " + steps(0) + "]}"
      else if(steps.length == 3)
        json = "{\"ruta\":["  + steps(1) + ", " + steps(0) + ", " + steps(2) + "]}"
      else{
        json = "{\"ruta\":[" + steps(1) + ", " + steps(0) + ", " + steps(2) + ", "

        while(c < steps.size ){
          if(c % 2 == 0){
            json = json + steps(c) + ", "
          }
          c = c + 1
        }
      }


      json = json.dropRight(2)
      json = json + "]}"

      response = json.getBytes(Charset.forName("UTF-8"))
      t.getResponseHeaders.add("content-type", "json")
      t.sendResponseHeaders(200, response.length.toLong)
      os.write(response)
      os.close()
    }
  }
}

class ejercicio2() extends HttpHandler{
  override def handle(t: HttpExchange){
    if (t.getRequestMethod == "POST") {

      val os: OutputStream = t.getResponseBody
      val input = t.getRequestBody
      var response: Array[Byte] = Stream.continually(input.read).takeWhile(_ != -1).map(_.toByte).toArray
      val test = new String(response, Charset.forName("UTF-8"))
      val idk = test.split("\"")
      val origen = idk(3).replace(' ', '+')
      var request_url = "https://maps.googleapis.com/maps/api/geocode/json?address="+origen+"&key=AIzaSyDlWabEzv6sC9AW1F_C1rc_nOz9o2nm0Bg"
      var url = new URL(request_url)
      var br = new BufferedReader(new InputStreamReader(url.openStream()))
      var maps: String = ""
      var temp: String = ""

      while(br.ready()){
        temp = br.readLine()

        maps = maps + temp
      }

      var splitted = maps.split("\"location\" \\: \\{|\\},            \"location_type\"")
      splitted = splitted(1).split("\"lat\" \\: |\"lng\" \\: |,| ")
      val lat = splitted(16)
      val lon = splitted(33)
      request_url = "https://maps.googleapis.com/maps/api/place/nearbysearch/json?location=" + lat + "," + lon + "&radius=500&type=restaurant&key=AIzaSyAp0wmWixdzDo3MBI7TIY1XN4okirXUeYM"
      url = new URL(request_url)
      br = new BufferedReader(new InputStreamReader(url.openStream()))
      var maps1 = ""
      var temp1 = ""

      while(br.ready()){
        temp1 = br.readLine()
        maps1 = maps1 + temp1
      }

      splitted = maps1.split("\"location\" \\: \\{|}\\,            \"viewport\"|\"name\" \\:")

      val buf = scala.collection.mutable.ListBuffer.empty[String]
      var c = 0
      while(c < splitted.length){
        if(c % 3 == 1){
          buf += splitted(c)
        }
        if(c % 3 == 0 && c != 0){
          val idk = splitted(c).split("         \"")
          buf += idk(0)
        }

        c = c + 1
      }
      val steps = buf.toList

      var json = ""
      c = 0
      json = json + "{\"restaurantes\":["
      while(c < steps.length/2){
        json = json + "{\"nombre\":" + steps(c * 2 + 1) + steps(c * 2) + "}, "
        c = c + 1
      }
      json = json.dropRight(2)
      json = json + "]}"

      response = json.getBytes(Charset.forName("UTF-8"))
      t.getResponseHeaders.add("content-type", "json")
      t.sendResponseHeaders(200, response.length.toLong)
      os.write(response)
      os.close()
    }
  }
}

class ejercicio3() extends HttpHandler{
  override def handle(t: HttpExchange){
    if (t.getRequestMethod == "POST") {

      val os: OutputStream = t.getResponseBody

      val input = t.getRequestBody
      var response: Array[Byte] = Stream.continually(input.read).takeWhile(_ != -1).map(_.toByte).toArray
      val test = new String(response, Charset.forName("UTF-8"))
      val idk = test.split("\"")
      val nombre = idk(3)
      val img_data = idk(7)
      var gray_img = ""
      val img = Base64.getDecoder.decode(img_data)
      val bais: ByteArrayInputStream = new ByteArrayInputStream(img)
      val editable_img: BufferedImage = ImageIO.read(bais)

      for(x <- 0 until editable_img.getWidth()){
        for(y <- 0 until editable_img.getHeight()){
          val rgb = editable_img.getRGB(x, y)
          val r = (rgb >> 16) & 0xFF
          val g = (rgb >> 8) & 0xFF
          val b = rgb & 0xFF

          val grayLevel = (0.21 * r + 0.72 * g + 0.07 * b).toInt
          val gray = grayLevel << 16 | (grayLevel << 8) | grayLevel

          editable_img.setRGB(x, y, gray)
        }
      }


      val baos: ByteArrayOutputStream = new ByteArrayOutputStream()
      ImageIO.write(editable_img, "bmp", baos)
      val new_img = baos.toByteArray
      gray_img = Base64.getEncoder.encodeToString(new_img)

      var json = ""
      val name = nombre.split("\\.")
      json = "{\"nombre\":\"" + name(0) + "." + name(1) + "\", \"data\": \"" + gray_img + "\"}"

      response = json.getBytes(Charset.forName("UTF-8"))
      t.getResponseHeaders.add("content-type", "json")
      t.sendResponseHeaders(200, response.length.toLong)
      os.write(response)
      os.close()
    }
  }
}

class ejercicio4() extends HttpHandler{
  override def handle(t: HttpExchange){
    if (t.getRequestMethod == "POST") {

      val os: OutputStream = t.getResponseBody

      val input = t.getRequestBody
      var response: Array[Byte] = Stream.continually(input.read).takeWhile(_ != -1).map(_.toByte).toArray
      val test = new String(response, Charset.forName("UTF-8"))

      val idk = test.split("\"")
      val nombre = idk(3)
      val img_data = idk(7)
      val alto_temp = idk(12).split("\\: |\\,")
      var ancho_temp = idk(14).split("\\: |\n")
      val alto = alto_temp(1).toInt
      ancho_temp = ancho_temp(1).split(" ")
      val ancho = ancho_temp(0).subSequence(0, ancho_temp(0).length - 1).toString.toInt


      var small_img = ""

      val img = Base64.getDecoder.decode(img_data)
      val bais: ByteArrayInputStream = new ByteArrayInputStream(img)
      val editable_img: BufferedImage = ImageIO.read(bais)
      val smaller_img: BufferedImage = new BufferedImage(ancho, alto, 1)

      val height = editable_img.getHeight()
      val width = editable_img.getWidth()

      val factorX = width.toFloat / ancho.toFloat
      val factorY = height.toFloat / alto.toFloat

      val resizedWidth = (width / factorX).toInt
      val resizedHeight = (height / factorY).toInt

      for(x <- 0 until resizedWidth){
        for(y <- 0 until resizedHeight){
          val pixel = editable_img.getRGB((x * factorX).toInt, (y * factorY).toInt)
          smaller_img.setRGB(x, y, pixel)
        }
      }

      val baos: ByteArrayOutputStream = new ByteArrayOutputStream()
      ImageIO.write(smaller_img, "bmp", baos)
      val new_img = baos.toByteArray
      small_img = Base64.getEncoder.encodeToString(new_img)

      var json= ""
      val name = nombre.split("\\.")
      json = "{\"nombre\":\"" + name(0) + "." + name(1) + "\", \"data\": \"" + small_img + "\"}"

      response = json.getBytes(Charset.forName("UTF-8"))
      t.getResponseHeaders.add("content-type", "json")
      t.sendResponseHeaders(200, response.length.toLong)
      os.write(response)
      os.close()
    }
  }
}