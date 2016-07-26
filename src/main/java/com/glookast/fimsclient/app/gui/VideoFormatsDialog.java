package com.glookast.fimsclient.app.gui;

import com.glookast.fimsclient.app.AppController;
import com.glookast.fimsclient.app.Service;
import com.glookast.fimsclient.app.gui.components.OKCancelButtonPanel;
import com.glookast.fimsclient.app.gui.components.ScrollTable;
import com.glookast.fimsclient.app.gui.components.ScrollTableModel;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.KeyStroke;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import tv.fims.base.VideoFormatType;

public class VideoFormatsDialog extends JDialog
{
    private final AppController myController;

    private final ActionListener myActionListener;

    private List<VideoFormatType> myVideoFormats;
    private final ScrollTableModel myTableModel;
    private final ScrollTable myTable;

    private final OKCancelButtonPanel myOKCancelPanel;

    private boolean myOK;

    private VideoFormatType mySelectedVideoFormat;

    public VideoFormatsDialog(Window owner, AppController controller, Service.Type serviceType)
    {
        super(owner, "Video Formats", ModalityType.APPLICATION_MODAL);
        setLayout(new GridBagLayout());
        setResizable(false);
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);

        myController = controller;

        myActionListener = new ActionListenerImpl();

        getRootPane().registerKeyboardAction(myActionListener, "Cancel", KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_IN_FOCUSED_WINDOW);

        myVideoFormats = new ArrayList<>();
        if (myController != null) {
            myVideoFormats.addAll(myController.getVideoFormats(serviceType));
        }
        myTableModel = new VideoFormatsTableModel();
        myTable = new ScrollTable(myTableModel);
        myTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        myTable.addListSelectionListener(new ListSelectionListenerImpl());
        myTable.setPreferredSize(new Dimension(myTable.getPreferredSize().width, 200));

        myOKCancelPanel = new OKCancelButtonPanel(myActionListener, true);

        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0;
        c.insets = new Insets(5, 5, 5, 5);
        c.weightx = 1;
        c.fill = GridBagConstraints.HORIZONTAL;
        add(myTable, c);
        add(myOKCancelPanel, c);

        pack();
        setLocationRelativeTo(owner);
    }

    private void refreshGUI()
    {
    }

    public boolean isOK()
    {
        return myOK;
    }

    public VideoFormatType getVideoFormat()
    {
        return mySelectedVideoFormat;
    }

    private class ActionListenerImpl implements ActionListener
    {
        @Override
        public void actionPerformed(ActionEvent e)
        {
            switch (e.getActionCommand()) {
                case "OK":
                    int index = myTable.convertRowIndexToModel(myTable.getSelectedRow());
                    mySelectedVideoFormat = (index >= 0) ? myVideoFormats.get(index) : null;
                    myOK = true;
                    dispose();
                    break;
                case "Cancel":
                    dispose();
                    break;
            }
        }
    }

    private class VideoFormatsTableModel extends ScrollTableModel
    {
        private final String[] myColumnNames = {"Codec", "Bitrate", "Bitrate Mode", "Width", "Height", "Frame Rate", "Scanning Format"};
        private final int[] myColumnWidths = {150, 80, 80, 50, 50, 80, 110};

        @Override
        public int getTableHeight()
        {
            return 200;
        }

        @Override
        public int getRowCount()
        {
            return myVideoFormats.size();
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
            VideoFormatType videoFormat = myVideoFormats.get(rowIndex);

            switch (columnIndex) {
                case 0:
                    if (videoFormat.getVideoEncoding() == null)
                        return "";
                    return videoFormat.getVideoEncoding().getName();
                case 1:
                    if (videoFormat.getBitRate() == null)
                        return "";
                    return videoFormat.getBitRate();
                case 2:
                    if (videoFormat.getBitRateMode() == null)
                        return "";
                    return String.valueOf(videoFormat.getBitRateMode()).toLowerCase();
                case 3:
                    if (videoFormat.getDisplayWidth() == null)
                        return "";
                    return videoFormat.getDisplayWidth().getValue();
                case 4:
                    if (videoFormat.getDisplayHeight() == null)
                        return "";
                    return videoFormat.getDisplayHeight().getValue();
                case 5:
                    if (videoFormat.getFrameRate() == null)
                        return "";
                    return String.format("%.2f", videoFormat.getFrameRate().getNumerator().doubleValue() / videoFormat.getFrameRate().getDenominator().doubleValue());
                case 6:
                    if (videoFormat.getScanningFormat() == null)
                        return "";
                    return String.valueOf(videoFormat.getScanningFormat()).toLowerCase();
            }
            throw new IndexOutOfBoundsException();
        }
    }

    private class ListSelectionListenerImpl implements ListSelectionListener
    {
        @Override
        public void valueChanged(ListSelectionEvent e)
        {
            refreshGUI();
        }
    }
}
