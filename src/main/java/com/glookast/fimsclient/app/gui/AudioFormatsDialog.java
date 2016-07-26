package com.glookast.fimsclient.app.gui;

import com.glookast.fimsclient.app.AppController;
import com.glookast.fimsclient.app.Service;
import com.glookast.fimsclient.app.gui.components.OKCancelButtonPanel;
import com.glookast.fimsclient.app.gui.components.ScrollTable;
import com.glookast.fimsclient.app.gui.components.ScrollTableModel;
import java.awt.Dialog;
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
import tv.fims.base.AudioFormatType;

public class AudioFormatsDialog extends JDialog
{
    private final AppController myController;

    private final ActionListener myActionListener;

    private List<AudioFormatType> myAudioFormats;
    private final ScrollTableModel myTableModel;
    private final ScrollTable myTable;

    private final OKCancelButtonPanel myOKCancelPanel;

    private boolean myOK;

    private AudioFormatType mySelectedAudioFormat;

    public AudioFormatsDialog(Window owner, AppController controller, Service.Type serviceType)
    {
        super(owner, "Audio Formats", Dialog.ModalityType.APPLICATION_MODAL);
        setLayout(new GridBagLayout());
        setResizable(false);
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);

        myController = controller;

        myActionListener = new ActionListenerImpl();

        getRootPane().registerKeyboardAction(myActionListener, "Cancel", KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_IN_FOCUSED_WINDOW);

        myAudioFormats = new ArrayList<>();
        if (myController != null) {
            myAudioFormats.addAll(myController.getAudioFormats(serviceType));
        }
        myTableModel = new AudioFormatsTableModel();
        myTable = new ScrollTable(myTableModel);
        myTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        myTable.addListSelectionListener(new ListSelectionListenerImpl());

        myOKCancelPanel = new OKCancelButtonPanel(myActionListener, true);

        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0;
        c.insets = new Insets(5, 5, 5, 5);
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 1;
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

    public AudioFormatType getAudioFormat()
    {
        return mySelectedAudioFormat;
    }

    private class ActionListenerImpl implements ActionListener
    {
        @Override
        public void actionPerformed(ActionEvent e)
        {
            switch (e.getActionCommand()) {
                case "OK":
                    int index = myTable.convertRowIndexToModel(myTable.getSelectedRow());
                    mySelectedAudioFormat = (index >= 0) ? myAudioFormats.get(index) : null;
                    myOK = true;
                    dispose();
                    break;
                case "Cancel":
                    dispose();
                    break;
            }
        }
    }

    private class AudioFormatsTableModel extends ScrollTableModel
    {
        private final String[] myColumnNames = {"Codec", "Channels", "Sample Size", "Sampling Rate", "Bitrate", "Bitrate Mode"};
        private final int[] myColumnWidths = {150, 60, 80, 90, 80, 80};

        @Override
        public int getTableHeight()
        {
            return 200;
        }

        @Override
        public int getRowCount()
        {
            return myAudioFormats.size();
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
            AudioFormatType audioFormat = myAudioFormats.get(rowIndex);

            switch (columnIndex) {
                case 0:
                    if (audioFormat.getAudioEncoding() == null)
                        return "";
                    return audioFormat.getAudioEncoding().getName();
                case 1:
                    if (audioFormat.getChannels() == null)
                        return "";
                    return audioFormat.getChannels();
                case 2:
                    if (audioFormat.getSampleSize() == null)
                        return "";
                    return audioFormat.getSampleSize();
                case 3:
                    if (audioFormat.getSamplingRate() == null)
                        return "";
                    return audioFormat.getSamplingRate();
                case 4:
                    if (audioFormat.getBitRate() == null)
                        return "";
                    return audioFormat.getBitRate();
                case 5:
                    if (audioFormat.getBitRateMode() == null)
                        return "";
                    return String.valueOf(audioFormat.getBitRateMode()).toLowerCase();
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
