package com.stevekung.pdf_uploader;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;

import javax.swing.JFileChooser;
import javax.swing.LookAndFeel;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import sun.swing.FilePane;

public class JSystemFileChooser extends JFileChooser
{
    @Override
    public void updateUI()
    {
        LookAndFeel old = UIManager.getLookAndFeel();

        try
        {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        }
        catch (Throwable e)
        {
            old = null;
        }

        super.updateUI();

        if (old != null)
        {
            FilePane filePane = JSystemFileChooser.findFilePane(this);
            filePane.setViewType(FilePane.VIEWTYPE_DETAILS);
            filePane.setViewType(FilePane.VIEWTYPE_LIST);

            Color background = UIManager.getColor("Label.background");
            this.setBackground(background);
            this.setOpaque(true);

            try
            {
                UIManager.setLookAndFeel(old);
            }
            catch (UnsupportedLookAndFeelException e) {}
        }
    }

    private static FilePane findFilePane(Container parent)
    {
        for (Component component : parent.getComponents())
        {
            if (FilePane.class.isInstance(component))
            {
                return (FilePane)component;
            }
            if (component instanceof Container)
            {
                Container container = (Container)component;

                if (container.getComponentCount() > 0)
                {
                    FilePane found = JSystemFileChooser.findFilePane(container);

                    if (found != null)
                    {
                        return found;
                    }
                }
            }
        }
        return null;
    }
}