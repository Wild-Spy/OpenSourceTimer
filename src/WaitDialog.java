import javax.swing.*;
import java.awt.event.*;

public class WaitDialog extends JDialog {
    private JPanel contentPane;
    private JButton buttonOK;
    private JButton buttonAbort;
    private JProgressBar progressBar;
    private JLabel displayLabel;
    private int max_prog;

    public WaitDialog(String display_text, int max_prog) {
        setContentPane(contentPane);
        setModal(true);
        getRootPane().setDefaultButton(buttonAbort);

        buttonAbort.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onAbort();
            }
        });

        // call onCancel() when cross is clicked
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                onAbort();
            }
        });

        // call onCancel() on ESCAPE
        contentPane.registerKeyboardAction(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onAbort();
            }
        }, KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);

        int n = 100;
        displayLabel.setText(String.format("%0" + n + "d", 0).replace("0", "a"));
        progressBar.setMaximum(max_prog);

        pack();
        displayLabel.setText(display_text);
    }

    public void update(int progress) {
        progressBar.setValue(progress);
        progressBar.updateUI();
    }

    public void increment_update() {
        progressBar.setValue(progressBar.getValue()+1);
        progressBar.updateUI();
    }

    public void update_text(String text) {
        displayLabel.setText(text);
    }

    private void onAbort() {
        // add your code here if necessary
        dispose();
    }
}
