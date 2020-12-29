package com.stevekung.pdf_uploader;

import java.awt.*;
import java.awt.TrayIcon.MessageType;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;
import java.util.stream.Collectors;

import javax.imageio.ImageIO;
import javax.swing.JFileChooser;
import javax.swing.SwingUtilities;

import com.electronwill.nightconfig.core.file.CommentedFileConfig;
import com.google.gson.JsonSyntaxException;

public class Main implements ActionListener
{
    private final CommentedFileConfig config = CommentedFileConfig.builder("config.toml").autosave().autoreload().build();
    private final MenuItem startStopMenu = new MenuItem("Pause Upload");
    private final MenuItem changePathMenu = new MenuItem("Change Path");
    private final MenuItem openFolderMenu = new MenuItem("Open Folder");
    private final MenuItem exitMenu = new MenuItem("Exit");
    private final PopupMenu popup = new PopupMenu();
    private final JSystemFileChooser chooser = new JSystemFileChooser();
    private TrayIcon trayIcon;
    private SystemTray tray;
    private String path = System.getProperty("user.home") + "\\lpru_pdf"; // default path ของโปรแกรมอัพโหลด
    private boolean pause;
    private File currentDirectory = new File(this.path);

    public static void main(String[] args)
    {
        SwingUtilities.invokeLater(Main::new);
    }

    public Main()
    {
        this.initConfig();

        Runtime.getRuntime().addShutdownHook(new Thread()
        {
            @Override
            public void run()
            {
                Main.this.config.save();
                Main.this.config.close();
                System.out.println("Shutting down!");
            }
        });

        this.init();
        this.runFileUploader();
    }

