package com.glookast.fimsclient.app.gui;

import com.glookast.fimsclient.app.AppController;
import com.glookast.fimsclient.app.gui.components.ScrollTableModel;
import com.glookast.fimsclient.app.gui.components.TablePanel;
import com.glookast.fimsclient.app.gui.components.ViewEditDialog;
import com.glookast.fimsclient.app.utils.StringUtils;
import com.glookast.fimsclient.app.utils.XmlUtils;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.awt.Window;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import tv.fims.base.BMContentFormatType;
import tv.fims.base.BMContentFormatsType;
import tv.fims.base.BMContentStatusType;
import tv.fims.base.BMContentType;
import tv.fims.base.BMStatusType;
import tv.fims.base.DescriptionType;
import tv.fims.base.DescriptionsType;
import tv.fims.description.BmContentDescriptionType;

public class BMContentDialog extends ViewEditDialog<BMContentType>
{
    private final DescriptionsTablePanel myDescriptionsTablePanel;
    private final TablePanel<BMContentFormatType> myBmContentFormatTablePanel;

    private final List<DescriptionType> myDescriptions = new ArrayList<>();
    private final List<BMContentFormatType> myBmContentFormats = new ArrayList<>();

    public BMContentDialog(Window owner, AppController controller, boolean editable)
    {
        super(owner, controller, editable);

        myDescriptionsTablePanel = new DescriptionsTablePanel("Descriptions:", myDescriptions, new DescriptionsTableModel(), myController, myEditable);
        myBmContentFormatTablePanel = new BMContentFormatTablePanel("Business Media Content Formats:", myBmContentFormats, new BMContentFormatTableModel(), myController, myEditable);

        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0;
        c.gridwidth = 3;
        c.insets = new Insets(5, 5, 5, 5);
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 1;

        myPanel.add(myDescriptionsTablePanel, c);
        myPanel.add(myBmContentFormatTablePanel, c);

        setTitle();
        pack();
        setLocationRelativeTo(owner);
        refreshGUI();
    }

    @Override
    protected String getObjectName()
    {
        return "Business Media Content";
    }

    @Override
    protected boolean saveObject()
    {
        if (myObject == null) {
            myObject = new BMContentType();
            myObject.setResourceID(UUID.randomUUID().toString());
            myObject.setResourceCreationDate(XmlUtils.createCalendar());
        }
        myObject.setResourceModifiedDate(XmlUtils.createCalendar());

        DescriptionsType descriptions = new DescriptionsType();
        descriptions.getDescription().addAll(myDescriptions);
        myObject.setDescriptions(descriptions);

        BMContentFormatsType bmContentFormats = new BMContentFormatsType();
        bmContentFormats.getBmContentFormat().addAll(myBmContentFormats);
        myObject.setBmContentFormats(bmContentFormats);

        BMContentStatusType status = new BMContentStatusType();
        status.setStatus(BMStatusType.ONLINE);

        return true;
    }

    @Override
    protected void loadObject()
    {
        myDescriptions.clear();
        myBmContentFormats.clear();

        if (myObject != null) {
            DescriptionsType descriptions = myObject.getDescriptions();
            if (descriptions != null) {
                myDescriptions.addAll(descriptions.getDescription());
            }

            BMContentFormatsType bmContentFormats = myObject.getBmContentFormats();
            if (bmContentFormats != null) {
                myBmContentFormats.addAll(bmContentFormats.getBmContentFormat());
            }
        }
    }


    private class DescriptionsTableModel extends ScrollTableModel
    {
        private final String[] myColumnNames = {"Title", "Description"};
        private final int[] myColumnWidths = {200, 200};

        @Override
        public int getTableHeight()
        {
            return 100;
        }

        @Override
        public int getRowCount()
        {
            return myDescriptions.size();
        }

        @Override
        public int getColumnCount()
        {
            return myColumnNames.length;
        }

        @Override
        public String getColumnName(int column)
        {
            return myColumnNames[column];
        }

        @Override
        public int getColumnWidth(int column)
        {
            return myColumnWidths[column];
        }

        @Override
        public Object getValueAt(int rowIndex, int columnIndex)
        {
            DescriptionType description = myDescriptions.get(rowIndex);
            BmContentDescriptionType contentDescription = description.getBmContentDescription();

            switch (columnIndex) {
                case 0:
                    if (contentDescription != null && !contentDescription.getTitle().isEmpty()) {
                        return contentDescription.getTitle().get(0).getValue();
                    }
                    return "";
                case 1:
                    if (contentDescription != null && !contentDescription.getDescription().isEmpty()) {
                        return contentDescription.getDescription().get(0).getValue();
                    }
                    return "";
            }
            throw new IndexOutOfBoundsException();
        }
    }

    private class DescriptionsTablePanel extends TablePanel<DescriptionType>
    {
        public DescriptionsTablePanel(String title, List<DescriptionType> objects, ScrollTableModel tableModel, AppController controller, boolean editable)
        {
            super(title, objects, tableModel, controller, editable);
        }

        @Override
        protected ViewEditDialog<DescriptionType> getViewEditDialog(Window owner, AppController controller, boolean editable)
        {
            return new DescriptionDialog(owner, controller, editable);
        }

        @Override
        protected String getKey(DescriptionType object)
        {
            return object.getResourceID();
        }
    }

    private class BMContentFormatTableModel extends ScrollTableModel
    {
        private final String[] myColumnNames = {"Description"};
        private final int[] myColumnWidths = {200};

        @Override
        public int getTableHeight()
        {
            return 100;
        }

        @Override
        public int getRowCount()
        {
            return myBmContentFormats.size();
        }

        @Override
        public int getColumnCount()
        {
            return myColumnNames.length;
        }

        @Override
        public String getColumnName(int column)
        {
            return myColumnNames[column];
        }

        @Override
        public int getColumnWidth(int column)
        {
            return myColumnWidths[column];
        }

        @Override
        public Object getValueAt(int rowIndex, int columnIndex)
        {
            BMContentFormatType bmContentFormat = myBmContentFormats.get(rowIndex);
            switch (columnIndex) {
                case 0:
                    return StringUtils.toString(bmContentFormat);
            }
            throw new IndexOutOfBoundsException();
        }
    }

    private class BMContentFormatTablePanel extends TablePanel<BMContentFormatType>
    {
        public BMContentFormatTablePanel(String title, List<BMContentFormatType> objects, ScrollTableModel tableModel, AppController controller, boolean editable)
        {
            super(title, objects, tableModel, controller, editable);
        }

        @Override
        protected ViewEditDialog<BMContentFormatType> getViewEditDialog(Window owner, AppController controller, boolean editable)
        {
            return new BMContentFormatDialog(owner, controller, editable);
        }

        @Override
        protected String getKey(BMContentFormatType object)
        {
            return object.getResourceID();
        }
    }

}
