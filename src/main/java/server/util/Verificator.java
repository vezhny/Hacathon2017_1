package server.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import server.exceptions.InvalidTimestampException;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;

public class Verificator {
    private static Logger logger = LoggerFactory.getLogger(Verificator.class);

    public static boolean isTimestampValid(String timestamp, String datePattern) throws InvalidTimestampException {
        DateFormat format = new SimpleDateFormat(datePattern);
        try {
            format.parse(timestamp);
            logger.info("Timestamp is valid");
            return true;
        } catch (ParseException e) {
            logger.info("Timestamp is invalid");
            throw new InvalidTimestampException(timestamp + " is invalid");
        }
    }
}
