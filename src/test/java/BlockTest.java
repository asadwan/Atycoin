import com.atypon.training.java.traniningproject.Block;
import org.junit.Before;
import org.junit.Test;

import static junit.framework.TestCase.assertTrue;


public class BlockTest {

    Block block;

    @Before
    public void setUp() {
        //block = new Block();
    }

    @Test
    public void minedBlockHashShouldStartWithFiveZeros() {
        block.mine(5);
        assertTrue(block.getHash().startsWith("00000"));
    }
}
