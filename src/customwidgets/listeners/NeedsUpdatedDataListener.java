package customwidgets.listeners;

import org.joda.time.Duration;
import org.joda.time.Interval;

import java.util.EventListener;

/**
 * Created by mcochrane on 29/11/16.
 */
public interface NeedsUpdatedDataListener extends EventListener {
    void needsUpdatedData(Interval window);
}
