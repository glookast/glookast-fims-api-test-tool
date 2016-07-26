package com.glookast.fimsclient.app;

import com.glookast.fimsclient.app.gui.MainFrame;
import java.awt.EventQueue;
import javax.swing.JFrame;

public class FimsClientApp
{
    private static JFrame theFrame;

    public static void main(String[] args)
    {
        EventQueue.invokeLater(new Runnable()
        {
            @Override
            public void run()
            {
                theFrame = new MainFrame("Glookast FIMS API Test Tool");
                theFrame.setSize(300, 200);
                theFrame.setVisible(true);
            }
        });
    }
}
