package com.glookast.fimsclient.app.gui;

import com.glookast.fimsclient.app.AppController;
import com.glookast.fimsclient.app.Service;
import com.glookast.fimsclient.app.ServiceFactory;
import com.glookast.fimsclient.app.gui.components.ViewEditDialog;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.awt.Window;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTextField;

public class ServiceDialog extends ViewEditDialog<Service>
{
    private final JTextField myNameField;
    private final JTextField myAddressField;
    private final JTextField myCallbackAddressField;

    public ServiceDialog(Window owner, AppController controller, boolean editable)
    {
        super(owner, controller, editable);

        JLabel lblName = new JLabel("Name:");
        myNameField = new JTextField();
        myNameField.setText("Gloobox");
        myNameField.setPreferredSize(new Dimension(500, myNameField.getPreferredSize().height));

        JLabel lblAddress = new JLabel("Address:");
        myAddressField = new JTextField();
        myAddressField.setText("http://localhost:4001");
        myAddressField.setPreferredSize(new Dimension(500, myAddressField.getPreferredSize().height));

        JLabel lblCallbackAddress = new JLabel("Callback address:");
        myCallbackAddressField = new JTextField();
        myCallbackAddressField.setPreferredSize(new Dimension(500, myAddressField.getPreferredSize().height));

        GridBagConstraints c1 = new GridBagConstraints();
        c1.gridx = 0;
        c1.anchor = GridBagConstraints.WEST;
        c1.insets = new Insets(5, 5, 5, 5);

        GridBagConstraints c2 = new GridBagConstraints();
        c2.gridx = 1;
        c2.anchor = GridBagConstraints.WEST;
        c2.weightx = 1;
        c2.fill = GridBagConstraints.HORIZONTAL;
        c2.insets = new Insets(5, 5, 5, 5);

        myPanel.add(lblName, c1);
        myPanel.add(myNameField, c2);
        myPanel.add(lblAddress, c1);
        myPanel.add(myAddressField, c2);
        myPanel.add(lblCallbackAddress, c1);
        myPanel.add(myCallbackAddressField, c2);

        setTitle();
        pack();
        setLocationRelativeTo(owner);
        refreshGUI();
    }

    @Override
    protected String getObjectName()
    {
        return "Service";
    }

    @Override
    protected boolean saveObject()
    {
        String name = myNameField.getText();
        URL address, callbackAddress = null;

        try {
            address = new URL(myAddressField.getText());
        } catch (MalformedURLException ex) {
            JOptionPane.showMessageDialog(this, "Address is not valid.");
            Logger.getLogger(ServiceDialog.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        }

        if (!myCallbackAddressField.getText().isEmpty()) {
            try {
                callbackAddress = new URL(myCallbackAddressField.getText());
            } catch (MalformedURLException ex) {
                JOptionPane.showMessageDialog(this, "Callback Address is not valid.");
                Logger.getLogger(ServiceDialog.class.getName()).log(Level.SEVERE, null, ex);
                return false;
            }
        }

        myObject = ServiceFactory.createService(name, Service.Type.Capture, Service.Method.SOAP, address, callbackAddress);
        return true;
    }

    @Override
    protected void loadObject()
    {
        if (myObject != null) {
            myNameField.setText(myObject.getName());
            myAddressField.setText(String.valueOf(myObject.getAddress()));
            if (myObject.getCallbackAddress() != null) {
                myCallbackAddressField.setText(String.valueOf(myObject.getCallbackAddress()));
            } else {
                myCallbackAddressField.setText("");
            }
        }
    }
}
