package TimerDescriptionLanguage;

import customwidgets.GraphMarker;
import customwidgets.GraphPanel;
import customwidgets.listeners.GraphMarkerPopupMenuListener;
import customwidgets.listeners.MouseClickedListener;
import customwidgets.menus.GraphMarkerPopupMenu;
import org.joda.time.DateTime;

import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;

/**
 * Created by mcochrane on 26/12/16.
 */
public class SimulatedEvent {
    private String name;
    private DateTime time;
    private GraphPanel graphPanel;
    private GraphMarker graphMarker;

    public SimulatedEvent(String name, DateTime time) {
        this.name = name;
        this.time = time;
    }

    public SimulatedEvent(String name, DateTime time, GraphPanel graphPanel) {
        this.name = name;
        this.time = time;
        this.graphPanel = graphPanel;
        this.graphMarker = this.graphPanel.addMarker(time, name);
        this.graphMarker.setIsDragable(true);
        this.graphMarker.setMouseClickedListener(new MouseClickedListener() {
            @Override
            public void mouseClicked(MouseEvent event) {
                if (event.getButton() != 1) return;
                SimulatedEvents.getInstance().setAllMarkersNotSelected();
                graphMarker.setSelected(true);
                SimulatedEvents.getInstance().setSelectedTreeItem(SimulatedEvent.this);
            }
        });
        this.graphMarker.setPopupMenu(new GraphMarkerPopupMenu(new GraphMarkerPopupMenuListener() {
            @Override
            public void deleteAction(ActionEvent actionEvent) {
                SimulatedEvents.getInstance().removeEvent(SimulatedEvent.this);
                graphPanel.repaint();
            }
        }));
    }

    public void deleteMarker() {
        this.graphPanel.deleteMarker(this.graphMarker);
    }

    public String getName() {
        return this.name;
    }

    public DateTime getTime() {
        return this.time;
    }

    public GraphMarker getMarker() {
        return this.graphMarker;
    }

    public void setTime(DateTime newTime) {
        this.time = newTime;
    }

}
