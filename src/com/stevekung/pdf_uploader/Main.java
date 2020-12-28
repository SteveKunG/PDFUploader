package com.stevekung.pdf_uploader;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.net.URL;

import javax.imageio.ImageIO;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;

import com.electronwill.nightconfig.core.file.CommentedFileConfig;

public class Main implements ActionListener
{
//    private static final CommentedFileConfig CONFIG = CommentedFileConfig.builder("config.toml").defaultResource("resources/default.toml").build();
    private static final CommentedFileConfig CONFIG = CommentedFileConfig.builder("config.toml").build();
    private final MenuItem startStopMenu = new MenuItem("Pause Upload");
    private TrayIcon trayIcon;
    private SystemTray tray;
    
    static
    {
        CONFIG.add("path", "user");
    }
    
    public static void main(String[] args)
    {
//        SwingUtilities.invokeLater(Main::new);
    }

    public Main()
    {
        CONFIG.load();

        this.init();
        // config.save(); not needed here thanks to autosave()
//        config.close(); // Close the FileConfig once you're done with it :)
    }
    
    @Override
    public void actionPerformed(ActionEvent event)
    {
        
    }
    
    private void init()
    {
        if (SystemTray.isSupported())
        {
            this.tray = SystemTray.getSystemTray();
            Image image = null;

            try
            {
                image = ImageIO.read(this.getResource("icon.png")); // เปลี่ยน Icon ของโปรแกรม
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }

            PopupMenu popup = new PopupMenu();
            this.startStopMenu.addActionListener(this);
            popup.add(this.startStopMenu);
            this.trayIcon = new TrayIcon(image, "Net Login", popup);
            this.trayIcon.setImageAutoSize(true);
        }
        else
        {
//            Main.displayErrorMessage("System Tray not supported", "Current operation system is not supported!");
        }
    }
    
    /**
     * ใช้เพื่อ get resource เช่น รูปภาพ
     * ไฟล์ทั้งหมดอยู่ในโฟลเดอร์ resources
     * @param fileName ชื่อของไฟล์ รวมนามสกุล
     * @return URL ของไฟล์
     */
    private URL getResource(String fileName)
    {
        return Main.class.getResource("/resources/" + fileName);
    }
}