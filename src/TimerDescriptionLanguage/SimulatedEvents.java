package TimerDescriptionLanguage;

import org.joda.time.DateTime;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by mcochrane on 26/12/16.
 */
public class SimulatedEvents {
    private static SimulatedEvents ourInstance = new SimulatedEvents();

    public static SimulatedEvents getInstance() {
        return ourInstance;
    }

    public static void resetInstance() { ourInstance = new SimulatedEvents(); }

    private SimulatedEvents() {}

    private List<SimulatedEvent> simulatedEvents = new ArrayList<>();

    public void addEvent(SimulatedEvent event) {
        simulatedEvents.add(event);
    }

    public List<DateTime> findEventTimes(String eventName) {
        List<DateTime> retList = new ArrayList<>();

        for (SimulatedEvent se : simulatedEvents) {
            if (eventName.equals(se.getName()))
                retList.add(se.getTime());
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


}
