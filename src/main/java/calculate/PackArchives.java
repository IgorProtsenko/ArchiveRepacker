package calculate;

import java.io.*;
import java.util.zip.GZIPOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class PackArchives extends Calculate {

    public void packGzipFiles() throws IOException {
        for (File currentGzip : pathsToGzipArchives) {
            {
                String newPath = currentGzip.getPath().substring(0, currentGzip.getPath().length() - 3);
                try (
                        FileInputStream fileInputStream = new FileInputStream(newPath);
                        FileOutputStream fos = new FileOutputStream(newPath + ".gz");
                        GZIPOutputStream gzipOS = new GZIPOutputStream(fos)
                ) {
                    readStream(fileInputStream, gzipOS);
                    fileInputStream.close();
                    deleteDirectoryWithContent(new File(newPath), false);
                } catch (IOException e) {
                    e.printStackTrace();}
            }
        }
        packZipFiles();
    }

    private void packZipFiles() {
        for (int i = pathsToZipArchives.size() - 1; i > -1; i--) {
            String newPath = pathsToZipArchives.get(i).getPath().substring(0, pathsToZipArchives.get(i).getPath().length() - 4);
            try {
                packParentFolder(newPath, pathsToZipArchives.get(i).getPath());
                deleteDirectoryWithContent(pathsToZipArchives.get(i), true);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        finalPack();
    }

    private void finalPack() {
        try {
            packParentFolder(getParentDirectory().getPath(), getParentDirectory() + "v2.zip");
            deleteDirectoryWithContent(getParentDirectory(), false);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void packParentFolder(String srcFolder, String destinationZipFile) throws Exception {
        try (
                FileOutputStream fileWriter = new FileOutputStream(destinationZipFile);
                ZipOutputStream zipOutputStream = new ZipOutputStream(fileWriter)
        ) {
            packZipFolder("", srcFolder, zipOutputStream);
            zipOutputStream.flush();
        }
    }

    private void packZipFile(String path, String currentFile, ZipOutputStream outputStream, boolean flag) throws Exception {
            File folder = new File(currentFile);
            if (flag) {
                outputStream.putNextEntry(new ZipEntry(path + "/" + folder.getName() + "/"));
            } else {
                if (folder.isDirectory()) {
                    packZipFolder(path, currentFile, outputStream);
                } else {
                    byte[] currentByte = new byte[BUFFER];
                    int length;
                    BufferedInputStream inputStream = new BufferedInputStream(new FileInputStream(currentFile));
                    outputStream.putNextEntry(new ZipEntry(path + "/" + folder.getName()));
                    while ((length = inputStream.read(currentByte,0 , BUFFER)) != -1) {
                        outputStream.write(currentByte, 0, length);}
                    }
            }
    }

    private void packZipFolder(String path, String srcFolder, ZipOutputStream zipOutputStream) throws Exception {
        File folder = new File(srcFolder);
        if (folder.list().length == 0) {
            packZipFile(path, srcFolder, zipOutputStream, true);
        } else {
            for (String fileName : folder.list()) {
                if (path.equals("")) {
                    packZipFile(folder.getName(), srcFolder + "/" + fileName, zipOutputStream, false);
                } else {
                    packZipFile(path + "/" + folder.getName(), srcFolder + "/" + fileName, zipOutputStream, false);
                }
            }
        }
    }
}