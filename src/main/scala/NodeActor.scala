package co.torri.filesyncher

import scala.actors._
import scala.actors.Actor._
import scala.actors.remote._
import scala.actors.remote.RemoteActor._
import java.io.{File, FileFilter}
import co.torri.filesyncher.FileUtils._
import co.torri.filesyncher.BaseActs._
import co.torri.filesyncher.{Log => log}
import co.torri.filesyncher.LogLevel._
import com.thoughtworks.syngit.git.GitFacade
import java.util.{List => JList}
import scala.collection.JavaConversions._

abstract class BaseActs(basePath: String) extends Actor {
    protected def sayHello(server: OutputChannel[Any]) = {
        log(INFO, "Greeting server")
        server ! ('hello)
    }

    protected def waitClient: OutputChannel[Any] = {
        log(INFO, "Waiting client")
        receive {
            case ('hello) => {
                log(INFO, "Client said 'hello'")
                return sender
            }
        }
    }

    protected def upload(files: List[File]) = {
        log(INFO, "Uploading")
        sender ! zip(basePath, files)
    }

    protected def download(server: OutputChannel[Any]) {
        log(INFO, "Downloading")
        receive {
            case zipped: Array[Byte] => {
                log(DEBUG, "Receiving new and modified files")
                unzip(basePath, zipped)
            }
            case a: Any => log(SEVERE, "Unexpected protocol error. Received " + a)
        }
    }

}
object BaseActs {
    private val TimeMonitorRE = """(\d+)\s*(\w)""".r
    private val FileChangeMonitorRE = """filechange\s*\((\d+)\)""".r

    def parseMonitor(basePath: String, monitor: String): () => Unit = monitor match {
        case TimeMonitorRE(time, "s") => {() => Thread.sleep(time.toInt * 1000)}
        case TimeMonitorRE(time, "m") => {() => Thread.sleep(time.toInt * 1000 * 60)}
        case TimeMonitorRE(time, "h") => {() => Thread.sleep(time.toInt * 1000 * 60 * 60)}
        case FileChangeMonitorRE(pollTime) => val watcher = new FilesWatcher(basePath, pollTime.toInt); {() => watcher.waitchange}
    }
}

class SyncServer(basePath: String, port: Int, monitor: String) extends BaseActs(basePath) {
    def act {
        alive(port)
        register('filesync, self)
        val waitFor = parseMonitor(basePath, monitor)

        loop {
            val sender = waitClient
            download(sender)
            waitFor()
        }

    }
}

class SyncClient private(basePath: String, server: AbstractActor, waitFor: () => Unit) extends BaseActs(basePath) {

    def this(basePath: String, serverIp: String, port: Int, monitor: String) =
        this (basePath, select(Node(serverIp, port), 'filesync), parseMonitor(basePath, monitor))

    def act {
        val gitFacade = new GitFacade(new File(basePath + File.separator + ".git"))
        loop {
            sayHello(server)
            val changedFiles: JList[File] = gitFacade.findChanges
            if (!changedFiles.isEmpty) {
                val files = new Array[File](changedFiles.size)
                for (val i <- 0 to changedFiles.size) {
                    files(i) = changedFiles.get(i)
                }
                upload(List.fromArray(files))
            }
            waitFor()
        }
    }
}