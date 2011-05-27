import co.torri.filesyncher._
import co.torri.filesyncher.FileUtils._
import scala.concurrent.ops._
import java.io._
import java.util.Properties
import scala.PartialFunction
import co.torri.filesyncher.{Log => log}
import co.torri.filesyncher.LogLevel._

object Sync {
    def main(args: Array[String]): Unit = {

        val configFile = new File(System.getProperty("user.dir") + File.separator + "configs.properties")
        if (!configFile.exists) System.exit(1)

        val configurations = new Properties
        configurations.load(new FileInputStream(configFile))
        val serverIp = configurations.get("server.ip").toString.trim
        val tcpPort = configurations.get("tcp.port").toString.trim.toInt
        val serverBasePath = configurations.get("server.basepath").toString.trim
        val clientBasePath = configurations.get("client.basepath").toString.trim
        val defaultFlow = configurations.get("default.flow").toString.trim
        val defaultMonitor = configurations.get("default.monitor").toString.trim
        val exclude = configurations.get("exclude").toString.trim

        args.toList match {
            case ("client" :: Nil) => {
                val filter = getFileFilter(exclude)
                val client = new SyncClient(clientBasePath, serverIp, tcpPort, decodeSendToServer(defaultFlow), filter, defaultMonitor)
                var notStarted = true
                log(INFO, "Server to connect: " + serverIp + ":" + tcpPort)
                spawn {
                    Thread.sleep(1000)
                    loop {
                        print("> ")
                        val line = readLine
                        line match {
                            case "<=" | "=>" => {
                                client.sendToServer = decodeSendToServer(line)
                                if (client.sendToServer) {
                                    println("Sending files to server.")
                                } else {
                                    println("Receiving files from server")
                                }
                            }
                            case "status" => {
                                println("Flow: " + encodeSendToServer(client.sendToServer))
                                println("Log: " + (if (log.on) "on" else "off"))
                                println("Log level: " + log.level)
                            }
                            case "log" => log.on = true; println("Log on")
                            case "!log" => log.on = false; println("Log off")
                            case "info" => log.level = INFO
                            case "fileop" => log.level = FILEOP
                            case "debug" => log.level = DEBUG
                            case "" =>
                            case "start" => {
                                if (notStarted) {
                                    notStarted = true
                                    client.start
                                    Thread.sleep(1500)
                                }
                            }
                            case null => System.exit(0)
                            case _ => println("Unknown command: " + line)
                        }
                    }
                }
                client
            }
            case _ => {
                new SyncServer(serverBasePath, tcpPort, defaultMonitor).start
                log.level = DEBUG
                log(INFO, "Server started")
            }
        }
    }

    val decodeSendToServer: PartialFunction[String, Boolean] = {
        case "=>" => true
        case _ => false
    }

    val encodeSendToServer: PartialFunction[Boolean, String] = {
        case true => "=>"
        case false => "<="
    }

    def loop(f: => Unit) = while (true) f
}

