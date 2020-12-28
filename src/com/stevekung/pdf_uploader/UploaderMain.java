package com.stevekung.pdf_uploader;

import java.awt.BorderLayout;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;

import com.electronwill.nightconfig.core.file.CommentedFileConfig;
import com.electronwill.nightconfig.core.file.FileConfig;

public class UploaderMain extends JFrame
{
    private JPanel contentPane;

    public static void main(String[] args)
    {
        SwingUtilities.invokeLater(UploaderMain::new);
    }

    public UploaderMain()
    {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setBounds(100, 100, 450, 300);
        contentPane = new JPanel();
        contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
        contentPane.setLayout(new BorderLayout(0, 0));
        setContentPane(contentPane);

        // Simple builder:

        // Advanced builder, default resource, autosave and much more (-> cf the wiki)
        CommentedFileConfig config = CommentedFileConfig.builder("config.toml").build();
        config.add("path", "user");
        config.load(); // This actually reads the config

//        String name = config.get("username"); // Generic return type!
//        List<String> names = config.get("users_list"); // Generic return type!
//        long id = config.getLong("account.id"); // Compound path: key "id" in subconfig "account"
//        int points = config.getIntOrElse("account.score", defaultScore); // Default value
//
//        config.set("account.score", points*2);
//
//        String comment = config.getComment("user");
        // NightConfig saves the config's comments (for TOML and HOCON)

        // config.save(); not needed here thanks to autosave()
        config.close(); // Close the FileConfig once you're done with it :)
    }
}