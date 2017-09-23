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
import server.json.Transaction;
import server.persistence.dao.TransactionDao;
import server.persistence.entity.Trx;
import server.util.HttpSender;

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
        String trxJson = gson.toJson(transaction);
        logger.info("Trx json: " + trxJson);
        ServerResponse serverResponse = HttpSender.httpPost("transaction=" + trxJson + "&",
                "http://localhost:10080/record?transaction=" + URLEncoder.encode(trxJson, "UTF-8") + "&");
        ModelAndView modelAndView;
        if (serverResponse.getCode() != 200) {
            logger.info(serverResponse.getBody());
            modelAndView = new ModelAndView("trx");
            modelAndView.addObject("trxData", transaction.getData());
            return modelAndView;
        }
        modelAndView = new ModelAndView("redirect:/trx");
        return modelAndView;
    }
}
