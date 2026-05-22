package core;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

import core.parser.ArraysParserTest;
import core.parser.CallsParserTest;
import core.parser.Enteties_OperationsParserTest;
import core.parser.StatementsParserTest;
import core.parser.DeclarationsParserTest;
import core.parser.MoreParserTest;

@RunWith(Suite.class)
@Suite.SuiteClasses({
        DeclarationsParserTest.class,
        StatementsParserTest.class,
        Enteties_OperationsParserTest.class,
        CallsParserTest.class,
        ArraysParserTest.class,
        MoreParserTest.class,
})
public class AllParserTest {
}