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
    }

    public void refreshDeviceList() {
        cbPortSelect.removeAllItems();
        buttonRefresh.setText("Refreshing .");

        String[] portNames = SerialPortList.getPortNames();
        FrameReceiver fr = new FrameReceiver();

        for (String name : portNames) {
            try {
                SerialHandler sh = new SerialHandler(name, 115200, fr);

                final Long[] devType = {null};

                fr.setResponseCallback(new ReceivedFrameHandler() {
                    @Override
                    public void handleReceivedFrame(Frame frame) {
                        devType[0] = SerialHandler.min_decode_unsigned(frame.get_payload());
                    }
                });

                sh.frame_transmitter.sendGetDeviceType();

                DateTime sendTime = DateTime.now();
                long timeout_ms = 500;

                // Wait for response or timeout
                while ((DateTime.now().getMillis() - sendTime.getMillis()) < timeout_ms && devType[0] == null);

                int copies = (buttonRefresh.getText().split(" ")[1].length() + 1) % 3;
                buttonRefresh.setText("Refreshing " +  new String(new char[copies]).replace("\0", "."));
                buttonRefresh.updateUI();

                if (devType[0] != null) {
                    cbPortSelect.addItem(name + ": " + Long.toHexString(devType[0]));
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