    @Override
    public void actionPerformed(ActionEvent event)
    {
        if (event.getSource() == this.exitMenu)
        {
            System.exit(0);
        }
        else if (event.getSource() == this.startStopMenu)
        {
            this.pause = !this.pause;

            if (this.pause)
            {
                this.displayWindowNotification("โปรแกรมอัพโหลด", "หยุดการอัพโหลดแล้ว");
                this.getMenuItem(this.startStopMenu).setLabel("Start Upload");
            }
            else
            {
                this.displayWindowNotification("โปรแกรมอัพโหลด", "เริ่มทำการอัพโหลดต่อ");
                this.getMenuItem(this.startStopMenu).setLabel("Pause Upload");
            }
            this.config.set("pause", this.pause);
            this.config.save();
        }
        else if (event.getSource() == this.changePathMenu)
        {
            this.chooser.showSaveDialog(null);

            if (this.chooser.getSelectedFile() != null)
            {
                this.path = this.chooser.getSelectedFile().getAbsolutePath();
                this.config.set("path", this.path);
                this.config.save();
                System.out.println("Selected path: " + this.path);
            }
        }
        else if (event.getSource() == this.openFolderMenu)
        {
            try
            {
                Desktop.getDesktop().open(this.currentDirectory);
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
        }
    }

    /**
     * เริ่มต้นระบบ Config
     */
    private void initConfig()
    {
        this.config.load();
        this.currentDirectory.mkdirs();
        System.out.println("ConfigBeforeLoad: " + this.config);

        if (this.config.get("path") == null)
        {
            this.config.set("path", this.path);
            this.config.set("pause", this.pause);
            this.config.save();
        }
        else
        {
            this.path = this.config.get("path");
            this.pause = this.config.get("pause");
            this.currentDirectory = new File(this.path);

            if (this.pause)
            {
                this.startStopMenu.setLabel("Start Upload");
            }
        }

        System.out.println("ConfigAfterLoad: " + this.config);
    }

    private void init()
    {
        if (SystemTray.isSupported())
        {
            this.tray = SystemTray.getSystemTray();
            Image image = null;

            try
            {
                image = ImageIO.read(this.getResource("cloud.png")); // เปลี่ยน Icon ของโปรแกรม
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }

            this.startStopMenu.addActionListener(this);
            this.popup.add(this.startStopMenu);
            this.changePathMenu.addActionListener(this);
            this.popup.add(this.changePathMenu);
            this.exitMenu.addActionListener(this);
            this.popup.add(this.openFolderMenu);
            this.openFolderMenu.addActionListener(this);
            this.popup.add(this.exitMenu);
            this.trayIcon = new TrayIcon(image, "PDF Uploader", this.popup);
            this.trayIcon.setImageAutoSize(true);
        }
        else
        {
            this.displayWindowNotification("System Tray not supported", "Current operation system is not supported!");
        }

        try
        {
            this.tray.add(this.trayIcon);
        }
        catch (AWTException e)
        {
            e.printStackTrace();
        }

        this.chooser.setCurrentDirectory(this.currentDirectory);
        this.chooser.setDialogTitle("Select folder to upload");
        this.chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        this.chooser.setAcceptAllFileFilterUsed(false);
    }

    /**
     * run task เพื่ออัพโหลดไฟล์
     */
    private void runFileUploader()
    {
        DirectoryWatcher task = new DirectoryWatcher(this.path, "pdf")
        {
            @Override
            protected void onFileAdd(File file)
            {
                System.out.println("File added: " + file.getName());

                if (!Main.this.pause)
                {
                    Main.this.upload(file);
                }
            }
        };

        Timer timer = new Timer();
        timer.schedule(task, new Date(), 1000L);
    }

    /**
     * อัพโหลดไฟล์ไปยัง server
     * @param file file ที่ต้องการจะอัพโหลด
     */
    private void upload(File file)
    {
        String boundary = Long.toHexString(System.currentTimeMillis()); // Just generate some unique random value.
        String CRLF = "\r\n"; // Line separator required by multipart/form-data.

        try
        {
            URLConnection connection = new URL(Constants.FILE_SERVICE).openConnection();
            connection.setDoOutput(true);
            connection.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);

            try (OutputStream output = connection.getOutputStream(); PrintWriter writer = new PrintWriter(new OutputStreamWriter(output, StandardCharsets.UTF_8), true);)
            {
                writer.append("--" + boundary).append(CRLF);
                writer.append("Content-Disposition: form-data; name=\"binaryFile\"; filename=\"" + file.getName() + "\"").append(CRLF);
                writer.append("Content-Type: " + URLConnection.guessContentTypeFromName(file.getName())).append(CRLF);
                writer.append("Content-Transfer-Encoding: binary").append(CRLF);
                writer.append(CRLF).flush();
                Files.copy(file.toPath(), output);
                output.flush(); // Important before continuing with writer!
                writer.append(CRLF).flush(); // CRLF is important! It indicates end of boundary.

                // End of multipart/form-data.
                writer.append("--" + boundary + "--").append(CRLF).flush();
            }

            // Request is lazily fired whenever you need to obtain information about response.
            HttpURLConnection urlConnection = (HttpURLConnection) connection;
            int responseCode = urlConnection.getResponseCode();
            String result = new BufferedReader(new InputStreamReader(urlConnection.getInputStream())).lines().collect(Collectors.joining("\n"));
            FileStatus status = Constants.GSON.fromJson(result, FileStatus.class); // แปลง json เป็น class FileStatus

            if (responseCode == 200) // response code ควรเป็น 200
            {
                if (status.isSuccess())
                {
                    this.displayWindowNotification("อัพโหลดไฟล์เรียบร้อย", "ไฟล์ถูกอัพโหลดที่: " + status.getURL());

                    this.schedule(() ->
                    {
                        try
                        {
                            Desktop.getDesktop().browse(new URI(status.getURL()));
                        }
                        catch (IOException | URISyntaxException e)
                        {
                            this.displayWindowNotification("เกิดข้อผิดพลาด", "ไม่สามารถเปิดลิ้งก์ได้");
                            e.printStackTrace();
                        }
                    }, 2000L);
                }
            }
            else
            {
                this.displayWindowNotification("ไม่สามารถอัพโหลดไฟล์ได้!", "Error Code: " + responseCode);
            }
        }
        catch (MalformedURLException e)
        {
            this.displayWindowNotification("เกิดข้อผิดพลาด", "ไม่สามารถเชื่อมต่ออินเทอร์เน็ตได้!");
            e.printStackTrace();
        }
        catch (IOException e)
        {
            this.displayWindowNotification("เกิดข้อผิดพลาด", "ไม่สามารถอัพโหลดไฟล์ได้!");
            e.printStackTrace();
        }
        catch (JsonSyntaxException e)
        {
            e.printStackTrace();
        }
    }

    /**
     * ใช้สำหรับ run task ด้วย delay
     * @param runnable task ที่ต้องการจะ run
     * @param delay ค่า delay
     */
    private void schedule(Runnable runnable, long delay)
    {
        Timer timer = new Timer();
        TimerTask task = new TimerTask()
        {
            @Override
            public void run()
            {
                runnable.run();
            }
        };
        timer.schedule(task, delay);
    }

    /**
     * ใช้เพื่อเปลี่ยนค่าใน MenuItem
     * @param menu เมนูที่จะค้นหา
     * @return เมนูที่จะใช้เปลี่ยนค่า
     */
    private MenuItem getMenuItem(MenuItem menu)
    {
        for (int i = 0; i < this.popup.getItemCount(); i++)
        {
            if (this.popup.getItem(i) == menu)
            {
                return this.popup.getItem(i);
            }
        }
        return null;
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

    /**
     * แสดงข้อความ Window Notification
     * @param title หัวข้อความ
     * @param message ข้อความ
     */
    private void displayWindowNotification(String title, String message)
    {
        this.trayIcon.displayMessage(title, message, MessageType.INFO);
    }
}