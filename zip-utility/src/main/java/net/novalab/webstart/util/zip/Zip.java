package net.novalab.webstart.util.zip;

import java.io.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class Zip {
    private static Logger LOGGER = Logger.getLogger(Zip.class.getName());

    private File root;
    private boolean includeRoot;

    public Zip(String path) {
        root = new File(path);
        if (!root.isDirectory()) {
            LOGGER.log(Level.WARNING, root.getAbsolutePath() + " is not a directory");
        }
        includeRoot = true;
    }

    public Zip doNotIncludeSource() {
        includeRoot = false;
        return this;
    }

    public boolean compress() throws IOException {
        return compress(root.getName() + ".zip");
    }

    public boolean compress(String target) throws IOException {
        if (!target.endsWith(".zip")) {
            throw new IllegalArgumentException("File name must end with .zip");
        }
        if (!root.isDirectory()) {
            return false;
        }

        File targetFile = root.getParentFile().toPath().resolve(new File(target).toPath()).toFile();
        LOGGER.log(Level.INFO, "Creating zip file: " + targetFile.getAbsolutePath());

        try (ZipOutputStream zos = new ZipOutputStream(new BufferedOutputStream(
                new FileOutputStream(targetFile)))) {
            try {
                processDirectory(root, zos);
                zos.flush();
            } catch (IOException e) {
                LOGGER.log(Level.SEVERE, "An error occured during compression", e);
                zos.close();
                targetFile.delete();
                return false;
            }
        }
        LOGGER.log(Level.INFO, "Completed creating zip file: " + targetFile.getAbsolutePath());

        return true;
    }

    public void processDirectory(File folder, ZipOutputStream zos) throws IOException {
        for (File file : folder.listFiles()) {
            if (file.isDirectory()) {
                processDirectory(file, zos);
            } else {
                ZipEntry entry = new ZipEntry(entryName(file));
                zos.putNextEntry(entry);
                try (FileInputStream fis = new FileInputStream(file)) {
                    byte[] bytes = new byte[1024];
                    int length;
                    while ((length = fis.read(bytes)) >= 0) {
                        zos.write(bytes, 0, length);
                    }
                    zos.closeEntry();
                }
            }
        }
    }

    private String entryName(File file) {
        return (includeRoot ? root.getParentFile().toURI().relativize(file.toURI()) :
                root.toURI().relativize(file.toURI())).toString();
    }

    public static void main(String[] args) throws IOException {
        if (args.length > 0) {
            String source = args[0];
            new Zip(source).compress();
        } else {
            LOGGER.log(Level.WARNING, "No source is specified");
        }
    }
}
