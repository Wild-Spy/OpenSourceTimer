import TimerDescriptionLanguage.SimulatedEvent;
import customwidgets.ImagePanel;
import customwidgets.SimulatedEventMutableTreeNode;
import customwidgets.listeners.GraphMarkerChangedListener;
import org.joda.time.DateTime;
import protobuf.hardware.Port;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.List;

/**
 * Created by mcochrane on 7/05/17.
 */
public class FrmDevice {
    private JFrame frameRoot;
    private FrmMain frmMain;
    private JPanel panelRoot;
    private ImagePanel imgDev;
    private JTree treeDesc;
    private JLabel lblTitle;
    private JRadioButton rbFront;
    private JRadioButton rbBack;
    private JRadioButton rbAngled;
    private HardwareType hardwareType;
    private String cur_img_name = "Front";

    public FrmDevice(FrmMain frmMain) {
        JFrame.setDefaultLookAndFeelDecorated(true);
        JDialog.setDefaultLookAndFeelDecorated(true);

        this.frmMain = frmMain;

        frameRoot = new JFrame("Open Source Timer: Device");
        ImageIcon icon = new ImageIcon("src/resources/timer-icon.png");
        frameRoot.setIconImage(icon.getImage());
        frameRoot.setContentPane(panelRoot);
        frameRoot.setJMenuBar(createMenuBar());
        frameRoot.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
        frameRoot.pack();
        frameRoot.setLocationRelativeTo(null);
        frameRoot.setVisible(true);

        rbAngled.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                if (e.getStateChange() == ItemEvent.SELECTED) {
                    updateHardwareImage("Angled");
                }
            }
        });

        rbFront.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                if (e.getStateChange() == ItemEvent.SELECTED) {
                    updateHardwareImage("Front");
                }
            }
        });

        rbBack.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                if (e.getStateChange() == ItemEvent.SELECTED) {
                    updateHardwareImage("Back");
                }
            }
        });

        List<HardwareType> allHardware = HardwareType.getAll();

        setHardwareType(allHardware.get(1));
    }

    public void setVisible(boolean visible) {
        frameRoot.setVisible(visible);
    }

    public void setHardwareType(HardwareType hardwareType) {
        this.hardwareType = hardwareType;
        this.lblTitle.setText(hardwareType.getDesc().getName());
        addHardwareInfoToTreeView();
        updateHardwareImage();
    }

    private void updateHardwareImage() {
        updateHardwareImage(this.cur_img_name);
    }
    private void updateHardwareImage(String img_name) {
        this.cur_img_name = img_name;
        String img_path = "src/resources/hardware/" + this.hardwareType.getName() + "/PCB_" + img_name + ".png";
        imgDev.setImage(img_path);
        imgDev.updateUI();
    }

    private void addHardwareInfoToTreeView() {
        DefaultTreeModel model = (DefaultTreeModel) treeDesc.getModel();
        DefaultMutableTreeNode root = (DefaultMutableTreeNode)model.getRoot();
        root.removeAllChildren();

        DefaultMutableTreeNode node = addItemToTreeNode(hardwareType.getDesc().getName(), root);
        DefaultMutableTreeNode node1 = addItemToTreeNode("Channels: " + hardwareType.getDesc().getNumChannels(), node);
        addPortInfoToTreeView(hardwareType.getDesc().getPortsList(), node);

        treeDesc.setRootVisible(false);
        treeDesc.setShowsRootHandles(true);
        treeDesc.updateUI();
    }

    private void addPortInfoToTreeView(List<Port.HardwarePort> ports, DefaultMutableTreeNode parent) {
//        DefaultMutableTreeNode portRoot = addItemToTreeNode("Ports (" + ports.size() + ")", parent);
        DefaultMutableTreeNode portRoot = addItemToTreeNode("Ports", parent);
        for (Port.HardwarePort port : ports) {
            if (!port.getDevOnly())
                addPortInfoToTreeView(port, portRoot);
        }
    }

    private void addPortInfoToTreeView(Port.HardwarePort port, DefaultMutableTreeNode parent) {
        DefaultMutableTreeNode portRoot = addItemToTreeNode(port.getDesignator(), parent);

        addItemToTreeNode("Designator: " + port.getDesignator(), portRoot);
        addItemToTreeNode("Description: " + port.getDescription(), portRoot);
        addItemToTreeNode("Location: " + port.getLocation(), portRoot);
        addItemToTreeNode("Pins: " + port.getNumPins(), portRoot);
        if (port.getCanPowerDevice()) {
            addItemToTreeNode("Can Power Device", portRoot);
        }
        if (port.hasMinInputVoltage()) {
            addItemToTreeNode("Min Input Voltage:" + port.getMinInputVoltage(), portRoot);
        }
        if (port.hasMaxInputVoltage()) {
            addItemToTreeNode("Max Input Voltage:" + port.getMaxInputVoltage(), portRoot);
        }

        treeDesc.collapsePath(new TreePath(portRoot.getPath()));
    }

    private DefaultMutableTreeNode addItemToTreeNode(String text, DefaultMutableTreeNode parent) {
        return addItemToTreeNode(text, parent, true);
    }

    private DefaultMutableTreeNode addItemToTreeNode(String text, DefaultMutableTreeNode parent, boolean expandParent) {
        DefaultMutableTreeNode node = new DefaultMutableTreeNode(text);
        parent.add(node);
        if (expandParent)
            treeDesc.expandPath(new TreePath(parent.getPath()));
        return node;
    }

    private JMenuBar createMenuBar() {
        JMenuBar menu_bar = new JMenuBar();
//        menu_bar.setBackground(GraphPanel.backgroundColor);
//        menu_bar.setForeground(GraphPanel.gridColor);
        JMenu menu_file = new JMenu("File");

        JMenuItem menu_item_new = new JMenuItem("New");
//        menu_item_new.addActionListener(new FrmMain.MenuNewActionListener());
        menu_file.add(menu_item_new);

        JMenuItem menu_item_open = new JMenuItem("Open");
//        menu_item_open.addActionListener(new FrmMain.MenuOpenActionListener());
        menu_file.add(menu_item_open);

        JMenuItem menu_item_save = new JMenuItem("Save");
//        menu_item_save.addActionListener(new FrmMain.MenuSaveActionListener());
        menu_file.add(menu_item_save);

        menu_bar.add(menu_file);

        return menu_bar;
    }

    private void createUIComponents() {
        // TODO: place custom component creation code here
        imgDev = new ImagePanel();
        imgDev.setImage("src/resources/hardware/OSTRev0/PCB_Front.png");
    }

}
