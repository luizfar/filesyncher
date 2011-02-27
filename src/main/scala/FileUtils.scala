package co.torri.filesyncher

import java.io.{File, FileFilter}
import java.util.zip.{ZipOutputStream, ZipInputStream, ZipEntry}
import java.io.{InputStream, OutputStream, FileInputStream, FileOutputStream, ByteArrayOutputStream, ByteArrayInputStream}
import java.security.MessageDigest
import co.torri.filesyncher.FileUtils._


object FileStatus extends Enumeration {
   val DELETED = Value('D')
   val ADDED = Value('A')
   val MODIFIED = Value('M')
   val SAME = Value('S')
}


object FileUtils {
    
    implicit def string2File(str: String) = new File(str)
    
    def recursiveListTree(f: File): Array[File] = {
        val these = f.listFiles
        these ++ these.filter(_.isDirectory).flatMap(recursiveListTree)
    }
    
    def recursiveListFiles(path: String, filter: FileFilter = AcceptAllFileFilter): List[File] = recursiveListTree(path).filter(f => f.isFile && filter.accept(f)).toList
    
    def filehash(f: File) = {
        var digest = MessageDigest.getInstance("MD5")
        digest.digest(content(f)).map(_.asInstanceOf[Int]).sum
    }
    
    def content(f: File) = {
        var fin = new FileInputStream(f)
        var bout = new ByteArrayOutputStream
        streamcopy(fin, bout)
        fin.close
        bout.toByteArray
    }
    
    def delete(files: List[File]) {
        files.foreach(_.delete)
    }
    
    def zip(basepath: String, files: List[File]): Array[Byte] = {
        if (files.size == 0) return Array[Byte]()
        var buf = Array.ofDim[Byte](1024)
        var byteout = new ByteArrayOutputStream
        var out = new ZipOutputStream(byteout)
        files.foreach { f =>
            debug("zip: " + f.getAbsolutePath)
            var in = new FileInputStream(f)
            out.putNextEntry(new ZipEntry(f.toString.replace(basepath, "")))
            
            streamcopy(in, out, buf)
            
            out.closeEntry()
            in.close()
        }
        out.close
        byteout.toByteArray
    }
    
    def unzip(dest: File, zip: Array[Byte]) {
        require(dest.isDirectory)
        var buf = Array.ofDim[Byte](1024)
        var zipinputstream = new ZipInputStream(new ByteArrayInputStream(zip))
        
        var entry = zipinputstream.getNextEntry
        while (entry != null) {
            var f = new File(dest.getAbsolutePath + File.separator + fixpath(entry.getName))
            f.getParentFile.mkdirs
            if (!f.exists) f.createNewFile
            debug("unzip: " + f.getAbsolutePath)
            var fout = new FileOutputStream(f)
            streamcopy(zipinputstream, fout, buf)
            fout.close
            zipinputstream.closeEntry
            
            entry = zipinputstream.getNextEntry
        }
        zipinputstream.close
    }
    
    def fixpath(path: String) = path.replace("/", File.separator).replace("""\""", File.separator)
    
    def streamcopy(in: InputStream, out: OutputStream, buf: Array[Byte] = Array.ofDim[Byte](1024)) {
        var len = 0
        do {
            len = in.read(buf)
            if (len > 0) out.write(buf, 0, len)
        } while (len > 0)
    }
    
    def getFileFilter(includeOnly: String, exclude: String) = (includeOnly, exclude) match {
        case (null, null) | ("", "") | (null, "") | ("", null) => AcceptAllFileFilter
        case (null, _) => new IncludeOrExludeFileFilter("", exclude)
        case (_, null) => new IncludeOrExludeFileFilter(includeOnly, "")
        case _ => new IncludeOrExludeFileFilter(includeOnly, exclude)
    }
}


class FilesWatcher(path: String, filter: FileFilter, poltime: Long = 5000) {
    
    var filestimestamp = getLastFileList
    
    def waitchange {
        var noChanges = true
        while({noChanges = noneAddedOrRemoved(getLastFileList); noChanges}) {
            Thread.sleep(poltime)
        }
    }
    
    private def noneAddedOrRemoved(newFiles: Map[File, Long]) = {
        filestimestamp.keys == newFiles.keys &&
        filestimestamp.filter(p => p._2 != newFiles(p._1)).isEmpty
    }
    
    private def getLastFileList = recursiveListFiles(path, filter).map(f => (f, f.lastModified)).toMap
}

class IncludeOrExludeFileFilter(includeOnly: String, exclude: String) extends FileFilter {
    
    def accept(f: File) = {
        if (includeOnly != "") {
            includeOnly.split(";").map(r => f.getAbsolutePath.matches(toRegex(r))).reduceLeft(_||_)
        } else {
            !exclude.split(";").map(r => f.getAbsolutePath.matches(toRegex(r))).reduceLeft(_||_)
        }
    }
    
    private def toRegex(str: String) = str.replace(".", "\\.").replace("*", ".*")
    
    override def toString = (includeOnly, exclude).toString
}

object AcceptAllFileFilter extends FileFilter {
    def accept(f: File) = true
    override def toString = "(,)"
}