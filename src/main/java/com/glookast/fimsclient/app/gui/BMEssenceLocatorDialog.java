package com.glookast.fimsclient.app.gui;

import com.glookast.fimsclient.app.AppController;
import com.glookast.fimsclient.app.gui.components.URIInputVerifier;
import com.glookast.fimsclient.app.gui.components.ViewEditDialog;
import com.glookast.fimsclient.app.utils.XmlUtils;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.awt.Window;
import java.util.ArrayList;
import java.util.UUID;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTextField;
import tv.fims.base.BMEssenceLocatorType;
import tv.fims.base.FolderLocatorType;
import tv.fims.base.ListFileLocatorType;
import tv.fims.base.SimpleFileLocatorType;

public class BMEssenceLocatorDialog extends ViewEditDialog<BMEssenceLocatorType>
{
    private final JTextField myFileField;

    public BMEssenceLocatorDialog(Window owner, AppController controller, boolean editable)
    {
        super(owner, controller, editable);

        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0;
        c.gridwidth = 3;
        c.insets = new Insets(5, 5, 5, 5);
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 1;

        JLabel lblURI = new JLabel("URI:");
        myFileField = new JTextField();
        myFileField.setText("smb://");
        myFileField.setPreferredSize(new Dimension(500, myFileField.getPreferredSize().height));
        myFileField.setInputVerifier(new URIInputVerifier());

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

        myPanel.add(lblURI, c1);
        myPanel.add(myFileField, c2);

        setTitle();
        pack();
        setLocationRelativeTo(owner);
        refreshGUI();
    }

    @Override
    protected String getObjectName()
    {
        return "Business Media Essence Locator";
    }

    private boolean verifyFields()
    {
        return myFileField.getInputVerifier().verify(myFileField);
    }

    @Override
    protected boolean saveObject()
    {
        if (!verifyFields()) {
            JOptionPane.showMessageDialog(this, "File must be of the form 'nfs://HOST[:PORT]/DIRECTORY' or 'smb://HOST[:PORT]/DIRECTORY'");
            return false;
        }

        if (myObject == null) {
            myObject = new SimpleFileLocatorType();
            myObject.setResourceID(UUID.randomUUID().toString());
            myObject.setResourceCreationDate(XmlUtils.createCalendar());
        }
        myObject.setResourceModifiedDate(XmlUtils.createCalendar());

        if (myObject instanceof SimpleFileLocatorType) {
            ((SimpleFileLocatorType)myObject).setFile(myFileField.getText());
        } else if (myObject instanceof ListFileLocatorType) {
            ((ListFileLocatorType) myObject).getFile().addAll(new ArrayList<String>());
        } else if (myObject instanceof FolderLocatorType) {
            ((FolderLocatorType) myObject).setFolder(myFileField.getText());
        }

        return true;
    }

    @Override
    protected void loadObject()
    {
        if (myObject instanceof SimpleFileLocatorType) {
            myFileField.setText(((SimpleFileLocatorType)myObject).getFile());
        } else if (myObject instanceof ListFileLocatorType) {
            myFileField.setText("Check output at console.");
            ((ListFileLocatorType)myObject).getFile().forEach(System.out::println);
            System.out.println("Dialog coming soon!");
        } else if (myObject instanceof FolderLocatorType) {
            myFileField.setText(((FolderLocatorType) myObject).getFolder());
        }
    }
}
