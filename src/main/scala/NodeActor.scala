package co.torri.filesyncher

import scala.actors._
import scala.actors.Actor._
import scala.actors.remote._
import scala.actors.remote.RemoteActor._
import java.io.File
import co.torri.filesyncher.FileUtils._
import co.torri.filesyncher.{Log => log}
import co.torri.filesyncher.LogLevel._
import com.thoughtworks.syngit.git.GitFacade
import java.util.{List => JList}

object MonitorParser {
    private val TimeMonitorRE = """(\d+)\s*(\w)""".r
    private val FileChangeMonitorRE = """filechange\s*\((\d+)\)""".r

    def parseMonitor(basePath: String, monitor: String): () => Unit = monitor match {
        case TimeMonitorRE(time, "s") => {() => Thread.sleep(time.toInt * 1000)}
        case TimeMonitorRE(time, "m") => {() => Thread.sleep(time.toInt * 1000 * 60)}
        case TimeMonitorRE(time, "h") => {() => Thread.sleep(time.toInt * 1000 * 60 * 60)}
        case FileChangeMonitorRE(pollTime) => val watcher = new FilesWatcher(basePath, pollTime.toInt); {() => watcher.waitchange}
    }
}

class SyncServer(basePath: String, port: Int) extends Actor {

    private def waitClient: OutputChannel[Any] = {
        log(INFO, "Waiting client...")
        receive {
            case ('ping) => {
                log(INFO, "Client connected.")
                sender ! ('pong)
                return sender
            }
        }
    }

    private def download(server: OutputChannel[Any]) {
        log(INFO, "Downloading")
        receive {
            case zipped: Array[Byte] => {
                log(DEBUG, "Receiving new and modified files")
                unzip(basePath, zipped)
            }
            case a: Any => log(SEVERE, "Unexpected protocol error. Received " + a)
        }
    }

    def act() {
        alive(port)
        register('filesync, self)
        val sender = waitClient
        loop {
            download(sender)
        }
    }
}

class SyncClient private(basePath: String, server: AbstractActor, waitFor: () => Unit) extends Actor {

    def this(basePath: String, serverIp: String, port: Int, monitor: String) =
        this (basePath, select(Node(serverIp, port), 'filesync), MonitorParser.parseMonitor(basePath, monitor))

    private def sayHello() {
        log(INFO, "Greeting server.")
        server ! ('ping)
        receive {
            case ('pong) => {
                log(INFO, "Server answered. Connection established.")
            }
        }
    }

    private def upload(files: List[File]) {
        log(INFO, "Uploading")
        server ! zip(basePath, files)
    }

    def act() {
        val gitFacade = new GitFacade(new File(basePath + File.separator + ".git"))
        sayHello()
        loop {
            val changedFiles: JList[File] = gitFacade.findChanges
            if (!changedFiles.isEmpty) {
                val files = new Array[File](changedFiles.size)
                for (val i <- 0 to (changedFiles.size - 1)) {
                    files(i) = changedFiles.get(i)
                }
                upload(files.toList)
            }
            waitFor()
        }
    }
}