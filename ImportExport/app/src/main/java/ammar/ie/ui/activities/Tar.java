package ammar.ie.ui.activities;

import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.archivers.tar.TarArchiveOutputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorOutputStream;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.filefilter.DirectoryFileFilter;
import org.apache.commons.io.filefilter.RegexFileFilter;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

public class Tar {

    public static void CreateTarGZ(String inputDirectoryPath, String outputPath) throws IOException {

        File inputFile = new File(inputDirectoryPath);
        File outputFile = new File(outputPath);

        try (FileOutputStream fileOutputStream = new FileOutputStream(outputFile);
             BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(fileOutputStream);
             GzipCompressorOutputStream gzipOutputStream = new GzipCompressorOutputStream(bufferedOutputStream);
             TarArchiveOutputStream tarArchiveOutputStream = new TarArchiveOutputStream(gzipOutputStream)) {

            tarArchiveOutputStream.setBigNumberMode(TarArchiveOutputStream.BIGNUMBER_POSIX);
            tarArchiveOutputStream.setLongFileMode(TarArchiveOutputStream.LONGFILE_GNU);

            List<File> files = new ArrayList<>(FileUtils.listFiles(
                    inputFile,
                    new RegexFileFilter("^(.*?)"),
                    DirectoryFileFilter.DIRECTORY
            ));

            for (int i = 0; i < files.size(); i++) {
                File currentFile = files.get(i);

                String relativeFilePath = inputFile.toURI().relativize(
                        new File(currentFile.getAbsolutePath()).toURI()).getPath();

                TarArchiveEntry tarEntry = new TarArchiveEntry(currentFile, relativeFilePath);
                tarEntry.setSize(currentFile.length());

                tarArchiveOutputStream.putArchiveEntry(tarEntry);
                tarArchiveOutputStream.write(IOUtils.toByteArray(new FileInputStream(currentFile)));
                tarArchiveOutputStream.closeArchiveEntry();
            }
            tarArchiveOutputStream.close();
        }
    }

    public static void decompress(File src, File dest) throws IOException {
        InputStream fi = new FileInputStream(src);
        decompress(fi, dest);
    }

    public static void decompress(InputStream fi, File dest) throws IOException {
        try (
                BufferedInputStream bi = new BufferedInputStream(fi);
                GzipCompressorInputStream gzi = new GzipCompressorInputStream(bi);
                TarArchiveInputStream ti = new TarArchiveInputStream(gzi)) {

            ArchiveEntry entry;
            while ((entry = ti.getNextEntry()) != null) {
                if (!ti.canReadEntryData(entry)) {
                    // log something?
                    continue;
                }
                //String name = fileName(src, entry);

                File f = new File(dest, entry.getName());
                if (entry.isDirectory()) {
                    if (!f.isDirectory() && !f.mkdirs()) {
                        throw new IOException("failed to create directory " + f);
                    }
                } else {
                    File parent = f.getParentFile();
                    if (parent != null && !parent.isDirectory() && !parent.mkdirs()) {
                        throw new IOException("failed to create directory " + parent);
                    }
                    try (OutputStream o = new FileOutputStream(f.getPath())) {
                        IOUtils.copy(ti, o);
                    }
                }
            }
        }
    }
    /*public static void decompressTarGzipFile(Path source, Path target)
            throws IOException {

        if (Files.notExists(source)) {
            throw new IOException("File doesn't exists!");
        }

        try (InputStream fi = Files.newInputStream(source);
             BufferedInputStream bi = new BufferedInputStream(fi);
             GzipCompressorInputStream gzi = new GzipCompressorInputStream(bi);
             TarArchiveInputStream ti = new TarArchiveInputStream(gzi)) {

            ArchiveEntry entry;
            while ((entry = ti.getNextEntry()) != null) {

                Path newPath = zipSlipProtect(entry, target);

                if (entry.isDirectory()) {
                    Files.createDirectories(newPath);
                } else {

                    // check parent folder again
                    Path parent = newPath.getParent();
                    if (parent != null) {
                        if (Files.notExists(parent)) {
                            Files.createDirectories(parent);
                        }
                    }

                    // copy TarArchiveInputStream to Path newPath
                    Files.copy(ti, newPath, StandardCopyOption.REPLACE_EXISTING);

                }
            }
        }
    }*/

    /*private static Path zipSlipProtect(ArchiveEntry entry, Path targetDir)
            throws IOException {

        Path targetDirResolved = targetDir.resolve(entry.getName());

        Path normalizePath = targetDirResolved.normalize();

        if (!normalizePath.startsWith(targetDir)) {
            throw new IOException("Bad entry: " + entry.getName());
        }

        return normalizePath;
    }*/
}