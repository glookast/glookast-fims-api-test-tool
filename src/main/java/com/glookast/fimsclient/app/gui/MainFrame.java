package com.glookast.fimsclient.app.gui;

import com.glookast.fimsclient.app.AppController;
import com.glookast.fimsclient.app.AppControllerImpl;
import com.glookast.fimsclient.app.Service;
import com.glookast.fimsclient.app.utils.XmlUtils;
import java.awt.EventQueue;
import java.awt.HeadlessException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JTabbedPane;
import tv.fims.base.JobType;
import tv.fims.base.ProfileType;

public class MainFrame extends JFrame
{
    private final AppController myController;
    private final ActionListener myActionListener;
    private final JTabbedPane myTabbedPane;

    public MainFrame(String title) throws HeadlessException
    {
        super(title);

        myController = new AppControllerImpl();
        myController.loadProperties();
        myController.loadApplicationData();
        myController.addListener(new EventListenerImpl());

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);

        myActionListener = new ActionListenerImpl();

        setJMenuBar(createMenuBar());

        myTabbedPane = new JTabbedPane();
        add(myTabbedPane);

        refreshGUI();
    }

    private void refreshGUI()
    {
        EventQueue.invokeLater(new Runnable()
        {
            @Override
            public void run()
            {
                myTabbedPane.removeAll();

                boolean doPack = false;

                for (Service.Type serviceType : new Service.Type[] { Service.Type.Capture, Service.Type.Transfer, Service.Type.Transform }) {
                     List<Service> services = myController.getServices(serviceType);
                     if (!services.isEmpty()) {
                         myTabbedPane.add(serviceType + " Service", new MainServicePanel(myController, services, serviceType));
                         doPack = true;
                     }
                }

                if (doPack) {
                    pack();
                }
                setLocationRelativeTo(null);
            }
        });
    }

    private JMenuBar createMenuBar()
    {
        JMenuBar menubar = new JMenuBar();

        menubar.add(createFileMenu());
        menubar.add(createToolsMenu());

        return menubar;
    }

    private JMenu createFileMenu()
    {
        JMenu menu = new JMenu("File");

        JMenuItem item = new JMenuItem("Import");
        item.setActionCommand("Import");
        item.addActionListener(myActionListener);
        menu.add(item);

        item = new JMenuItem("Export");
        item.setActionCommand("Export");
        item.addActionListener(myActionListener);
        menu.add(item);

        menu.addSeparator();

        item = new JMenuItem("Exit");
        item.setActionCommand("Exit");
        item.addActionListener(myActionListener);
        menu.add(item);

        return menu;
    }

    private JMenu createToolsMenu()
    {
        JMenu menu = new JMenu("Tools");

        JMenuItem item = new JMenuItem("Capture Profiles");
        item.setActionCommand("CaptureProfiles");
        item.addActionListener(myActionListener);
        menu.add(item);

        item = new JMenuItem("Transfer Profiles");
        item.setActionCommand("TransferProfiles");
        item.addActionListener(myActionListener);
        menu.add(item);

        item = new JMenuItem("Transform Profiles");
        item.setActionCommand("TransformProfiles");
        item.addActionListener(myActionListener);
        menu.add(item);

        item = new JMenuItem("Business Media Objects");
        item.setActionCommand("BusinessMediaObjects");
        item.addActionListener(myActionListener);
        menu.add(item);

        menu.addSeparator();

        item = new JMenuItem("Services");
        item.setActionCommand("Services");
        item.addActionListener(myActionListener);
        menu.add(item);

        return menu;
    }

    private class ActionListenerImpl implements ActionListener
    {
        @Override
        public void actionPerformed(ActionEvent e)
        {
            switch (e.getActionCommand()) {
                case "Import":
                    doImport();
                    break;
                case "Export":
                    doExport();
                    break;
                case "Exit":
                    dispose();
                    break;
                case "CaptureProfiles":
                    openProfilesDialog(Service.Type.Capture);
                    break;
                case "TransferProfiles":
                    openProfilesDialog(Service.Type.Transfer);
                    break;
                case "TransformProfiles":
                    openProfilesDialog(Service.Type.Transform);
                    break;
                case "BusinessMediaObjects":
                    openBmObjectsDialog();
                    break;
                case "Services":
                    openServicesDialog();
                    break;
            }
        }
    }

    private void doImport()
    {
        XmlUtils.doImport(this, myController, true, true, true, true, true, true);
    }

    private void doExport()
    {
        List<JobType> jobs = new ArrayList<>();
        jobs.addAll(myController.getJobs(Service.Type.Capture));
        jobs.addAll(myController.getJobs(Service.Type.Transfer));
        jobs.addAll(myController.getJobs(Service.Type.Transform));
        List<ProfileType> profiles = new ArrayList<>();
        profiles.addAll(myController.getProfiles(Service.Type.Capture));
        profiles.addAll(myController.getProfiles(Service.Type.Transfer));
        profiles.addAll(myController.getProfiles(Service.Type.Transform));
        XmlUtils.doExport(this, jobs, profiles, true, true, true, true, true, true);
    }

    private void openProfilesDialog(Service.Type serviceType)
    {
        ProfilesDialog dialog = new ProfilesDialog(this, myController, serviceType);
        dialog.setVisible(true);
    }

    private void openBmObjectsDialog()
    {
        BMObjectsDialog dialog = new BMObjectsDialog(this, myController);
        dialog.setVisible(true);
    }

    private void openServicesDialog()
    {
        ServicesDialog dialog = new ServicesDialog(this, myController);
        dialog.setVisible(true);
    }

    private class EventListenerImpl implements AppController.EventListener
    {
        @Override
        public void onEvent(Event event)
        {
            if (event == Event.Services) {
                refreshGUI();
            }
        }
    }
}
