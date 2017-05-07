package customwidgets.listeners;

import org.joda.time.DateTime;

import java.util.EventListener;

public interface GraphMarkerChangedListener extends EventListener {
    void onChange(DateTime newTime);
}
