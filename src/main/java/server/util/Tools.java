package server.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import server.exceptions.InvalidTimestampException;
import server.persistence.entity.Trx;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Tools {
    private static Logger logger = LoggerFactory.getLogger(Tools.class);

    public static String generateHash(String hashingString) {
        StringBuffer md5Code = new StringBuffer();
        try {
            byte[] bytes = hashingString.getBytes("UTF-8");
            MessageDigest messageDigest = MessageDigest.getInstance("MD5");
            byte[] encodedBytes = messageDigest.digest(bytes);
            for (byte aByteData : encodedBytes) {
                md5Code.append(Integer.toString((aByteData & 0xff) + 0x100, 16).substring(1));
            }
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return md5Code.toString();
    }

    public static String generateHash(Trx trx, String previousHash) {
        return generateHash(trx.getVersion(), previousHash, trx.getData(), trx.getTimestamp());
    }

    public static String generateHash(Trx trx) {
        return generateHash(trx.getVersion(), trx.getPrevBlock(), trx.getData(), trx.getTimestamp());
    }

    public static String generateHash(int version, String prevBlock, String data, String timestamp) {
        String hashingString = version + ";" + prevBlock + ";" + data + ";" + timestamp + ";";
        return generateHash(hashingString);
    }

    public static String getDateByPattern(String timestamp, String datePattern) throws InvalidTimestampException {
//        logger.info("Parsing date with pattern " + datePattern);
        DateFormat timestampFormat = new SimpleDateFormat("yyyyMMddHHmmss");
        try {
            Date timestampDate = timestampFormat.parse(timestamp);
            return new SimpleDateFormat(datePattern).format(timestampDate);
        } catch (ParseException e) {
            logger.info(e.getMessage());
            return timestamp;
        }
    }
}
