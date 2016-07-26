package com.glookast.fimsclient.app.gui.components;

import com.glookast.fimsclient.app.utils.XmlUtils;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.math.BigInteger;
import java.text.NumberFormat;
import java.util.Date;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFormattedTextField;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerDateModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import tv.fims.base.ListFilterType;

public class ListFilterPanel extends JPanel
{
    private final ActionListener myActionListener;

    private final JCheckBox myQueuedCheckBox;
    private final JCheckBox myActiveCheckBox;
    private final JCheckBox myFinishedCheckBox;
    private final JCheckBox myFailedCheckBox;

    private final JCheckBox myFromDateCheckBox;
    private final JSpinner myFromDateSpinner;

    private final JCheckBox myToDateCheckBox;
    private final JSpinner myToDateSpinner;

    private final JCheckBox myMaxNumberResultsCheckBox;
    private final JFormattedTextField myMaxNumberResultsField;

    private final JButton myQueryButton;

    public ListFilterPanel(ActionListener actionListener)
    {
        super(new GridBagLayout());

        myActionListener = actionListener;

        myQueuedCheckBox = new JCheckBox("Queued");
        myQueuedCheckBox.setSelected(true);

        myActiveCheckBox = new JCheckBox("Active");
        myActiveCheckBox.setSelected(true);

        myFinishedCheckBox = new JCheckBox("Finished");
        myFinishedCheckBox.setSelected(true);

        myFailedCheckBox = new JCheckBox("Failed");
        myFailedCheckBox.setSelected(true);

        myFromDateCheckBox = new JCheckBox("From");
        myFromDateSpinner = new JSpinner(new SpinnerDateModel());
        myFromDateSpinner.setEditor(new JSpinner.DateEditor(myFromDateSpinner, "yyyy-MM-dd HH:mm:ss"));
        myFromDateSpinner.setValue(new Date((System.currentTimeMillis() - 10800000) / 3600000 * 3600000));
        myFromDateSpinner.addChangeListener(new ChangeListenerImpl());

        myToDateCheckBox = new JCheckBox("To");
        myToDateSpinner = new JSpinner(new SpinnerDateModel());
        myToDateSpinner.setEditor(new JSpinner.DateEditor(myToDateSpinner, "yyyy-MM-dd HH:mm:ss"));
        myToDateSpinner.setValue(new Date((System.currentTimeMillis() + 10800000) / 3600000 * 3600000));
        myFromDateSpinner.addChangeListener(new ChangeListenerImpl());

        myMaxNumberResultsCheckBox = new JCheckBox("Max Results");
        myMaxNumberResultsCheckBox.setActionCommand("Filter");
        myMaxNumberResultsCheckBox.addActionListener(actionListener);myMaxNumberResultsField = new JFormattedTextField(NumberFormat.getIntegerInstance());
        myMaxNumberResultsField.setValue(100L);
        myMaxNumberResultsField.addActionListener(actionListener);

        myQueryButton = new JButton("Query");
        myQueryButton.setActionCommand("Query");
        myQueryButton.addActionListener(actionListener);

        GridBagConstraints c = new GridBagConstraints();
        c.insets.right = 5;
        add(myQueuedCheckBox, c);
        c.insets.left = 5;
        add(myActiveCheckBox, c);
        add(myFinishedCheckBox, c);
        add(myFailedCheckBox, c);
        c.insets.right = 0;
        add(myFromDateCheckBox, c);
        c.insets.left = 0;
        c.insets.right = 5;
        add(myFromDateSpinner, c);
        c.insets.left = 5;
        c.insets.right = 0;
        add(myToDateCheckBox, c);
        c.insets.left = 0;
        c.insets.right = 5;
        add(myToDateSpinner, c);
        c.insets.left = 5;
        c.insets.right = 0;
        add(myMaxNumberResultsCheckBox, c);
        c.insets.left = 0;
        c.insets.right = 5;
        add(myMaxNumberResultsField, c);
        c.insets.left = 5;
        c.insets.right = 0;
        add(myQueryButton, c);

        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 1;
        add(Box.createGlue(), c);
    }

    public ListFilterType getFilter()
    {
        ListFilterType filter = new ListFilterType();

        filter.setIncludeQueued(myQueuedCheckBox.isSelected());
        filter.setIncludeActive(myActiveCheckBox.isSelected());
        filter.setIncludeFinished(myFinishedCheckBox.isSelected());
        filter.setIncludeFailed(myFailedCheckBox.isSelected());

        if (myFromDateCheckBox.isSelected()) {
            filter.setFromDate(XmlUtils.createCalendar((Date)myFromDateSpinner.getValue()));
        }

        if (myToDateCheckBox.isSelected()) {
            filter.setToDate(XmlUtils.createCalendar((Date)myToDateSpinner.getValue()));
        }

        if (myMaxNumberResultsCheckBox.isSelected()) {
            filter.setMaxNumberResults(BigInteger.valueOf((long)myMaxNumberResultsField.getValue()));
        }

        return filter;
    }

    private class ChangeListenerImpl implements ChangeListener
    {
        @Override
        public void stateChanged(ChangeEvent e)
        {
            myActionListener.actionPerformed(new ActionEvent(e.getSource(), ActionEvent.ACTION_PERFORMED, "Filter"));
        }
    }

}
