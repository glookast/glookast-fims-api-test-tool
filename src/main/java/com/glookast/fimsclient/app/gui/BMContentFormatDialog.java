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
import tv.fims.base.BMEssenceLocatorType;
import tv.fims.base.BMEssenceLocatorsType;
import tv.fims.base.SimpleFileLocatorType;

public class BMContentFormatDialog extends ViewEditDialog<BMContentFormatType>
{
    private final BMEssenceLocatorTablePanel myBmEssenceLocatorTablePanel;

    private final List<BMEssenceLocatorType> myBmEssenceLocators = new ArrayList<>();

    public BMContentFormatDialog(Window owner, AppController controller, boolean editable)
    {
        super(owner, controller, editable);

        myBmEssenceLocatorTablePanel = new BMEssenceLocatorTablePanel("Business Media Essence Locators:", myBmEssenceLocators, new BMEssenceLocatorlTableModel(), controller, editable);

        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0;
        c.gridwidth = 3;
        c.insets = new Insets(5, 5, 5, 5);
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 1;

        myPanel.add(myBmEssenceLocatorTablePanel, c);

        setTitle();
        pack();
        setLocationRelativeTo(owner);
        refreshGUI();
    }

    @Override
    protected String getObjectName()
    {
        return "Business Media Content Format";
    }

    @Override
    protected boolean saveObject()
    {
        if (myObject == null) {
            myObject = new BMContentFormatType();
            myObject.setResourceID(UUID.randomUUID().toString());
            myObject.setResourceCreationDate(XmlUtils.createCalendar());
        }
        myObject.setResourceModifiedDate(XmlUtils.createCalendar());

        BMEssenceLocatorsType bmEssenceLocators = new BMEssenceLocatorsType();
        bmEssenceLocators.getBmEssenceLocator().addAll(myBmEssenceLocators);
        myObject.setBmEssenceLocators(bmEssenceLocators);

        return true;
    }

    @Override
    protected void loadObject()
    {
        myBmEssenceLocators.clear();

        if (myObject != null) {
            BMEssenceLocatorsType bmEssenceLocators = myObject.getBmEssenceLocators();
            if (bmEssenceLocators != null) {
                myBmEssenceLocators.addAll(bmEssenceLocators.getBmEssenceLocator());
            }
        }
    }

    private class BMEssenceLocatorlTableModel extends ScrollTableModel
    {
        private final String[] myColumnNames = {"Description"};
        private final int[] myColumnWidths = {400};

        @Override
        public int getTableHeight()
        {
            return 100;
        }

        @Override
        public int getRowCount()
        {
            return myBmEssenceLocators.size();
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
            BMEssenceLocatorType bmEssenceLocator = myBmEssenceLocators.get(rowIndex);
            switch (columnIndex) {
                case 0:
                    return StringUtils.toString(bmEssenceLocator);

            }
            throw new IndexOutOfBoundsException();
        }
    }

    private class BMEssenceLocatorTablePanel extends TablePanel<BMEssenceLocatorType>
    {
        public BMEssenceLocatorTablePanel(String title, List<BMEssenceLocatorType> objects, ScrollTableModel tableModel, AppController controller, boolean editable)
        {
            super(title, objects, tableModel, controller, editable);
        }

        @Override
        protected ViewEditDialog<BMEssenceLocatorType> getViewEditDialog(Window owner, AppController controller, boolean editable)
        {
            return new BMEssenceLocatorDialog(owner, controller, editable);
        }

        @Override
        protected String getKey(BMEssenceLocatorType object)
        {
            throw new UnsupportedOperationException("Not supported yet.");
        }
    }

}
