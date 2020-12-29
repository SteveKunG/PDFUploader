package com.stevekung.pdf_uploader;

import java.io.File;
import java.io.FileFilter;

public class DirectoryFilterWatcher implements FileFilter
{
    private final String filter;

    public DirectoryFilterWatcher(String filter)
    {
        this.filter = filter;
    }

    @Override
    public boolean accept(File file)
    {
        return file.getName().endsWith(this.filter);
    }
}