package main;

import java.io.*;
import java.util.Objects;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

public class Loader {
    private static final int BUFFER_SIZE = 1024;
    private static final int COMPRESSION_LEVEL = 9;

    /**
     * Entry point to program.
     * At the entrance can take files or/and directories and add them to archive.
     * Or, if args is empty, take input stream from archive and extract files.
     *
     * @param args - files or/and directories to add to the archive.
     */
    public static void main(String[] args) {
        if (args.length != 0) {
            try (ZipOutputStream zos = new ZipOutputStream(System.out)) {
                zos.setLevel(COMPRESSION_LEVEL);
                for (String path : args) {
                    doZip(new File(path), zos);
                }
                zos.finish();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            try (ZipInputStream zis = new ZipInputStream(System.in)) {
                File unzipDirectory = new File(".").getCanonicalFile();
                doUnzip(unzipDirectory, zis);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * This method extracts files from archive.
     *
     * @param unzipDirectory - directory to extract files.
     * @param zis            - zip input stream.
     * @throws IOException - can throw I/O Exception.
     */
    static void doUnzip(File unzipDirectory, ZipInputStream zis) throws IOException {
        ZipEntry zipEntry = zis.getNextEntry();
        while (zipEntry != null) {
            File newFile = newFile(unzipDirectory, zipEntry);
            try (FileOutputStream fos = new FileOutputStream(newFile)) {
                write(zis, fos);
            } catch (IOException e) {
                e.printStackTrace();
            }
            zipEntry = zis.getNextEntry();
        }
        zis.closeEntry();
    }

    /**
     * This method adds files to archive.
     *
     * @param file - file to add.
     * @param out  - zip output stream.
     */
    static void doZip(File file, ZipOutputStream out) {
        if (file.isDirectory()) {
            for (File innerFile : Objects.requireNonNull(file.listFiles())) {
                doZip(innerFile, out);
            }
        } else {
            try (FileInputStream fis = new FileInputStream(file)) {
                out.putNextEntry(new ZipEntry(file.getPath()));
                write(fis, out);
                out.closeEntry();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * This method write bytes from input stream to output stream.
     *
     * @param in  - input stream.
     * @param out - output stream.
     * @throws IOException - can throw I/O Exception.
     */
    private static void write(InputStream in, OutputStream out) throws IOException {
        byte[] buffer = new byte[BUFFER_SIZE];
        int length;
        while ((length = in.read(buffer)) >= 0) {
            out.write(buffer, 0, length);
        }
    }

    /**
     * This method created new file in unzip directory from an archive entry.
     *
     * @param destinationDir - unzip directory.
     * @param zipEntry       - archive entry.
     * @return - return new file.
     * @throws IOException - if entry is outside of the target dir or directory not created.
     */
    private static File newFile(File destinationDir, ZipEntry zipEntry) throws IOException {
        File destFile = new File(destinationDir, zipEntry.getName());

        String destDirPath = destinationDir.getCanonicalPath();
        String destFilePath = destFile.getCanonicalPath();

        if (!destFilePath.startsWith(destDirPath + File.separator)) {
            throw new IOException("Entry is outside of the target dir: " + zipEntry.getName());
        }
        if (!destFile.getParent().equals(destDirPath)
                && !destFile.getParentFile().exists()
                && !destFile.getParentFile().mkdirs()) {
            throw new IOException("Directory not created: " + destFile.getParentFile());
        }
        return destFile;
    }
}