package me.yugy.app.common.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class FileUtils {

    public static void copy(File src, File dst) throws IOException {
        File directory = dst.getParentFile();
        if (directory != null && !directory.exists() && !directory.mkdirs()) {
            throw new IOException("Cannot create dir " + directory.getAbsolutePath());
        }
        if (!dst.exists() && !dst.createNewFile()){
            throw new IOException("Cannot create file " + dst.getName());
        }

        InputStream in = new FileInputStream(src);
        OutputStream out = new FileOutputStream(dst);

        // Copy the bits from instream to outstream
        byte[] buf = new byte[1024];
        int len;
        while ((len = in.read(buf)) > 0) {
            out.write(buf, 0, len);
        }
        in.close();
        out.close();
    }

    public static String getFileExtension(File file){
        if(file.isFile()){
            String[] array = file.getName().split("\\.");
            return array[array.length - 1];
        }
        return "";
    }

}
