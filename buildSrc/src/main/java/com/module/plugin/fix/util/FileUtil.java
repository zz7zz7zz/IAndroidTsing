package com.module.plugin.fix.util;


import org.apache.http.util.TextUtils;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.io.Serializable;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Enumeration;
import java.util.zip.GZIPInputStream;
import java.util.zip.Inflater;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;


/**
 * 文件操作类
 */
public class FileUtil {

    private static final int FILE_BUFFER_SIZE = 51200;

    private FileUtil() {
        /* cannot be instantiated */
        throw new UnsupportedOperationException("FileUtil cannot be instantiated");
    }

    public static void byte2File(byte[] buf, String filePath, String fileName) {
        BufferedOutputStream bos = null;
        FileOutputStream fos = null;
        File file = null;
        try {
            File dir = new File(filePath);
            if (!dir.exists() && dir.isDirectory()) {
                dir.mkdirs();
            }
            file = new File(filePath + File.separator + fileName);
            if (!file.exists()) {
                file.createNewFile();
            }
            fos = new FileOutputStream(file);
            bos = new BufferedOutputStream(fos);
            bos.write(buf);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (bos != null) {
                try {
                    bos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (fos != null) {
                try {
                    fos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * 转换文件大小
     */
    public static String FormetFileSize(long fileS) {
        String fileSizeString = "";
        if (fileS < 1024) {
            fileSizeString = formatSize((double) fileS) + "B";
        } else if (fileS < 1048576) {
            fileSizeString = formatSize((double) fileS / 1024) + "K";
        } else if (fileS < 1073741824) {
            fileSizeString = formatSize((double) fileS / 1048576) + "M";
        } else {
            fileSizeString = formatSize((double) fileS / 1073741824) + "G";
        }
        return fileSizeString;
    }

    /**
     * 保留两位小数
     */
    public static double formatSize(double size) {
        return (double) (Math.round(size * 100)) / 100;
    }

    /**
     * 判断文件是否存在
     */
    public static boolean fileIsExist(String filePath) {
        if (filePath == null || filePath.length() < 1) {
            return false;
        }
        File f = new File(filePath);
        return f.exists();
    }

    public static boolean fileIsExist(File file) {
        return file.exists() && file.isFile();
    }


    /**
     * 重命名文件
     */
    public static void renameFile(String oldPath, String newPath) {
        if (TextUtils.isEmpty(oldPath) || TextUtils.isEmpty(newPath)) {
            return;
        }
        File file = new File(oldPath);
        if (file.exists()) {
            file.renameTo(new File(newPath));
        } else {
        }
    }


    /******** 删除相关 ********/
    /**
     * 删除文件夹
     */
    public static boolean delete(File file) {
        if (file.isFile()) {
            return file.delete();
        }
        if (file.isDirectory()) {
            File[] childFiles = file.listFiles();
            if (childFiles == null || childFiles.length == 0) {
                return file.delete();
            }

            for (int i = 0; i < childFiles.length; i++) {
                delete(childFiles[i]);
            }
            return file.delete();
        }

        return false;
    }

    /**
     * 删除文件
     *
     * @param filePath 文件路径
     * @return 是否删除成功
     */
    public static boolean deleteWithPath(String filePath) {
        try {
            if (fileIsExist(filePath)) {
                File f = new File(filePath);
                return delete(f);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }

    /**
     * 删除指定文件夹，保留特定文件
     * @param filePath 文件夹目录
     * @param unDeleteFileName 不要删除的文件名
     */
    public static void deleteWithOut(String filePath, String unDeleteFileName) {
        String[] fileNames = new File(filePath).list();
        if (fileNames != null && fileNames.length > 0){
            for (String name : fileNames){
                if (!equals(name, unDeleteFileName)){
                   deleteWithPath(filePath + name);
                }
            }
        }
    }

    public static boolean equals(CharSequence a, CharSequence b) {
        if (a == b) return true;
        int length;
        if (a != null && b != null && (length = a.length()) == b.length()) {
            if (a instanceof String && b instanceof String) {
                return a.equals(b);
            } else {
                for (int i = 0; i < length; i++) {
                    if (a.charAt(i) != b.charAt(i)) return false;
                }
                return true;
            }
        }
        return false;
    }

//    /**
//     * 删除目录
//     */
//    public static boolean deleteDirectory(String filePath) {
//        if (null == filePath) {
//            return false;
//        }
//        File file = new File(filePath);
//        if (file == null || !file.exists()) {
//            return false;
//        }
//        if (file.isDirectory()) {
//            File[] list = file.listFiles();
//            for (int i = 0; i < list.length; i++) {
//                if (list[i].isDirectory()) {
//                    deleteWithPath(list[i].getAbsolutePath());
//                } else {
//                    list[i].delete();
//                }
//            }
//        }
//        file.delete();
//        return true;
//    }


    /******** 写入相关 ********/
    /**
     * 写文件
     *
     * @param filePath    文件路径
     * @param fileContent 文件内容
     * @return 是否写入成功
     */
    public static boolean writeFile(String filePath, String fileContent) {
        return writeFile(filePath, fileContent, false);
    }

    public static boolean writeFile(String filePath, String fileContent, boolean append) {
        if (null == filePath || fileContent == null || filePath.length() < 1 || fileContent.length() < 1) {
            return false;
        }
        try {
            File file = new File(filePath);
            if (!file.exists()) {
                if (!file.createNewFile()) {
                    return false;
                }
            }
            BufferedWriter output = new BufferedWriter(new FileWriter(file, append));
            output.write(fileContent);
            output.flush();
            output.close();
        } catch (IOException ioe) {
            return false;
        }
        return true;
    }

    /**
     * 写入序列化对象到指定路径
     *
     * @param filePath     文件路径
     * @param serializable 需要写入的序列化对象
     * @return 是否写入成功
     */
    public static boolean writeToPath(String filePath, Serializable serializable) {
        boolean result = false;
        FileOutputStream fileOut = null;
        ObjectOutputStream objectOut = null;

        try {
            File file = new File(filePath);
            File parentFile = file.getParentFile();

            // 如果父文件夹不存在，则生成父文件夹
            if (!parentFile.exists()) {
                boolean isMkDirSuccess = parentFile.mkdirs();
            }

            if (!file.exists()) {
                boolean isFileExists = file.createNewFile();
            }

            fileOut = new FileOutputStream(file);
            objectOut = new ObjectOutputStream(fileOut);
            objectOut.writeObject(serializable);
            fileOut.getFD().sync();
            result = true;

        } catch (Exception e) {
            e.printStackTrace();
        } finally {

            try {
                if (null != fileOut) {
                    fileOut.close();
                }
                if (null != objectOut) {
                    objectOut.close();
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return result;
    }

    /**
     * 写文件
     */
    public static boolean writeFile(String filePath, byte[] content) {
        if (null == filePath || null == content) {
            return false;
        }

        FileOutputStream fos = null;
        try {
            String pth = filePath.substring(0, filePath.lastIndexOf("/"));
            File pf = null;
            pf = new File(pth);
            if (pf.exists() && !pf.isDirectory()) {
                pf.delete();
            }
            pf = new File(filePath);
            if (pf.exists()) {
                if (pf.isDirectory())
                    deleteWithPath(filePath);
                else
                    pf.delete();
            }

            pf = new File(pth + File.separator);
            if (!pf.exists()) {
                if (!pf.mkdirs()) {
                }
            }

            fos = new FileOutputStream(filePath);
            fos.write(content);
            fos.flush();
            fos.close();
            fos = null;
            pf.setLastModified(System.currentTimeMillis());

            return true;

        } catch (Exception ex) {
        } finally {
            if (null != fos) {
                try {
                    fos.close();
                } catch (Exception ex) {
                }
            }
        }
        return false;
    }


//    /******** 读取相关 ********/
//    /**
//     * 读取文件输入流
//     */
//    public static InputStream readFile(String filePath) {
//        if (null == filePath) {
//            return null;
//        }
//        InputStream is = null;
//        try {
//            if (fileIsExist(filePath)) {
//                File f = new File(filePath);
//                is = new FileInputStream(f);
//            } else {
//                return null;
//            }
//        } catch (Exception ex) {
//            return null;
//        }
//        return is;
//    }

    /**
     * 读取序列化对象
     *
     * @param filePath 文件路径
     * @return Serializable对象
     */
    public static Object readFromPath(String filePath) {
        Object resultObj = null;
        FileInputStream fileInputStream = null;
        ObjectInputStream objectInputStream = null;

        try {
            fileInputStream = new FileInputStream(filePath);
            objectInputStream = new ObjectInputStream(fileInputStream);
            resultObj = objectInputStream.readObject();

        } catch (Exception e) {
            e.printStackTrace();
        } finally {

            try {
                if (null != fileInputStream) {
                    fileInputStream.close();
                }

                if (null != objectInputStream) {
                    objectInputStream.close();
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return resultObj;
    }

    /**
     * 读取流
     */
    public static byte[] readAll(InputStream is) throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream(1024);
        byte[] buf = new byte[1024];
        int c = is.read(buf);
        while (-1 != c) {
            baos.write(buf, 0, c);
            c = is.read(buf);
        }
        baos.flush();
        baos.close();
        return baos.toByteArray();
    }

 

    /**
     * 拷贝文件
     */
    public static boolean copyFile(File oldfile, String newPath) {
        try {
            int bytesum = 0;
            int byteread = 0;
            if (oldfile.exists()) {
                createFile(newPath);
                InputStream inStream = new FileInputStream(oldfile);
                FileOutputStream fs = new FileOutputStream(newPath);
                byte[] buffer = new byte[1444];
                while ((byteread = inStream.read(buffer)) != -1) {
                    bytesum += byteread;
                    System.out.println(bytesum);
                    fs.write(buffer, 0, byteread);
                }
                inStream.close();
                return true;
            }
        } catch (Exception e) {
            System.out.println("error  ");
            e.printStackTrace();
        }

        return false;
    }

    public static boolean copyTo(String fromPath, String toPath) {
        
        // 判断有空路径则直接返回
        if (null == fromPath || null == toPath) {
            System.out.println("FromFile or ToFile is null!!!");
            return false;
        }

        try {
            // 目标路径是文件，则取该文件所在的目录
            File toFile = new File(toPath);
            if (!toFile.isDirectory()) {
                toFile = toFile.getParentFile();
            }

            // 目录不存在则创建目录
            if (!toFile.exists()) {
                toFile.mkdirs();

                // 创建目录失败则复制失败
                if (!toFile.exists()) {
                    System.out.println("ToFile is not exists!!!");
                    return false;
                }
            }

            // 源路径是文件夹下的所有数据
            File fromFile = new File(fromPath);
            if (fromFile.isDirectory()) {
                File[] files = fromFile.listFiles();

                // 默认成功，如果有一个以上拷贝失败，则返回失败
                boolean isSuccess = true;
                for (File childFile : files) {
                    if (!copyFile(childFile, toFile)) {
                        isSuccess = false;
                    }
                }
                return isSuccess;
            }

            if (fromFile.isFile()) {
                return copyFile(fromFile, toFile);
            }

            System.out.println("FromFile not exists!!!  fromFile = " + fromFile);

        } catch (Exception e) {
            System.out.println("copyTo error!!!");
            e.printStackTrace();
        }

        return false;
    }

    /**
     * 拷贝文件到指定文件夹目录
     *
     * @param fromFile 文件路径
     * @param toFile   目标文件夹路径
     * @return 是否拷贝成功
     */
    public static boolean copyFile(File fromFile, File toFile) {
        if (null == fromFile || null == toFile) {
            System.out.println("FromFile or ToFile is null!!!");
            return false;
        }

        if (!fromFile.isFile() || !toFile.isDirectory()) {
            System.out.println("FromFile not a File, or ToFile not a Directory!!!");
            return false;
        }

        boolean isSuccess = false;
        FileInputStream inputStream = null;
        FileOutputStream outputStream = null;
        try {
            // 设置可读权限
            toFile.setReadable(true);
            String fileName = fromFile.getName();
            toFile = new File(toFile.getAbsolutePath() + "/" + fileName);
            inputStream = new FileInputStream(fromFile);
            outputStream = new FileOutputStream(toFile);

            int byteRead = 0;
            byte[] buffer = new byte[1024];
            while ((byteRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, byteRead);
            }
            isSuccess = true;
            System.out.println("Copy file success! from:" + fromFile + " to:" + toFile);

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (null != inputStream) {
                    inputStream.close();
                }
                if (null != outputStream) {
                    outputStream.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return isSuccess;
    }


    /******** 获取文件大小相关 ********/
    /**
     * 递归取得文件夹大小
     */
    public static long getFileSize(File f) throws Exception {
        if (f == null) {
            System.out.println("file is null !!!");
            return 0;
        }

        long size = 0;
        File flist[] = f.listFiles();

        if (flist == null) {
            System.out.println("flist is null !!!");
            return size;
        }

        for (File aFlist : flist) {
            if (aFlist.isDirectory()) {
                size = size + getFileSize(aFlist);
            } else {
                size = size + aFlist.length();
            }
        }
        return size;
    }

    /**
     * 获取文件/文件夹大小
     *
     * @param filePath 文件路径
     * @return 文件大小
     */
    public static long getFileSizeWithPath(String filePath) {
        try {
            File file = new File(filePath);
            return getFileSize(file);

        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

//    /**
//     * 得到文件大小
//     *
//     * @param filePath
//     * @return
//     */
//    public static long getFileSize(String filePath) {
//        if (null == filePath) {
//            return 0;
//        }
//        File file = new File(filePath);
//        if (file == null || !file.exists()) {
//            return 0;
//        }
//        return file.length();
//    }


    /******** 修改文件属性相关 ********/
    /**
     * 得到文件修改日期
     */
    public static long getFileModifyTime(String filePath) {
        if (null == filePath) {
            return 0;
        }
        File file = new File(filePath);
        if (file == null || !file.exists()) {
            return 0;
        }
        return file.lastModified();
    }

    /**
     * 设置文件修改日期
     */
    public static boolean setFileModifyTime(String filePath, long modifyTime) {
        if (null == filePath) {
            return false;
        }

        File file = new File(filePath);
        if (file == null || !file.exists()) {
            return false;
        }

        return file.setLastModified(modifyTime);
    }


    /******** 解压缩文件相关 ********/
    /**
     * 压缩文件
     */
    public static boolean readZipFile(String zipFileName, StringBuffer crc) {
        try {
            ZipInputStream zis = new ZipInputStream(new FileInputStream(zipFileName));
            ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null) {
                long size = entry.getSize();
                crc.append(entry.getCrc() + ", size: " + size);
            }
            zis.close();
        } catch (Exception ex) {
            return false;
        }
        return true;
    }

    /**
     * 压缩文件
     */
    @SuppressWarnings("resource")
    public static byte[] readGZipFile(String zipFileName) {
        if (fileIsExist(zipFileName)) {
            try {
                FileInputStream fin = new FileInputStream(zipFileName);
                int size;
                byte[] buffer = new byte[1024];
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                while ((size = fin.read(buffer, 0, buffer.length)) != -1) {
                    baos.write(buffer, 0, size);
                }
                return baos.toByteArray();
            } catch (Exception ex) {
            }
        }
        return null;
    }

    /**
     * 压缩文件
     */
    public static boolean zipFile(String baseDirName, String fileName, String targerFileName) throws IOException {
        if (baseDirName == null || "".equals(baseDirName)) {
            return false;
        }
        File baseDir = new File(baseDirName);
        if (!baseDir.exists() || !baseDir.isDirectory()) {
            return false;
        }

        String baseDirPath = baseDir.getAbsolutePath();
        File targerFile = new File(targerFileName);
        ZipOutputStream out = new ZipOutputStream(new FileOutputStream(targerFile));
        File file = new File(baseDir, fileName);

        boolean zipResult = false;
        if (file.isFile()) {
            zipResult = fileToZip(baseDirPath, file, out);
        } else {
            zipResult = dirToZip(baseDirPath, file, out);
        }
        out.close();
        return zipResult;
    }

    /**
     * 解压缩文件
     */
    public static boolean unZipFile(String fileName, String unZipDir)  {

        try{
            File f = new File(unZipDir);

            if (!f.exists()) {
                f.mkdirs();
            }

            BufferedInputStream is = null;
            ZipEntry entry;
            ZipFile zipfile = new ZipFile(fileName);
            Enumeration<?> enumeration = zipfile.entries();
            byte data[] = new byte[FILE_BUFFER_SIZE];

            while (enumeration.hasMoreElements()) {
                entry = (ZipEntry) enumeration.nextElement();

                if (entry.isDirectory()) {
                    File f1 = new File(unZipDir + "/" + entry.getName());
                    if (!f1.exists()) {
                        f1.mkdirs();
                    }
                } else {
                    is = new BufferedInputStream(zipfile.getInputStream(entry));
                    int count;
                    String name = unZipDir + "/" + entry.getName();
                    RandomAccessFile m_randFile = null;
                    File file = new File(name);
                    if (file.exists()) {
                        file.delete();
                    }

                    file.createNewFile();
                    m_randFile = new RandomAccessFile(file, "rw");
                    int begin = 0;

                    while ((count = is.read(data, 0, FILE_BUFFER_SIZE)) != -1) {
                        try {
                            m_randFile.seek(begin);
                        } catch (Exception ex) {
                        }

                        m_randFile.write(data, 0, count);
                        begin = begin + count;
                    }

//                    file.delete();
                    m_randFile.close();
                    is.close();
                }
            }
            return true;
        }catch (Exception e){
            e.printStackTrace();
        }
        return false;
    }

    /**
     * 解压文件
     */
    private static boolean fileToZip(String baseDirPath, File file, ZipOutputStream out) throws IOException {
        FileInputStream in = null;
        ZipEntry entry = null;

        byte[] buffer = new byte[FILE_BUFFER_SIZE];
        int bytes_read;
        try {
            in = new FileInputStream(file);
            entry = new ZipEntry(getEntryName(baseDirPath, file));
            out.putNextEntry(entry);

            while ((bytes_read = in.read(buffer)) != -1) {
                out.write(buffer, 0, bytes_read);
            }
            out.closeEntry();
            in.close();
        } catch (IOException e) {
            return false;
        } finally {
            if (out != null) {
                out.closeEntry();
            }

            if (in != null) {
                in.close();
            }
        }
        return true;
    }

    private static boolean dirToZip(String baseDirPath, File dir, ZipOutputStream out) throws IOException {
        if (!dir.isDirectory()) {
            return false;
        }

        File[] files = dir.listFiles();
        if (files.length == 0) {
            ZipEntry entry = new ZipEntry(getEntryName(baseDirPath, dir));

            try {
                out.putNextEntry(entry);
                out.closeEntry();
            } catch (IOException e) {
            }
        }

        for (int i = 0; i < files.length; i++) {
            if (files[i].isFile()) {
                fileToZip(baseDirPath, files[i], out);
            } else {
                dirToZip(baseDirPath, files[i], out);
            }
        }
        return true;
    }

    private static String getEntryName(String baseDirPath, File file) {
        if (!baseDirPath.endsWith(File.separator)) {
            baseDirPath = baseDirPath + File.separator;
        }

        String filePath = file.getAbsolutePath();
        if (file.isDirectory()) {
            filePath = filePath + "/";
        }

        int index = filePath.indexOf(baseDirPath);
        return filePath.substring(index + baseDirPath.length());
    }

    public static void closeQuietly(Closeable closeable) {
        if (closeable != null) {
            try {
                closeable.close();
            } catch (Throwable e) {
            }
        }
    }


    /**
     * 解压缩
     *
     * @param data 待压缩的数据
     * @return byte[] 解压缩后的数据
     */
    public static byte[] decompress(byte[] data) {
        byte[] output = new byte[0];

        Inflater decompresser = new Inflater();
        decompresser.reset();
        decompresser.setInput(data);

        ByteArrayOutputStream o = new ByteArrayOutputStream(data.length);
        try {
            byte[] buf = new byte[1024];
            while (!decompresser.finished()) {
                int i = decompresser.inflate(buf);
                o.write(buf, 0, i);
            }
            output = o.toByteArray();
        } catch (Exception e) {
            output = data;
            e.printStackTrace();
        } finally {
            try {
                o.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        decompresser.end();
        return output;
    }

    /**
     * 解压缩
     *
     * @param data 待压缩的数据
     * @return byte[] 解压缩后的数据
     */
    public static byte[] unGzip(byte[] data) {
        StringBuffer sb = new StringBuffer();

        try {
            InputStream in = new ByteArrayInputStream(data);
            InputStream is = new GZIPInputStream(in);
            BufferedReader br = new BufferedReader(new InputStreamReader(is));
            String line = null;

            while ((line = br.readLine()) != null) {
                sb.append(line);
            }

            is.close();
            in.close();
        } catch (IOException e) {
            System.out.println("IOException e: " + e.toString());
        }

        return sb.toString().getBytes();
    }

    /**
     * 创建一个文件，创建成功返回true
     *
     * @param filePath
     * @return
     */
    public static boolean createFile(String filePath) {
        try {
            File file = new File(filePath);
            if (!file.exists()) {
                if (!file.getParentFile().exists()) {
                    file.getParentFile().mkdirs();
                }

                file.createNewFile();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return true;
    }



    public static boolean downloadFile(String urlStr, String localFileDir, String localFileName) {
        try {
            URL url = new URL(urlStr);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            //设置超时间为3秒
            conn.setConnectTimeout(3 * 1000);
            //防止屏蔽程序抓取而返回403错误
            conn.setRequestProperty("User-Agent", "Mozilla/4.0 (compatible; MSIE 5.0; Windows NT; DigExt)");

            //得到输入流
            InputStream inputStream = conn.getInputStream();
            //获取自己数组
            byte[] getData = readInputStream(inputStream);

            //文件保存位置
            File saveDir = new File(localFileDir);
            if (!saveDir.exists()) {
                saveDir.mkdirs();
            }
            File file = new File(saveDir + File.separator + localFileName);
            FileOutputStream fos = new FileOutputStream(file);
            fos.write(getData);
            fos.flush();
            if (fos != null) {
                fos.close();
            }
            if (inputStream != null) {
                inputStream.close();
            }
            System.out.println("downloadFile success");
            return true;
        } catch (Exception e) {
            System.out.println(e.toString());
        }
        return false;
    }

    /**
     * 从输入流中获取字节数组
     * @param inputStream
     * @return
     * @throws IOException
     */
    public static  byte[] readInputStream(InputStream inputStream) throws IOException {
        byte[] buffer = new byte[1024];
        int len = 0;
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        while((len = inputStream.read(buffer)) != -1) {
            bos.write(buffer, 0, len);
        }
        bos.close();
        return bos.toByteArray();
    }


    public static void copyDirectory(File src, File dest) throws IOException {
        if (src.isDirectory()) {
            if (!dest.exists()) {
                dest.mkdir();
            }
            String files[] = src.list();
            for (String file : files) {
                File srcFile = new File(src, file);
                File destFile = new File(dest, file);
                // 递归复制
                copyDirectory(srcFile, destFile);
            }
        } else {
            InputStream in = new FileInputStream(src);
            OutputStream out = new FileOutputStream(dest);

            byte[] buffer = new byte[1024];

            int length;

            while ((length = in.read(buffer)) > 0) {
                out.write(buffer, 0, length);
            }
            in.close();
            out.close();
        }
    }


    /**
     * 读取文件，返回以byte数组形式的数据
     * @param filePath 要读取的文件路径名
     * @return
     */
    public static byte[] readFile(String filePath){
        if(isFileExist(filePath))
        {
            try {
                FileInputStream fi= new FileInputStream(filePath);
                return readFile(fi);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    /**
     * 从一个数量流里读取数据,返回以byte数组形式的数据
     * @param in 要读取的输入流
     * @return
     * @throws IOException
     */
    public static byte[] readFile(InputStream in)
    {
        try {
            byte[] b;
            b = new byte[in.available()];
            in.read(b);
            if(null!=in)
            {
                in.close();
                in=null;
            }
            return b;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static boolean isFileExist(String filePath)
    {
        File file=new File(filePath);
        return file.exists();
    }
}
