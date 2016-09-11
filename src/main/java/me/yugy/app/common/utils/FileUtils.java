package me.yugy.app.common.utils;

import android.support.annotation.WorkerThread;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

@SuppressWarnings("unused")
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

    /**
     * Get total size of a directory
     * @return return directory size in bytes, return 0 if file is null or not a directory or directory is empty.
     */
    @WorkerThread
    public static long getFileSize(File file) {
        if (file == null) {
            return 0L;
        }
        if (!file.isDirectory()) {
            return file.length();
        }
        long totalSize = 0L;
        try {
            File[] files = file.listFiles();
            for (File f : files) {
                if (f.isDirectory()) {
                    totalSize += getFileSize(f);
                } else {
                    totalSize += f.length();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return totalSize;
    }

    public static boolean deleteFilesByDirectory(File dir) {
        if (dir == null) {
            return false;
        }
        if (dir.isDirectory()) {
            String[] children = dir.list();
            for (String aChildren : children) {
                boolean success = deleteFilesByDirectory(new File(dir, aChildren));
                if (!success) {
                    return false;
                }
            }
        }
        return dir.delete();
    }

}
