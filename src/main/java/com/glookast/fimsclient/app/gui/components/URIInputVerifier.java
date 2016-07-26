package com.glookast.fimsclient.app.gui.components;

import java.awt.Color;
import java.util.regex.Pattern;
import javax.swing.BorderFactory;
import javax.swing.InputVerifier;
import javax.swing.JComponent;
import javax.swing.border.Border;
import javax.swing.text.JTextComponent;

public class URIInputVerifier extends InputVerifier
{
    private Pattern myPattern;
    private Border myBorder;

    public URIInputVerifier()
    {
        myPattern = Pattern.compile("^(smb|nfs|cifs)://[a-zA-Z0-9-]+/[a-zA-Z0-9`~!@#$%^&(){}'._-]+([ ]+[a-zA-Z0-9`~!@#$%^&(){}'._-]+)*(/[^ \\\\/:*?\"\"<>|]+([ ]+[^ \\\\/:*?\"\"<>|]+)*)*/?$");
    }

    @Override
    public boolean verify(JComponent input)
    {
        if (input instanceof JTextComponent) {
            JTextComponent field = (JTextComponent) input;
            String text = field.getText();

            boolean verified = myPattern.matcher(text).find();

            if (myBorder == null) {
                myBorder = input.getBorder();
            }

            input.setBorder(verified ? myBorder : BorderFactory.createLineBorder(Color.red));

            return verified;
        }
        return false;
    }
}
