package customwidgets.menus;

import customwidgets.GraphPanelXAxis;
import customwidgets.listeners.GraphMarkerPopupMenuListener;
import customwidgets.listeners.GraphPanelXAxisPopupMenuListener;
import org.joda.time.DateTime;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Created by mcochrane on 6/05/17.
 */
public class GraphPanelXAxisPopupMenu extends JPopupMenu {
    private GraphPanelXAxisPopupMenuListener listener;
    GraphPanelXAxis parent;
    private JMenuItem anItem;
    private DateTime menuOpenLocation = null;

    public GraphPanelXAxisPopupMenu(GraphPanelXAxis parent, GraphPanelXAxisPopupMenuListener listener) {
        this.listener = listener;
        this.parent = parent;
        anItem = new JMenuItem("Add Simulated Event");
        anItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                listener.createEventAction(menuOpenLocation);
            }
        });

        add(anItem);
    }

    @Override
    public void show(Component component, int i, int i1) {
        this.menuOpenLocation = parent.parent.getXPosition(i);

        super.show(component, i, i1);
    }
}