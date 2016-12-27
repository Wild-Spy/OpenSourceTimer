package customwidgets;

import TimerDescriptionLanguage.ChannelState;
import TimerDescriptionLanguage.RuleState;
import org.joda.time.DateTime;

/**
 * Created by mcochrane on 19/12/16.
 */
public class GraphPoint {
    private DateTime dateTime;
    private ChannelState state;

    public GraphPoint(DateTime dateTime, ChannelState state) {
        this.dateTime = dateTime;
        this.state = state;
    }

    public GraphPoint(DateTime dateTime, RuleState state) {
        this.dateTime = dateTime;
        if (state == RuleState.ACTIVE) {
            this.state = ChannelState.ENABLED;
        } else {
            this.state = ChannelState.DISABLED;
        }
    }

    public GraphPoint(DateTime dateTime, boolean state) {
        this.dateTime = dateTime;
        if (state) {
            this.state = ChannelState.ENABLED;
        } else {
            this.state = ChannelState.DISABLED;
        }
    }

    public DateTime getDateTime() {
        return dateTime;
    }

    public ChannelState getState() {
        return state;
    }

//    public ChannelState getState(Channel chan) {
//        for (int i = 0; i < Channels.getInstance().getCount(); i++) {
//            Channel c = Channels.getInstance().get(i);
//            if (c == chan) {
//                state.get(i);
//            }
//        }
//        return null;
//    }
//
//    public ChannelState getState(int chanId) {
//        for (int i = 0; i < Channels.getInstance().getCount(); i++) {
//            Channel c = Channels.getInstance().get(i);
//            if (c.getId() == chanId) {
//                state.get(i);
//            }
//        }
//        return null;
//    }


}
