package ru.sergeykozhukhov.recorderservice;

import android.media.MediaRecorder;
import android.os.Build;

import java.io.File;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

/**
 * Диктофон
 */
public class VoiceRecorder {

    /**
     * Класс для записи звука
     */
    private MediaRecorder mediaRecorder;

    /**
     * Путь к файлу
     */
    private String filePath;

    /**
     * Имя файла
     */
    private String fileName;


    public VoiceRecorder() {

    }

    /**
     * Начало записи
     */
    public void start() {
        try {
            releaseRecorder();

            File outFile = new File(filePath+newFileNameByDate());
            if (outFile.exists()) {
                outFile.delete();
            }

            mediaRecorder = new MediaRecorder();
            mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
            mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
            mediaRecorder.setOutputFile(filePath+fileName);
            mediaRecorder.prepare();
            mediaRecorder.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Остановка записи
     */
    public void stop() {
        if (mediaRecorder != null) {
            mediaRecorder.stop();
        }
    }

    public void recodePause(){
        if (mediaRecorder!= null){
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                mediaRecorder.pause();
            }
        }
    }

    public void recordResume(){
        if(mediaRecorder!=null){
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                mediaRecorder.resume();
            }
        }
    }

    /**
     * Освобождение ресурсов
     */
    private void releaseRecorder() {
        if (mediaRecorder != null) {
            mediaRecorder.release();
            mediaRecorder = null;
        }
    }

    /**
     * Создание имени для файла по текущей дате и времени
     * @return имя для файла
     */
    private String newFileNameByDate(){
        Date date = new Date();
        GregorianCalendar calendar = new GregorianCalendar();
        calendar.setTime(date);
        String fileName = "/"+
                + calendar.get(Calendar.YEAR)+"_"
                + calendar.get(Calendar.MONTH) + "_"
                + calendar.get(Calendar.DAY_OF_MONTH) + "_"
                + calendar.get(Calendar.HOUR) + "_"
                + calendar.get(Calendar.MINUTE) + "_"
                + calendar.get(Calendar.SECOND) + ".3gpp";
        this.fileName = fileName;
        return fileName;
    }

    public MediaRecorder getMediaRecorder() {
        return mediaRecorder;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }
}
