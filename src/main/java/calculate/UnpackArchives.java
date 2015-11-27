package calculate;

import org.apache.commons.io.FileUtils;
import java.io.*;
import java.util.Enumeration;
import java.util.zip.GZIPInputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class UnpackArchives extends Calculate {

    public void unpackZip(String zipFile) throws IOException {
        String newPath = zipFile.substring(0, zipFile.length() - 4);
        ZipFile file = new ZipFile(zipFile);
        FileUtils.forceMkdir(new File(newPath));
        Enumeration zipFileEntries = file.entries();
        while (zipFileEntries.hasMoreElements()) {
            ZipEntry entry = (ZipEntry) zipFileEntries.nextElement();
            String currentEntry = entry.getName();
            File originalFile = new File(newPath, currentEntry);
            if (!entry.isDirectory()) {
                try (
                        BufferedInputStream zipInputStream = new BufferedInputStream(file
                                .getInputStream(entry));
                        FileOutputStream fileOutput = new FileOutputStream(originalFile)
                ) {
                    readStream(zipInputStream, fileOutput);
                }
                if (currentEntry.endsWith(".gz")) {
                    pathsToGzipArchives.add(new File(newPath + "/" + currentEntry));
                }
                if (currentEntry.endsWith(".zip")) {
                    pathsToZipArchives.add(new File(newPath + "/" + currentEntry));
                    unpackZip(originalFile.getAbsolutePath());
                }
            } else {
                FileUtils.forceMkdir(new File(newPath, currentEntry));
                pathToDirectories.add(new File(newPath + "\\" + currentEntry));
           }
        }
        file.close();
    }

    public void unpackGzip() throws IOException {
        for (File archive : pathsToGzipArchives) {
            if (archive.toString().endsWith(".gz")) {
                File originalFile = new File(archive.getParent(), archive.getName().replaceAll("\\.gz$", ""));
                pathToDirectories.add(new File(archive.getParent()));
                try (
                        GZIPInputStream gzipInputStream = new GZIPInputStream(new FileInputStream(archive));
                        FileOutputStream fileOutput = new FileOutputStream(originalFile)
                ) {
                    readStream(gzipInputStream, fileOutput);
                }
            }
        }
    }
}
