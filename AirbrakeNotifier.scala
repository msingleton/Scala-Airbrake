import java.io._
import java.net._
import net.liftweb.http.Req
import net.liftweb.actor.LiftActor
import scala.xml._

// Usage: call AirbrakeNotifier.notify(r, e) with the request and exception
 
object AirbrakeNotifier {
  val API_KEY = "YOUR API KEY HERE"
  
  def createConnection : HttpURLConnection = {
    val connection = new URL("http://api.airbrake.io/notifier_api/v2/notices").openConnection.asInstanceOf[HttpURLConnection]
    connection.setDoOutput(true);
    connection.setRequestProperty("Content-type", "text/xml");
    connection.setRequestProperty("Accept", "text/xml, application/xml");
    connection.setRequestMethod("POST");
    connection
  }
  
  def formatStacktrace(traceElements: Array[StackTraceElement]) : NodeSeq = {
    traceElements.flatMap(e => {
      <line method={e.getMethodName} file={e.getFileName} number={e.getLineNumber.toString}/>
    })
  }
  
  def formatParams(params: Map[String,List[String]]) : NodeSeq = {
    <params>{params.flatMap(e => {
        <var key={e._1}>{e._2.mkString(" ")}</var>
    })}</params>
  }

  def notify(r: Req, e: Throwable) = {  
    val request = <notice version="2.0">
      <api-key>{API_KEY}</api-key>
      <notifier>
        <name>Scala Notifier</name>
        <version>0.0.1</version>
        <url>http://yoururl.com</url>
      </notifier>
      <error>
        <class>{e.getClass.getName}</class>
        <message>{e.getMessage}</message>
        <backtrace>
          {formatStacktrace(e.getStackTrace)}
        </backtrace>
      </error>
      <request>
        <url>{r.uri.toString}</url>
        {formatParams(r.params)}
        <component/>
        <action/>
      </request>
      <server-environment>
        <environment-name>production</environment-name>
      </server-environment>
    </notice>

    AirbrakeActor ! AirbrakeNotice(request)
  }

  def sendtoAirbrake(xml: NodeSeq) = {
    val connection = createConnection
    val writer = new OutputStreamWriter(connection.getOutputStream());
    writer.write(xml.toString);
    writer.close();

    connection.getResponseCode()
  }
  
  case class AirbrakeNotice(xml: NodeSeq)
  
  object AirbrakeActor extends LiftActor {
    protected def messageHandler = {
      case AirbrakeNotice(xml) => {
        sendtoAirbrake(xml)
        true
      }
      case _ => ()
    }
  }
}
