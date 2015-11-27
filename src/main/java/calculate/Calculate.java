package calculate;

import utils.MapReplacementEngine;
import utils.ReplacementHandler;
import java.io.*;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Calculate {

    protected int BUFFER = 1024 * 30;
    protected static List<File> pathsToZipArchives = new LinkedList<>();
    protected static List<File> pathsToGzipArchives = new LinkedList<>();
    protected static List<File> pathsToUnpackedFiles = new LinkedList<>();
    protected static List<File> pathToDirectories = new LinkedList<>();
    protected static final String ERROR_DIRECTIONS = " To succeed, please fix directory permissions " +
            "for the file/directory, or try to move file/folder to another directory and run application again\n";
    private Set<String> phones = new TreeSet<>();
    private Set<String> emails = new TreeSet<>();
    private static String parentDirectory;

    public void setParentDirectory(String zipFile) throws IOException {
        parentDirectory = zipFile.substring(0, zipFile.length() - 4);
    }

    public File getParentDirectory() {
        return new File(parentDirectory);
    }

    public void deleteArchivesAndGetPureFiles() throws IOException {
        for (File currentZip : pathsToZipArchives) {
            Files.delete(currentZip.toPath());
        }
        for (File currentGzip : pathsToGzipArchives) {
            Files.delete(currentGzip.toPath());
        }
        listUnpackedFiles(getParentDirectory());

    }

    // Optimized parcer replaces phone codes and writes derived phones/emails into collection.
    public void replaceAreaCodes() throws IOException {
        for (File currentFile : pathsToUnpackedFiles) {
            try (
                BufferedReader reader = new BufferedReader(new ReplacementHandler(new FileReader
                        (new File(currentFile.getPath())), replacementInitialization()), BUFFER);
                PrintWriter rawFileWriter = new PrintWriter(new BufferedWriter(new FileWriter
                        (currentFile + ".temp"), BUFFER))
        ) {
            // in case you want to exclude area codes without phone number itself, use the pattern below
            //Pattern patternPhone = Pattern.compile("\\+.{2,5}\\(\\d{1,4}\\).{1,}?(?=(\\s.{1,9}@)|$)");
            Pattern patternPhone = Pattern.compile("\\+.{2,5}\\(\\d{1,4}\\).{0,}?(?=(\\s.{1,9}@)|$)");
            Pattern patternEmail = Pattern.compile("[a-z0-9.]{1,}@[a-z0-9.]{1,}.org");
            Matcher matcherPhone = patternPhone.matcher("");
            Matcher matcherEmail = patternEmail.matcher("");
            LineNumberReader lineReader = new LineNumberReader(reader);
            for (String line = lineReader.readLine(); line != null; line = lineReader.readLine()) {
                rawFileWriter.println(line);
                matcherPhone.reset(line);
                matcherEmail.reset(line);
                if (matcherPhone.find()) {
                    phones.add(matcherPhone.group().replaceAll("-", "").replaceAll("\\s", ""));
                }
                while (matcherEmail.find()) {
                    emails.add(matcherEmail.group());}
                }
            }
        }
    }

    public PackArchives cleanUp() throws IOException {
        pathsToUnpackedFiles.clear();
        listUnpackedFiles(getParentDirectory());
        extractUserData();
        clearTemp();
        return new PackArchives();
    }

    protected void deleteDirectoryWithContent(File currentElement, boolean parseName) throws IOException {
        Path path;
        if (parseName) {
            String newPath = (currentElement.getPath().substring(0, currentElement.getPath().length() - 4));
            path = Paths.get(newPath);
        } else path = currentElement.toPath();
        Files.walkFileTree(path,new SimpleFileVisitor<Path>()    {
            @Override
            public FileVisitResult visitFile (Path file, BasicFileAttributes attributes)throws IOException {
            Files.delete(file);
            return FileVisitResult.CONTINUE;
            }
            @Override
            public FileVisitResult postVisitDirectory (Path directory, IOException e) throws IOException {
            Files.delete(directory);
            return FileVisitResult.CONTINUE;
            }
        });
    }

    protected void readStream(InputStream inputStream, OutputStream fileOutput) throws IOException {
        try (
                BufferedOutputStream outputStream = new BufferedOutputStream(fileOutput, BUFFER)
        ) {
            int currentCharOrByte;
            byte data[] = new byte[BUFFER];
            while ((currentCharOrByte = inputStream.read(data, 0, BUFFER)) != -1) {
                outputStream.write(data, 0, currentCharOrByte);}
        }
    }

    private void listUnpackedFiles(final File folder) throws NullPointerException {
        File[] files = folder.listFiles();
        assert files != null;
        for (File fileEntry : files) {
            if (fileEntry.isDirectory()) {
                listUnpackedFiles(fileEntry);
            } else {
                pathsToUnpackedFiles.add(fileEntry);
            }
        }
    }

    private void extractUserData() throws IOException {
        try (
                PrintWriter phonesWriter = new PrintWriter(new BufferedWriter(new FileWriter
                        ((getParentDirectory() + "\\phones.txt.temp")), BUFFER));
                PrintWriter emailWriter = new PrintWriter(new BufferedWriter(new FileWriter
                        ((getParentDirectory() + "\\emails.txt.temp")), BUFFER))
        ) {
            Iterator<String> phonesIt = phones.iterator();
            Iterator<String> emailsIt = emails.iterator();
            while (phonesIt.hasNext()){
                phonesWriter.println(phonesIt.next());
            }
            while (emailsIt.hasNext()){
                emailWriter.println(emailsIt.next());
            }
        }
        pathsToUnpackedFiles.add(new File(getParentDirectory() + "\\phones.txt.temp"));
        pathsToUnpackedFiles.add(new File(getParentDirectory() + "\\emails.txt.temp"));
    }

    private void clearTemp() throws IOException {
        for (File currentFile : pathsToUnpackedFiles) {
            if (!currentFile.getName().endsWith(".temp")) {
                if (!currentFile.delete()) {
                    throw new IOException("Cannot remove file '" + currentFile.getName() + "'\n" + ERROR_DIRECTIONS);
                }
            } else {
                File newFile = new File(currentFile.getCanonicalPath().replace(".temp", ""));
                if (!currentFile.renameTo(newFile)) {
                    throw new IOException("Cannot rename file '" + currentFile.getName() + "'\n" + ERROR_DIRECTIONS);
                }
            }
        }
    }

    private MapReplacementEngine replacementInitialization() throws IOException{
        Map<String, String> replacementKey = new HashMap<>();
        replacementKey.put("101", "(401)");
        replacementKey.put("202", "(802)");
        replacementKey.put("301", "(321)");
        return new MapReplacementEngine(replacementKey);
    }
}