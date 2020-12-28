package com.stevekung.pdf_uploader;

import java.io.File;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

public class DirWatcherTest
{
    public static void main(String[] args)
    {
        TimerTask task = new DirectoryWatcher("G:/wasinthorn/file_changes", "pdf")
        {
            @Override
            protected void onFileAdd(File file)
            {
                System.out.println("File "+ file.getName());
                PDFUploader.upload(file);
            }
        };

        Timer timer = new Timer();
        timer.schedule(task, new Date(), 1000L);
    }
}