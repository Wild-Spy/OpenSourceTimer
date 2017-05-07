package TimerDescriptionLanguage;

import customwidgets.listeners.GraphMarkerChangedListener;
import customwidgets.SimulatedEventMutableTreeNode;
import org.joda.time.DateTime;

import javax.swing.*;
import javax.swing.tree.*;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Objects;

/**
 * Created by mcochrane on 26/12/16.
 */
public class SimulatedEvents {
    private static SimulatedEvents ourInstance = new SimulatedEvents();
    public static SimulatedEvents getInstance() {
        return ourInstance;
    }
    public static void resetInstance() { ourInstance = new SimulatedEvents(); }

    JTree tree = null;

    private SimulatedEvents() {}

    private SimulatedEvents(JTree tree) {
        updateUiTreeView(tree);
    }

    private List<SimulatedEvent> simulatedEvents = new ArrayList<>();

    public void addEvent(SimulatedEvent event) {
        simulatedEvents.add(event);
        addEventToTree(event);
    }

    private void addEventToTree(SimulatedEvent event) {
        if (tree == null) return;

        DefaultTreeModel model = (DefaultTreeModel) tree.getModel();
        DefaultMutableTreeNode root = (DefaultMutableTreeNode)model.getRoot();

        DefaultMutableTreeNode eventNameNode = findEventNameInTree(event.getName());

        if (eventNameNode == null) {
            //need to add event name node...
            eventNameNode = new DefaultMutableTreeNode(event.getName());
            root.add(eventNameNode);
        }

        SimulatedEventMutableTreeNode timeNode = new SimulatedEventMutableTreeNode(event);
        eventNameNode.add(timeNode);
        event.getMarker().setOnChangeCallback(new GraphMarkerChangedListener() {
            @Override
            public void onChange(DateTime newTime) {
                event.setTime(newTime);
                model.nodeChanged(timeNode);
            }
        });
        tree.updateUI();
        tree.expandPath(new TreePath(eventNameNode.getPath()));
        tree.updateUI();
    }

    public void removeEvent(SimulatedEvent event) {
        simulatedEvents.remove(event);
        event.deleteMarker();
        removeTreeItem(event);
    }

    public void setAllMarkersNotSelected() {
        for (SimulatedEvent se : simulatedEvents) {
            se.getMarker().setSelected(false);
        }
    }

    public List<DateTime> findEventTimes(String eventName) {
        List<DateTime> retList = new ArrayList<>();

        for (SimulatedEvent se : simulatedEvents) {
            if (eventName.equals(se.getName()))
                retList.add(se.getTime());
        }
        return retList;
    }

    public List<SimulatedEvent> findEventInstances(String eventName) {
        List<SimulatedEvent> retList = new ArrayList<>();

        for (SimulatedEvent se : simulatedEvents) {
            if (eventName.equals(se.getName()))
                retList.add(se);
        }
        return retList;
    }

    public List<String> getSimulatedEventNames() {
        List<String> retList = new ArrayList<>();

        for (SimulatedEvent se : simulatedEvents) {
            if (!retList.contains(se.getName()))
                retList.add(se.getName());
        }
        return retList;
    }

    private void removeTreeItem(SimulatedEvent event) {
        if (tree == null) return;
        DefaultTreeModel model = (DefaultTreeModel) tree.getModel();

        SimulatedEventMutableTreeNode foundNode = findEventInTree(event);
        if (foundNode == null) return;
        DefaultMutableTreeNode parent = (DefaultMutableTreeNode) foundNode.getParent();
        model.removeNodeFromParent(foundNode);
        if (parent.getChildCount() == 0) {
            model.removeNodeFromParent(parent);
        }
    }

    private SimulatedEventMutableTreeNode findEventInTree(SimulatedEvent event) {
        if (tree == null) return null;
        DefaultMutableTreeNode root = (DefaultMutableTreeNode) tree.getModel().getRoot();

        Enumeration en = root.depthFirstEnumeration();
        while (en.hasMoreElements()) {
            DefaultMutableTreeNode node = (DefaultMutableTreeNode) en.nextElement();
            if (node.getClass() == SimulatedEventMutableTreeNode.class) {
                if (((SimulatedEventMutableTreeNode) node).getEvent() == event) {
                    return (SimulatedEventMutableTreeNode) node;
                }
            }
        }
        return null;
    }


    private DefaultMutableTreeNode findEventNameInTree(String eventName) {
        if (tree == null) return null;
        DefaultMutableTreeNode root = (DefaultMutableTreeNode) tree.getModel().getRoot();

        Enumeration en = root.depthFirstEnumeration();
        while (en.hasMoreElements()) {
            DefaultMutableTreeNode node = (DefaultMutableTreeNode) en.nextElement();
            if (node.getClass() == DefaultMutableTreeNode.class) {
                if (Objects.equals(node.toString(), eventName)) {
                    return node;
                }
            }
        }
        return null;
    }

    public void updateUiTreeView(JTree uiTree) {
        this.tree = uiTree;
        ((DefaultMutableTreeNode)tree.getModel().getRoot()).removeAllChildren();

        for (SimulatedEvent event : simulatedEvents) {
            addEventToTree(event);
        }

        tree.setRootVisible(false);
        tree.setShowsRootHandles(true);
        tree.updateUI();
    }

//    public void updateUiTreeView(JTree uiTree) {
//        this.tree = uiTree;
//        DefaultTreeModel model = (DefaultTreeModel) tree.getModel();
//        DefaultMutableTreeNode root = (DefaultMutableTreeNode)model.getRoot();
//        root.removeAllChildren();
//
//        for (String eventName : getSimulatedEventNames()) {
//            DefaultMutableTreeNode node = new DefaultMutableTreeNode(eventName);
//            for (SimulatedEvent eventInst : findEventInstances(eventName)) {
//                SimulatedEventMutableTreeNode timeNode = new SimulatedEventMutableTreeNode(eventInst);
//                node.add(timeNode);
//                eventInst.getMarker().setOnChangeCallback(new GraphMarkerChangedListener() {
//                    @Override
//                    public void onChange(DateTime newTime) {
//                        eventInst.setTime(newTime);
////                        DefaultTreeModel model = (DefaultTreeModel) tree.getModel();
//                        model.nodeChanged(timeNode);
//                    }
//                });
//            }
//            tree.expandPath(new TreePath(node));
//            root.add(node);
//        }
//
//        tree.setRootVisible(false);
//        tree.setShowsRootHandles(true);
//        tree.updateUI();
//    }

}
