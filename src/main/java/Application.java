import calculate.Calculate;
import calculate.PackArchives;
import calculate.UnpackArchives;

import java.io.*;
import java.util.Date;

public class Application {

    private static final String ERROR_ARGS = " Error! You have to define input file, following the pattern:\n" +
            "java -jar [application].jar [name].zip";
    private static final String ERROR_FILE = " Process was not completed due to an error. Please try any of the following:\n" +
            "\t1. Check specified path or integrity of given .zip file\n" +
            "\t2. Fix file/directory permissions\n" +
            "\t3. Try to move file/folder to another directory and run application again\n";

    public static void main(String args[]) {
        if (args.length == 1) {
            String inputFile = args[0];
            new Application().execute(inputFile);
        } else {
            System.out.println(ERROR_ARGS);
        }
        System.exit(0);
    }

    public void execute(String inputFileName) {
        try {
           Calculate repackArchive = new Calculate();
           repackArchive.setParentDirectory(inputFileName);
           UnpackArchives unpack = new UnpackArchives();
           unpack.unpackZip(inputFileName);
           System.out.println(" Zip archives unpacking complete.");
           unpack.unpackGzip();
           System.out.println(" Gzip archives unpacking complete.");
           repackArchive.deleteArchivesAndGetPureFiles();
           System.out.println(" Data manipulation initiated: replace area numbers codes, extract phones and emails.");
           repackArchive.replaceAreaCodes();
           System.out.println(" Data was modified, required phones and email extracted successfully.");
           repackArchive.cleanUp().packGzipFiles();
           System.out.println("\n Repacking complete!\n The path to modified archive is: " + repackArchive.getParentDirectory() + "v2.zip");
       }
        catch (IOException e) {
            System.out.println("Repacking failed.\n" + ERROR_FILE);
            e.printStackTrace();
        }
    }
}