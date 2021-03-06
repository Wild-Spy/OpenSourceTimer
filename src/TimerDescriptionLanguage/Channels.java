package TimerDescriptionLanguage;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by mcochrane on 17/11/16.
 */
public class Channels {
    private Map<String, Channel> channels = new HashMap<>();

    private static Channels ourInstance = new Channels();

    public static Channels getInstance() {
        return ourInstance;
    }

    private Channels() {
        channels.put("1", new Channel(0, ChannelState.DISABLED));
        channels.put("2", new Channel(1, ChannelState.DISABLED));
        channels.put("3", new Channel(2, ChannelState.DISABLED));
        channels.put("4", new Channel(3, ChannelState.DISABLED));
    }

    public void allOff() {
        for (Channel c : channels.values()) {
            c.disable();
        }
    }

    public Channel get(String name) {
        return channels.get(name);
    }

    public Channel get(int index) {
        for (Channel c : channels.values()) {
            if (c.getId() == index) return c;
        }
        return null;
    }

    public int getCount() {
        return channels.size();
    }

    Map<String, Channel> getChannels() {
        return channels;
    }


}
