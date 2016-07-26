package com.glookast.fimsclient.app.gui;

import com.glookast.fimsclient.app.AppController;
import com.glookast.fimsclient.app.Service;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.util.List;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

public class MainServicePanel extends JPanel
{
    private final AppController myController;
    private final List<Service> myServices;

    private final JTabbedPane myTabbedPane;

    public MainServicePanel(AppController controller, List<Service> services, Service.Type serviceType)
    {
        super(new GridBagLayout());

        myController = controller;
        myServices = services;

        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 1;
        c.weighty = 1;
        c.insets.top = 5;
        c.insets.bottom = 5;
        c.anchor = GridBagConstraints.WEST;

        add(new JobsPanel(myController, serviceType), c);

        myTabbedPane = new JTabbedPane();
        myTabbedPane.addChangeListener(new ChangeListenerImpl());

        add(myTabbedPane, c);

        for (Service service : services) {
            myTabbedPane.add(service.getName(), new JobsServicePanel(controller, service));
        }
    }

    private class ChangeListenerImpl implements ChangeListener
    {
        @Override
        public void stateChanged(ChangeEvent e)
        {
            myController.setActiveService(myServices.get(myTabbedPane.getSelectedIndex()));
        }
    }
}
