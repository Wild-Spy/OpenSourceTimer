package TimerDescriptionLanguage;

/**
 * Created by mcochrane on 17/11/16.
 */
public class Channel {
    ChannelState state;

    Channel(ChannelState initialState) {
        this.state = initialState;
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

}
