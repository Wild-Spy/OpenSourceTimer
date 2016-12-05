package TimerDescriptionLanguage;

/**
 * Created by mcochrane on 17/11/16.
 */
public class Channel {
    private int id;
    private ChannelState state;

    Channel(ChannelState initialState) {
        this(Channels.getInstance().getChannels().size(), initialState);
    }

    Channel(int id, ChannelState initialState) {
        this.state = initialState;
        this.id = id;
    }

    public void enable() {
        this.state = ChannelState.ENABLED;
    }

    public void disable() {
        this.state = ChannelState.DISABLED;
    }

    public ChannelState getState() {
        return state;
    }

    public int getId() {
        return id;
    }
}
