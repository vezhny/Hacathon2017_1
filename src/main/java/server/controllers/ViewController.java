package server.controllers;

import com.google.gson.Gson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;
import server.beans.ServerResponse;
import server.exceptions.InvalidTimestampException;
import server.json.Transaction;
import server.persistence.dao.TransactionDao;
import server.persistence.entity.Trx;
import server.util.HttpSender;
import server.util.Verificator;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Objects;

@Controller
public class ViewController {
    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private TransactionDao transactionDao;

    @Autowired
    private Gson gson;

    @RequestMapping(value = "/trx", method = RequestMethod.GET)
    public ModelAndView getTrxView() {
        ModelAndView modelAndView = new ModelAndView("trxView");
        List<Trx> trxList = transactionDao.getAllTransactions();
        modelAndView.addObject("trxList", trxList);
        return modelAndView;
    }

    @RequestMapping(value = "/newTrx", method = RequestMethod.GET)
    public ModelAndView getTrxPage() {
        ModelAndView modelAndView = new ModelAndView("trx");
        modelAndView.addObject("operation", "addTrx");
        modelAndView.addObject("errorString", "");
        return modelAndView;
    }

    @RequestMapping(value = "/addTrx", method = RequestMethod.POST)
    public ModelAndView addTrx(@RequestParam(name = "data") String data,
                               @RequestParam(name = "timestamp", required = false) String timestamp,
                               @RequestBody String requestBody) throws UnsupportedEncodingException {
        logger.info("Request body: " + requestBody);
        if (Objects.equals(timestamp, null) || timestamp.trim().equals("")) {
            timestamp = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());
        }
        Transaction transaction = new Transaction(data, timestamp);
        ModelAndView modelAndView = checkTrx(transaction, null, "addTrx");
        if (!Objects.equals(modelAndView, null)) {
            return modelAndView;
        }
        String trxJson = gson.toJson(transaction);
        logger.info("Trx json: " + trxJson);
        ServerResponse serverResponse = HttpSender.httpPost("transaction=" + trxJson + "&",
                "http://localhost:10080/record?transaction=" + URLEncoder.encode(trxJson, "UTF-8") + "&");
        if (serverResponse.getCode() != 200) {
            logger.info(serverResponse.getBody());
            modelAndView = new ModelAndView("trx");
            modelAndView.addObject("trxData", transaction.getData());
            modelAndView.addObject("timestamp", transaction.getTimestamp());
            modelAndView.addObject("errorString", "Server error");
            return modelAndView;
        }
        modelAndView = new ModelAndView("redirect:/trx");
        return modelAndView;
    }

    @RequestMapping(value = "/editTrx", method = RequestMethod.GET)
    public ModelAndView editTrx(@RequestParam(name = "id") int id) {
        Trx trx = transactionDao.findById(id);
        ModelAndView modelAndView = new ModelAndView("trx");
        modelAndView.addObject("trxId" , id);
        modelAndView.addObject("operation" , "editTrx");
        modelAndView.addObject("trxData", trx.getData());
        modelAndView.addObject("timestamp", trx.getTimestamp());
        modelAndView.addObject("errorString", "");
        return modelAndView;
    }

    @RequestMapping(value = "/editTrx", method = RequestMethod.POST)
    public ModelAndView editTrx(@RequestParam(name = "id") int id,
                                @RequestParam(name = "data") String data,
                                @RequestParam(name = "timestamp") String timestamp) {
        if (Objects.equals(timestamp, null) || timestamp.trim().equals("")) {
            timestamp = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());
        }
        ModelAndView modelAndView = checkTrx(new Transaction(data, timestamp), String.valueOf(id), "editTrx");
        if (!Objects.equals(modelAndView, null)) {
            return modelAndView;
        }
        Trx trx = transactionDao.findById(id);
        trx.setData(data);
        trx.setTimestamp(timestamp);
        transactionDao.update(trx);
        return new ModelAndView("redirect:/trx");
    }

    @RequestMapping(value = "/deleteTrx", method = RequestMethod.GET)
    public ModelAndView deleteTrx(@RequestParam(name = "id") int id) {
        transactionDao.delete(id);
        return new ModelAndView("redirect:/trx");
    }

    private ModelAndView checkTrx(Transaction transaction, String id, String operation) {
        ModelAndView modelAndView;
        if (Objects.equals(transaction.getData(), null) || transaction.getData().trim().equals("")) {
            modelAndView = new ModelAndView("trx");
            modelAndView.addObject("trxId", id);
            modelAndView.addObject("operation", operation);
            modelAndView.addObject("trxData", transaction.getData());
            modelAndView.addObject("timestamp", transaction.getTimestamp());
            modelAndView.addObject("errorString", "Trx should be not empty");
            return modelAndView;
        }
        try {
            Verificator.isTimestampValid(transaction.getTimestamp(), "yyyyMMddHHmmss");
        } catch (InvalidTimestampException e) {
            modelAndView = new ModelAndView("trx");
            modelAndView.addObject("trxId", id);
            modelAndView.addObject("operation", operation);
            modelAndView.addObject("trxData", transaction.getData());
            modelAndView.addObject("timestamp", transaction.getTimestamp());
            modelAndView.addObject("errorString", "Date&Time is invalid (pattern: yyyyMMddHHmmss)");
            return modelAndView;
        }
        return null;
    }
}
