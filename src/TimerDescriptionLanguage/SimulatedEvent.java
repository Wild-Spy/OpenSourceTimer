package TimerDescriptionLanguage;

import org.joda.time.DateTime;

/**
 * Created by mcochrane on 26/12/16.
 */
public class SimulatedEvent {
    String name;
    DateTime time;

    public SimulatedEvent(String name, DateTime time) {
        this.name = name;
        this.time = time;
    }

    public String getName() {
        return this.name;
    }

    public DateTime getTime() {
        return this.time;
    }

}
