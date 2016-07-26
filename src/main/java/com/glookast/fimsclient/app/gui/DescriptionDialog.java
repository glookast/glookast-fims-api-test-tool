package com.glookast.fimsclient.app.gui;

import com.glookast.fimsclient.app.AppController;
import com.glookast.fimsclient.app.gui.components.ViewEditDialog;
import com.glookast.fimsclient.app.utils.XmlUtils;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.awt.Window;
import java.util.UUID;
import javax.swing.JLabel;
import javax.swing.JTextField;
import tv.fims.base.DescriptionType;
import tv.fims.description.BmContentDescriptionType;
import tv.fims.description.TitleType;

public class DescriptionDialog extends ViewEditDialog<DescriptionType>
{
    private final JTextField myTitleField;
    private final JTextField myDescriptionField;

    public DescriptionDialog(Window owner, AppController controller, boolean editable)
    {
        super(owner, controller, editable);

        JLabel lblTitle = new JLabel("Title:");
        myTitleField = new JTextField();
        myTitleField.setPreferredSize(new Dimension(200, myTitleField.getPreferredSize().height));
        JLabel lblDescription = new JLabel("Description:");
        myDescriptionField = new JTextField();
        myDescriptionField.setPreferredSize(new Dimension(200, myDescriptionField.getPreferredSize().height));

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

        myPanel.add(lblTitle, c1);
        myPanel.add(myTitleField, c2);
        myPanel.add(lblDescription, c1);
        myPanel.add(myDescriptionField, c2);

        setTitle();
        pack();
        setLocationRelativeTo(owner);
        refreshGUI();
    }

    @Override
    protected final void refreshGUI()
    {
    }

    @Override
    protected String getObjectName()
    {
        return "Description";
    }

    @Override
    protected boolean saveObject()
    {
        if (myObject == null) {
            myObject = new DescriptionType();
            myObject.setResourceID(UUID.randomUUID().toString());
            myObject.setResourceCreationDate(XmlUtils.createCalendar());
        }
        myObject.setResourceModifiedDate(XmlUtils.createCalendar());

        TitleType title = new TitleType();
        title.setValue(myTitleField.getText());

        tv.fims.description.DescriptionType description = new tv.fims.description.DescriptionType();
        description.setValue(myDescriptionField.getText());

        BmContentDescriptionType bmContentDescription = new BmContentDescriptionType();
        bmContentDescription.getTitle().add(title);
        bmContentDescription.getDescription().add(description);

        myObject.setBmContentDescription(bmContentDescription);

        return true;
    }

    @Override
    protected void loadObject()
    {
        if (myObject != null) {
            BmContentDescriptionType bmContentDescription = myObject.getBmContentDescription();
            if (bmContentDescription != null) {
                if (!bmContentDescription.getTitle().isEmpty()) {
                    myTitleField.setText(bmContentDescription.getTitle().get(0).getValue());
                }
                if (!bmContentDescription.getDescription().isEmpty()) {
                    myDescriptionField.setText(bmContentDescription.getDescription().get(0).getValue());
                }
            }
        }
    }

}
