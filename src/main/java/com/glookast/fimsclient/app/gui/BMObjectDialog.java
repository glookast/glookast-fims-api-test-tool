package com.glookast.fimsclient.app.gui;

import com.glookast.fimsclient.app.AppController;
import com.glookast.fimsclient.app.gui.components.ViewEditDialog;
import com.glookast.fimsclient.app.gui.components.ScrollTableModel;
import com.glookast.fimsclient.app.gui.components.TablePanel;
import com.glookast.fimsclient.app.utils.StringUtils;
import com.glookast.fimsclient.app.utils.XmlUtils;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.awt.Window;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import tv.fims.base.BMContentType;
import tv.fims.base.BMContentsType;
import tv.fims.base.BMObjectType;

public class BMObjectDialog extends ViewEditDialog<BMObjectType>
{
    private final BMContentsTableModel myBmContentsTableModel;
    private final BMContentsTablePanel myBmContentsTablePanel;

    private final List<BMContentType> myBmContents = new ArrayList<>();

    public BMObjectDialog(Window owner, AppController controller, boolean editable)
    {
        super(owner, controller, editable);

        myBmContentsTableModel = new BMContentsTableModel();
        myBmContentsTablePanel = new BMContentsTablePanel("Business Media Contents:", myBmContents, myBmContentsTableModel, myController, myEditable);

        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0;
        c.gridwidth = 3;
        c.insets = new Insets(5, 5, 5, 5);
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 1;

        myPanel.add(myBmContentsTablePanel, c);

        setTitle();
        pack();
        setLocationRelativeTo(owner);
        refreshGUI();
    }

    @Override
    protected String getObjectName()
    {
        return "Business Media Object";
    }

    @Override
    protected boolean saveObject()
    {
        if (myObject == null) {
            myObject = new BMObjectType();
            myObject.setResourceID(UUID.randomUUID().toString());
            myObject.setResourceCreationDate(XmlUtils.createCalendar());
        }
        myObject.setResourceModifiedDate(XmlUtils.createCalendar());

        BMContentsType bmContents = new BMContentsType();
        bmContents.getBmContent().addAll(myBmContents);
        myObject.setBmContents(bmContents);

        return true;
    }

    @Override
    protected void loadObject()
    {
        myBmContents.clear();

        if (myObject != null) {
            BMContentsType bmContents = myObject.getBmContents();
            if (bmContents != null) {
                myBmContents.addAll(bmContents.getBmContent());
            }
        }
    }

    private class BMContentsTableModel extends ScrollTableModel
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
            return myBmContents.size();
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
            BMContentType bmContent = myBmContents.get(rowIndex);
            switch (columnIndex) {
                case 0:
                    return StringUtils.toString(bmContent);
            }
            throw new IndexOutOfBoundsException();
        }
    }

    private class BMContentsTablePanel extends TablePanel<BMContentType>
    {
        public BMContentsTablePanel(String title, List<BMContentType> objects, ScrollTableModel tableModel, AppController controller, boolean editable)
        {
            super(title, objects, tableModel, controller, editable);
        }

        @Override
        protected ViewEditDialog<BMContentType> getViewEditDialog(Window owner, AppController controller, boolean editable)
        {
            return new BMContentDialog(owner, controller, editable);
        }

        @Override
        protected String getKey(BMContentType object)
        {
            return object.getResourceID();
        }
    }


}
