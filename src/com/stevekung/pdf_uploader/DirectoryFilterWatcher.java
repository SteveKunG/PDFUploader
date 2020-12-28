package com.stevekung.pdf_uploader;

import java.io.File;
import java.io.FileFilter;

public class DirectoryFilterWatcher implements FileFilter
{
    private final String filter;

    public DirectoryFilterWatcher()
    {
        this.filter = "";
    }

    public DirectoryFilterWatcher(String filter)
    {
        this.filter = filter;
    }

    @Override
    public boolean accept(File file)
    {
        if ("".equals(this.filter))
        {
            return true;
        }
        return file.getName().endsWith(this.filter);
    }
}