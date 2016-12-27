package TimerDescriptionLanguage;

import org.joou.UByte;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by mcochrane on 18/11/16.
 */
public class Rules {

    public class RuleAlreadyExists extends Exception {};

    private Map<String, Rule> rules = new HashMap<>();

    private static Rules ourInstance = new Rules();

    public static Rules getInstance() {
        return ourInstance;
    }

    public static void resetInstance() { ourInstance = new Rules(); }

    private Rules() {
    }

    void add(String name, Rule rule) throws RuleAlreadyExists {
        name = name.toLowerCase();
        if (rules.get(name) != null) throw new RuleAlreadyExists();
        rules.put(name, rule);
    }

    public boolean exists(String name) {
        return (get(name) != null);
    }

    public Rule get(String name) {
        name = name.toLowerCase();
        return rules.get(name);
    }

    public Rule get(int index) {
        for (Rule r : rules.values()) {
            if (r.getId() == index) return r;
        }
        return null;
    }

    public int count() {
        return rules.size();
    }

    public String getUniqueName() {
        int ruleNumber = count()+1;
        String name = makeName(ruleNumber);
        while (exists(name)) makeName(++ruleNumber);
        return name;
    }

    public List<List<UByte>> getAllCompiled() {
        List<List<UByte>> retLst = new ArrayList<>();
        for (Rule r : rules.values()) {
            retLst.add(r.compile());
        }
        return retLst;
    }

    private String makeName(int number) {
        return "Rule_" + String.valueOf(number);
    }

    Map<String, Rule> getRules() {
        return rules;
    }



}
