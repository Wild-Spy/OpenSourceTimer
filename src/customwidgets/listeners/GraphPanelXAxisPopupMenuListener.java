package customwidgets.listeners;

import org.joda.time.DateTime;

import java.awt.event.ActionEvent;
import java.util.EventListener;

/**
 * Created by mcochrane on 6/05/17.
 */
public interface GraphPanelXAxisPopupMenuListener extends EventListener {
    void createEventAction(DateTime eventTime);
}
