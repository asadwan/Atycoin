import com.atypon.training.java.traniningproject.Wallet;
import org.junit.Before;
import org.junit.Test;

import java.security.Security;

public class UtilityTests {

    private byte[] signture;
    private Wallet wallet;
    private String str;

    @Before
    public void setUp() {
        //Setup Bouncey castle as a Security Provider
        Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());
        wallet = Wallet.getSharedInstance();
        str = "hello";
    }

    @Test
    public void testPublicKeyToStringAndStringToPublicKeyIsWorking() {
        //signture = Utility.applyECDSASignuture(wallet.privateKey, str);

        //String publicKeyString = Utility.getStringFromPublicKey(wallet.publicKey);
        //PublicKey publicKey = Utility.getPublicKeyFromString(publicKeyString);

        //assertTrue(Utility.verifyECDSASignuture(publicKey,signture,str));
    }
}
