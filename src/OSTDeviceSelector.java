import com.google.protobuf.UInt32Value;
import jssc.SerialPortException;
import jssc.SerialPortList;
import min.Frame;
import min.FrameReceiver;
import min.ReceivedFrameHandler;
import min.SerialHandler;
import org.joda.time.DateTime;
import org.joou.UInteger;

import javax.swing.*;
import java.awt.event.*;

public class OSTDeviceSelector extends JDialog {
    private JPanel contentPane;
    private JButton buttonOK;
    private JButton buttonCancel;
    private JButton buttonRefresh;
    private JComboBox<String> cbPortSelect;
    public String result = "";

    public OSTDeviceSelector() {
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

        buttonRefresh.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                refreshDeviceList();
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

        refreshDeviceList();

        pack();
        setLocationRelativeTo(null);
    }

    public void refreshDeviceList() {
        cbPortSelect.removeAllItems();
        buttonRefresh.setText("Refreshing .");

        String[] portNames = SerialPortList.getPortNames();
        FrameReceiver fr = new FrameReceiver();

        for (String name : portNames) {
            try {
                SerialHandler sh = new SerialHandler(name, 115200, fr);

                String dev_name = sh.frame_transmitter.sendGetDeviceTypeBlocking();

                // Refresh button animation...
                int copies = (buttonRefresh.getText().split(" ")[1].length() + 1) % 3;
                buttonRefresh.setText("Refreshing " +  new String(new char[copies]).replace("\0", "."));
                buttonRefresh.updateUI();

                if (dev_name.startsWith("OSTRev")) {
                    cbPortSelect.addItem(name + ": " + dev_name);
                }

                sh.Disconnect();
            } catch (SerialPortException ex) {
                //do nothing...
            }
        }
        buttonRefresh.setText("Refresh");
    }

    private void onOK() {
        result = ((String) cbPortSelect.getSelectedItem()).split(":")[0];
        dispose();
    }

    private void onCancel() {
        result = "";
        dispose();
    }
}
