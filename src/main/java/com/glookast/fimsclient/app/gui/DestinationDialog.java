package com.glookast.fimsclient.app.gui;

import com.glookast.fimsclient.app.AppController;
import com.glookast.fimsclient.app.gui.components.URIInputVerifier;
import com.glookast.fimsclient.app.gui.components.ViewEditDialog;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.awt.Window;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTextField;
import tv.fims.base.TransferAtomType;

public class DestinationDialog extends ViewEditDialog<TransferAtomType>
{
    private final JTextField myStorageLocation;

    public DestinationDialog(Window owner, AppController controller, boolean editable)
    {
        super(owner, controller, editable);

        JLabel lblURI = new JLabel("Destination:");
        myStorageLocation = new JTextField();
        myStorageLocation.setText("smb://");
        myStorageLocation.setPreferredSize(new Dimension(500, myStorageLocation.getPreferredSize().height));
        myStorageLocation.setInputVerifier(new URIInputVerifier());

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
        myPanel.add(myStorageLocation, c2);

        setTitle();
        pack();
        setLocationRelativeTo(owner);
        refreshGUI();
    }

    @Override
    protected String getObjectName()
    {
        return "Destination";
    }

    @Override
    protected boolean saveObject()
    {
        if (!verifyFields()) {
            JOptionPane.showMessageDialog(DestinationDialog.this, "URI must be of the form 'nfs://HOST[:PORT]/DIRECTORY/' or 'smb://HOST[:PORT]/DIRECTORY/'");
            return false;
        }

        myObject = new TransferAtomType();
        myObject.setDestination(myStorageLocation.getText());

        return true;
    }

    @Override
    protected void loadObject()
    {
        if (myObject != null) {
            myStorageLocation.setText(myObject.getDestination());
        }
    }

    private boolean verifyFields()
    {
        return myStorageLocation.getInputVerifier().verify(myStorageLocation);
    }
}
