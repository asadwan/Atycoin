import com.atypon.training.java.atycoin.transactions_system.Wallet;
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
        //signture = utility.applyECDSASignature(wallet.privateKey, str);

        //String publicKeyString = utility.getStringFromPublicKey(wallet.publicKey);
        //PublicKey publicKey = utility.getPublicKeyFromString(publicKeyString);

        //assertTrue(utility.verifyECDSASignature(publicKey,signture,str));
    }
}
