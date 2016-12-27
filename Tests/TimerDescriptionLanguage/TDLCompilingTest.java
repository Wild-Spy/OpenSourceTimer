package TimerDescriptionLanguage;

import min.SerialHandler;
import org.joou.UByte;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by mcochrane on 3/12/16.
 */
public class TDLCompilingTest {

    private void assertCompiledListsEqual(List<UByte> actual, List<UByte> expected) {
        Assert.assertEquals(actual.size(), expected.size());
        for (int i = 0; i < actual.size(); i++) {
            Assert.assertEquals(actual.get(i), expected.get(i));
        }
    }

    @Test
    public void testCompileAction() throws Exception {
        Action a = new Action(ActivatorState.ENABLED, new ChannelActivator("1", ActivatorState.DISABLED));
        List<UByte> compiled = a.compile();

        List<UByte> expected = new ArrayList<>();
        expected.add(UByte.valueOf(Action.ACTION_TARGET_CHANNEL));
        expected.addAll(SerialHandler.min_encode_16((short)0));
        expected.add(UByte.valueOf(0));

        assertCompiledListsEqual(compiled, expected);
    }
}
