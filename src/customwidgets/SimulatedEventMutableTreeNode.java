package customwidgets;

import TimerDescriptionLanguage.SimulatedEvent;

import javax.swing.tree.DefaultMutableTreeNode;

/**
 * Created by mcochrane on 2/05/17.
 */
public class SimulatedEventMutableTreeNode extends DefaultMutableTreeNode {
    SimulatedEvent event;

    public SimulatedEventMutableTreeNode(SimulatedEvent event) {
        super(event, false);
        this.event = event;
    }

    public SimulatedEvent getEvent() {
        return event;
    }

    @Override
    public String toString() {
        return this.event.getTime().toString("yyyy-MM-dd hh:mm:ss");
    }
}
