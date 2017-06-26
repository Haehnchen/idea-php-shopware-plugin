package de.espend.idea.shopware.tests;

import junit.framework.TestCase;
import org.apache.commons.net.util.Base64;
import org.jetbrains.annotations.NotNull;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.UnsupportedEncodingException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

public class FooTest extends TestCase {

    public void testFoobar() {
        String content = "ZRbRMrX+jZlbvGj9XOWdkpcC8p7z\\/kv1Ze6s3zA2W6cYzc3j7uMp\\/MDTXbn2946O";
    }
}
