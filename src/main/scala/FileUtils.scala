package co.torri.filesyncher

import scala.tools.nsc.io.File
import java.util.zip.{ZipOutputStream, ZipInputStream, ZipEntry}
import java.security.MessageDigest
import co.torri.filesyncher.{Log => log}
import co.torri.filesyncher.LogLevel._
import scala.annotation.tailrec
import com.thoughtworks.syngit.Diff
import com.thoughtworks.syngit.git.GitFacade
import java.io.{ByteArrayOutputStream, File => JFile, InputStream, OutputStream, FileInputStream, FileOutputStream, ByteArrayInputStream}

object FileStatus extends Enumeration {
   val DELETED = Value('D')
   val ADDED = Value('A')
   val MODIFIED = Value('M')
   val SAME = Value('S')
}

object FileUtils {
    
    implicit def string2File(str: String) = new JFile(str)
    
    def zip(basePath: String, diff: Diff): Array[Byte] = {
        val buf = Array.ofDim[Byte](1024)
        val byteOut = new ByteArrayOutputStream
        val out = new ZipOutputStream(byteOut)

        out.putNextEntry(new ZipEntry("_cached_diff_"))
        out.write(diff.getCachedPatch.getBytes)
        out.putNextEntry(new ZipEntry("_diff_"))
        out.write(diff.getPatch.getBytes)

        for (val i <- 0 to (diff.getNewFiles.size - 1)) {
            val file = diff.getNewFiles.get(i)
            if (file.exists) {
                log(FILEOP, "zip: " + file.getAbsolutePath)
                val in = new FileInputStream(file)
                out.putNextEntry(new ZipEntry("_untracked_" + file.toString.replace(basePath, "")))
                streamCopy(in, out, buf)
                out.closeEntry()
                in.close()
            }
        }

        out.close()
        byteOut.toByteArray
    }
    
    def applyZippedDiff(dest: JFile, zip: Array[Byte]) {
        require(dest.isDirectory)
        val zipInputStream = new ZipInputStream(new ByteArrayInputStream(zip))

        val git = new GitFacade(dest + File.separator + ".git")
        git.clean()

        var entry = zipInputStream.getNextEntry
        while (entry != null) {
            entry.getName match {
                case "_cached_diff_" =>
                    git.applyCachedPatch(readToString(zipInputStream))

                case "_diff_" =>
                    git.applyPatch(readToString(zipInputStream))

                case name: String =>
                    readToFile(zipInputStream, dest.getAbsolutePath, name.replace("_untracked_", ""))
            }
            entry = zipInputStream.getNextEntry
        }
        zipInputStream.close()
    }

    private def readToString(input: ZipInputStream): String = {
        val buf = Array.ofDim[Byte](1024)
        val out = new ByteArrayOutputStream()
        streamCopy(input, out, buf)

        val result = new String(out.toByteArray)
        out.close()

        result
    }

    private def readToFile(input: ZipInputStream, path: String, name: String) {
        val buf = Array.ofDim[Byte](1024)

        val file = new JFile(path, fixPath(name))
        file.getParentFile.mkdirs
        if (!file.exists) file.createNewFile

        log(FILEOP, "unzip: " + file.getAbsolutePath)
        val out = new FileOutputStream(file)
        streamCopy(input, out, buf)

        out.close()
        input.closeEntry()
    }
    
    def fileHash(f: JFile) = MessageDigest.getInstance("MD5").digest(content(f)).map(_.asInstanceOf[Int]).sum

    def content(f: JFile) = try { new File(f).bytes.toArray } catch { case _ => Array[Byte]() }

    def fixPath(path: String) = path.replace("/", JFile.separator).replace("""\""", JFile.separator)
    
    private def streamCopy(in: InputStream, out: OutputStream, buf: Array[Byte] = Array.ofDim[Byte](1024)) {
        @tailrec def read(len: Int): Unit = if (len > 0) {
            out.write(buf, 0, len)
            read(in.read(buf))
        }
        read(in.read(buf))
    }
}