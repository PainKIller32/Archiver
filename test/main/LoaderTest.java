package main;

import org.junit.jupiter.api.Test;

import java.io.*;
import java.nio.file.Files;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import static org.junit.jupiter.api.Assertions.*;

class LoaderTest {

    @Test
    void doZipAndUnzip() {
        String testFileName = "test.txt";
        String archiveName = "archive.zip";
        File originFile = new File(testFileName);
        String testValue = "test value in archived file";
        try {
            try (FileOutputStream fos = new FileOutputStream(originFile)) {
                fos.write(testValue.getBytes());
            }

            File zipFile = new File(archiveName);
            try (ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(zipFile))) {
                Loader.doZip(originFile, zos);
                zos.finish();
            }
            Files.delete(originFile.toPath());

            try (ZipInputStream zis = new ZipInputStream(new FileInputStream(zipFile))) {
                Loader.doUnzip(new File("."), zis);
            }
            Files.delete(zipFile.toPath());

            File unzipFile = new File(testFileName);
            String result;
            try (FileInputStream fis = new FileInputStream(unzipFile)) {
                result = new String(fis.readAllBytes());
            }
            Files.delete(unzipFile.toPath());

            assertEquals(testValue, result);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}