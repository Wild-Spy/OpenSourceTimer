import jssc.SerialPortList;

import javax.swing.*;
import java.awt.event.*;

public class DeviceSelector extends JDialog {
    private JPanel contentPane;
    private JButton buttonOK;
    private JButton buttonCancel;
    private JComboBox<String> cbPortSelect;
    public String result = "";

    public DeviceSelector() {
        setContentPane(contentPane);
        setModal(true);
        getRootPane().setDefaultButton(buttonOK);

        buttonOK.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onOK();
            }
        });

        buttonCancel.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onCancel();
            }
        });

        // call onCancel() when cross is clicked
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                onCancel();
            }
        });

        // call onCancel() on ESCAPE
        contentPane.registerKeyboardAction(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onCancel();
            }
        }, KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);

        String[] portNames = SerialPortList.getPortNames();
//        System.out.println("Serial Port Names:");
        for (String name : portNames) {
//            System.out.println(name);
            cbPortSelect.addItem(name);
        }

        pack();
    }

    private void onOK() {
        result = (String) cbPortSelect.getSelectedItem();
        dispose();
    }

    private void onCancel() {
        result = "";
        dispose();
    }
}
