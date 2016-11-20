package TimerDescriptionLanguage;

import org.joda.time.DateTime;

/**
 * Created by mcochrane on 13/11/16.
 */
public class ChannelActivator extends Activator {
    private Channel channel;

    public ChannelActivator(String channelName, ActivatorState defaultState) {
        super(defaultState);
        this.channel = Channels.getInstance().get(channelName);
    }

    public ChannelActivator(Channel channel, ActivatorState defaultState) {
        super(defaultState);
        this.channel = channel;
    }

    @Override
    public void enable(DateTime now) {
        super.enable(now);
        channel.enable();
    }

    @Override
    public void disable() {
        super.disable();
        channel.disable();
    }

    public Channel getChannel() {
        return channel;
    }

}
