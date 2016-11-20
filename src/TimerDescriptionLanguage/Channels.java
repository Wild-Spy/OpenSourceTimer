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
        channels.put("1", new Channel(ChannelState.DISABLED));
        channels.put("2", new Channel(ChannelState.DISABLED));
        channels.put("3", new Channel(ChannelState.DISABLED));
        channels.put("4", new Channel(ChannelState.DISABLED));
    }

    public Channel get(String name) {
        return channels.get(name);
    }

    Map<String, Channel> getChannels() {
        return channels;
    }

}
