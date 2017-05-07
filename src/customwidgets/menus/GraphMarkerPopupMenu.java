package customwidgets.menus;

import customwidgets.listeners.GraphMarkerPopupMenuListener;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Created by mcochrane on 6/05/17.
 */
public class GraphMarkerPopupMenu extends JPopupMenu {
    private GraphMarkerPopupMenuListener listener;

    public GraphMarkerPopupMenu(GraphMarkerPopupMenuListener listener) {
        this.listener = listener;

        JMenuItem anItem = new JMenuItem("Delete Simulated Event");
        anItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                listener.deleteAction(actionEvent);
            }
        });
        add(anItem);
    }
}
