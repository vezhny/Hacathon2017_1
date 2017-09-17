package tests;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class MD5Test {
    private Logger logger = LoggerFactory.getLogger(this.getClass());
    private String someString = "This is test string";

    @Test
    public void toMD5Test() throws UnsupportedEncodingException, NoSuchAlgorithmException {
        byte[] bytes = someString.getBytes("UTF-8");
        MessageDigest messageDigest = MessageDigest.getInstance("MD5");
        byte[] encodedBytes = messageDigest.digest(bytes);
        StringBuffer sb = new StringBuffer();
        for (byte aByteData : encodedBytes) {
            sb.append(Integer.toString((aByteData & 0xff) + 0x100, 16).substring(1));
        }
        logger.info("Got MD5: " + sb.toString());
    }

}
