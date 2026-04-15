package core.semantics;

import org.junit.Test;
import pt.up.fe.comp.test.env.JmmTestEnv;

public class ConditionSemanticTest extends JmmTestEnv {
    private static final String BASE_PATH = "core/semantics/conditions/";
    private static final String RESOURCES_LOCATION = "test";

    public ConditionSemanticTest() {
        super(BASE_PATH, RESOURCES_LOCATION);
    }

    @Test
    public void ifConditionMustBeBoolean() {
        setDescription("If condition must have boolean type");
        semantics("IfConditionBooleanFail.jmm", true);
        semantics("IfConditionBooleanOk.jmm", false);
    }

    @Test
    public void whileConditionMustBeBoolean() {
        setDescription("While condition must have boolean type");
        semantics("WhileConditionBooleanFail.jmm", true);
        semantics("WhileConditionBooleanOk.jmm", false);
    }

    @Test
    public void forConditionMustBeBoolean() {
        setDescription("For condition must have boolean type");
        semantics("ForConditionBooleanFail.jmm", true);
        semantics("ForConditionBooleanOk.jmm", false);
    }

    @Test
    public void doWhileConditionMustBeBoolean() {
        setDescription("Do-while condition must have boolean type");
        semantics("DoWhileConditionBooleanFail.jmm", true);
        semantics("DoWhileConditionBooleanOk.jmm", false);
    }
}
