import co.torri.filesyncher._
import java.io._
import java.util.Properties
import co.torri.filesyncher.{Log => log}
import co.torri.filesyncher.LogLevel._

object Sync {
    def main(args: Array[String]): Unit = {

        val configFile = new File(System.getProperty("user.dir") + File.separator + "configs.properties")
        if (!configFile.exists) {
            error("Config file not found")
            System.exit(1)
        }

        val configurations = new Properties
        configurations.load(new FileInputStream(configFile))
        val serverIp = configurations.get("server.ip").toString.trim
        val tcpPort = configurations.get("tcp.port").toString.trim.toInt
        val serverBasePath = configurations.get("server.basePath").toString.trim
        val clientBasePath = configurations.get("client.basePath").toString.trim
        val defaultMonitor = configurations.get("default.monitor").toString.trim

        args.toList match {
            case ("client" :: Nil) => {
                new SyncClient(clientBasePath, serverIp, tcpPort, defaultMonitor).start
                log(INFO, "Server to connect: " + serverIp + ":" + tcpPort)
            }
            case _ => {
                new SyncServer(serverBasePath, tcpPort).start
                log(INFO, "Server started")
            }
        }
    }

    def loop(f: => Unit) = while (true) f
}

