package com.tongji.zhixin.graduation;

import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.RandomAccessFile;

/**
 * Created by zhixin on 2018/3/16.
 */

public class FileOperation {

    private String filePath;
    private String fileName_Acceleration;
    private String fileName_Gyro;
    private String fileName_magnetic;
    private String fileName_magnetic_mean;
    private String fileName_All_Sensors;

    public FileOperation() {
        initData();
    }

    private void initData() {
        //我靠，这畜生写到SD卡里面去了，找半天没找出来错在哪
        filePath = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator +
                "AAAA" + File.separator;
        fileName_Acceleration = "log_Acceleration.txt";
        fileName_Gyro = "log_Gyro.txt";
        fileName_magnetic = "log_magnetic.txt";
        fileName_magnetic_mean = "log_magnetic_mean.txt";
        fileName_All_Sensors = "All_Sensors.txt";
    }

    // 将字符串写入到文本文件中
    public void writeTxtToFile(String strcontent, String filePath, String fileName) {

        //生成文件夹之后，再生成文件，不然会出错
        makeFilePath(filePath, fileName);
        String strFilePath = filePath + fileName;
        // 每次写入时，都换行写
        String strContent = strcontent + "\r\n";
        try {
            File file = new File(strFilePath);
            if (!file.exists()) {
                Log.e("TestFile", "Create the file:" + strFilePath);
                file.getParentFile().mkdirs();
                file.createNewFile();
            }
            RandomAccessFile raf = new RandomAccessFile(file, "rwd");
            raf.seek(file.length());
            raf.write(strContent.getBytes());
            raf.close();
        } catch (Exception e) {
            Log.e("TestFile", "Error on write File:" );
        }
    }

    // 生成文件
    public File makeFilePath(String filePath, String fileName) {
        File file = null;
        makeRootDirectory(filePath);
        try {
            file = new File(filePath + fileName);
            if (!file.exists()) {
                file.createNewFile();
                Log.e("File", "生成文件成功");
            }
        } catch (Exception e) {
            Log.e("File", "生成文件出错");
            e.printStackTrace();
        }
        return file;
    }

    // 生成文件夹
    public static void makeRootDirectory(String filePath) {
        File file;
        try {
            file = new File(filePath);
            if (!file.exists()) {
                file.mkdirs();
            }
        } catch (Exception e) {
            Log.e("error:", "创建文件夹出错");
        }
    }

    public String getFilePath() {
        return filePath;
    }

    public String getFileName_Acceleration() {
        return fileName_Acceleration;
    }

    public String getFileName_Gyro() {
        return fileName_Gyro;
    }

    public String getFileName_magnetic() {
        return fileName_magnetic;
    }

    public String getFileName_magnetic_mean() {
        return fileName_magnetic_mean;
    }

    public String getFileName_All_Sensors() {
        return fileName_All_Sensors;
    }
}
