package server.controllers;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;
import server.beans.ServerResponse;
import server.exceptions.BrokenChainException;
import server.exceptions.InvalidTimestampException;
import server.exceptions.ServerErrorException;
import server.json.DataBlock;
import server.json.Transaction;
import server.persistence.dao.ServerDao;
import server.persistence.dao.TransactionDao;
import server.persistence.entity.Server;
import server.persistence.entity.Trx;
import server.util.HttpSender;
import server.util.Tools;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import static server.constants.Exceptions.*;
import static server.constants.Server.*;
import static server.util.HttpSender.httpPost;
import static server.util.Tools.generateHash;
import static server.util.Verificator.isTimestampValid;

@Controller
public class ServerController {
    private Logger logger = LoggerFactory.getLogger(this.getClass());
    private static final String DATE_PATTERN = "yyyyMMddHHmmss";
    private static final String UTF_8 = "UTF-8";

    @Autowired
    private Gson gson;

    @Autowired
    private TransactionDao transactionDao;

    @Autowired
    private ServerDao serverDao;

    @RequestMapping(value = "/", method = RequestMethod.GET)
    public ModelAndView getMainPage() {
        return new ModelAndView("index");
    }

    @RequestMapping(value = "/record", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<String> addNewRecord(@RequestParam(name = "transaction", required = false) String transaction,
                                               @RequestBody String requestBody) {
        try {
            logger.info("Incoming record request: " + URLDecoder.decode(requestBody, UTF_8));
            transaction = URLDecoder.decode(transaction, UTF_8);
            logger.info("Trx: " + transaction);
            Transaction incomingTransaction = getTransactionFromJson(transaction);
            isTimestampValid(incomingTransaction.getTimestamp(), DATE_PATTERN);
            checkChain();
            Trx newBlock;
            if (transactionDao.hasData()) {
                Trx previousBlock = transactionDao.getLastTransaction();
                logger.info("Last record: " + previousBlock.toString());
                Trx sameBlock = transactionDao.findByDataAndTimestamp(incomingTransaction.getData(), incomingTransaction.getTimestamp());
                if (sameBlock == null) {
                    newBlock = new Trx(1,
                            previousBlock.getHash(), incomingTransaction.getData(), incomingTransaction.getTimestamp());
                } else {
                    logger.info("Such block is already exist in db. Version upped");
                    newBlock = new Trx(sameBlock.getVersion() + 1,
                            previousBlock.getHash(), incomingTransaction.getData(), incomingTransaction.getTimestamp());
                }
                transactionDao.newTransaction(newBlock);
            } else {
                logger.info("This is a first record.");
                newBlock = new Trx(1, "",
                        incomingTransaction.getData(), incomingTransaction.getTimestamp());
                transactionDao.newTransaction(newBlock);
            }
            sendNewTrx(new DataBlock(newBlock));
            return new ResponseEntity<String>(HttpStatus.OK);
        } catch (UnsupportedEncodingException e) {
            logger.info(UNSUPPORTED_ENCODING);
            return new ResponseEntity<String>(UNSUPPORTED_ENCODING, HttpStatus.BAD_REQUEST);
        } catch (JsonSyntaxException e) {
            logger.info(BROKEN_JSON);
            return new ResponseEntity<String>(BROKEN_JSON, HttpStatus.BAD_REQUEST);
        } catch (InvalidTimestampException e) {
            return new ResponseEntity<String>(e.toString(), HttpStatus.BAD_REQUEST);
        } catch (ServerErrorException e) {
            return new ResponseEntity<String>(e.toString(), HttpStatus.BAD_REQUEST);
        }
    }

    @RequestMapping(value = "/check", method = RequestMethod.POST)
    public ResponseEntity<String> checkTotalHash(@RequestParam(name = "totalHash") String requestTotalHash,
                                                 @RequestBody String requestBody) {
        logger.info("Check request");
        logger.info("Request body: " + requestBody);
        logger.info("Incoming total hash: " + requestTotalHash);
        String totalHash = getTotalHash(transactionDao.getAllTransactions());
        if (totalHash.equals(requestTotalHash)) {
            logger.info("Total hash match");
            return new ResponseEntity<String>(HttpStatus.OK);
        }
        logger.info("Total doesn't hash match");
        return new ResponseEntity<String>("Total hash doesn't match", HttpStatus.BAD_REQUEST);
    }

    @RequestMapping(value = "/update", method = RequestMethod.POST)
    public ResponseEntity<String> updateDataBase() {
        logger.info("Update request");
        List<Trx> trxList = transactionDao.getAllTransactions();
        String updateData = "";
        logger.info("Data blocks:");
        for (Trx trx : trxList) {
            String dataBlock = gson.toJson(new DataBlock(trx));
            logger.info(dataBlock);
            updateData += dataBlock + "\n";
        }
        return new ResponseEntity<String>(updateData, HttpStatus.OK);
    }

    @RequestMapping(value = "/trxRefresh", method = RequestMethod.POST)
    public ResponseEntity<String> trxRefresh(@RequestParam(name = "trx") String trx,
                                             @RequestBody String requestBody) {
        logger.info("Request body: " + requestBody);
        logger.info("Trx: " + trx);
        DataBlock dataBlock = gson.fromJson(trx, DataBlock.class);
        transactionDao.newTransaction(new Trx(dataBlock));
        return new ResponseEntity<String>(HttpStatus.OK);
    }

    private void sendNewTrx(DataBlock dataBlock) throws UnsupportedEncodingException, ServerErrorException {
        String serverUrl = getServer().getUrl(RECORD_CONTEXT);
        logger.info("Sending new data block to " + serverUrl);
        String dataBlockJson = gson.toJson(dataBlock);
        logger.info("Data block: " + dataBlockJson);
        ServerResponse serverResponse = httpPost("trx=" + dataBlockJson + "&",
                serverUrl + "?trx=" + URLEncoder.encode(dataBlockJson, UTF_8) + "&");
        if (serverResponse.getCode() != 200) {
            throw new ServerErrorException(SERVER_IS_UNAVAILABLE);
        }
    }

    private Transaction getTransactionFromJson(String jsonTransaction) throws JsonSyntaxException{
        return gson.fromJson(jsonTransaction, Transaction.class);
    }

    private void checkChain() throws ServerErrorException {
        List<Trx> trxList = transactionDao.getAllTransactions();
        logger.info("Checking chain sequence...");
        try {
        if (trxList.size() > 1) {
            for(int i = 1; i < trxList.size(); i++) {
                if (!trxList.get(i - 1).getHash().equals(trxList.get(i).getPrevBlock())) {
                    throw new BrokenChainException(BROKEN_CHAIN);
                }
                if (!generateHash(trxList.get(i)).equals(generateHash(trxList.get(i), trxList.get(i - 1).getHash()))) {
                    throw new BrokenChainException(BROKEN_CHAIN);
                }
                if (!generateHash(trxList.get(i)).equals(trxList.get(i).getHash())) {
                    throw new BrokenChainException(BROKEN_CHAIN);
                }
            }
        }
            sendCheckToOtherServer(getTotalHash(trxList));
        } catch (BrokenChainException e) {
            logger.info(e.toString());
            sendUpdateToOtherServer();
        }
    }

    private void sendUpdateToOtherServer() throws ServerErrorException {
        String serverUrl = getServer().getUrl(UPDATE_CONTEXT);
        logger.info("Sending update to " + serverUrl);
        ServerResponse serverResponse = httpPost("", serverUrl);
        if (serverResponse.getCode() != 200) {
            throw new ServerErrorException(SERVER_IS_UNAVAILABLE);
        } else {
            logger.info("Server response:");
            logger.info(serverResponse.getBody());
            updateDataBase(getUpdateList(getDataBlocks(serverResponse.getBody())));
        }
    }

    private void updateDataBase(List<Trx> trxList) {
        transactionDao.clear();
        for (Trx trx : trxList) {
            transactionDao.newTransaction(trx);
        }
//        transactionDao.insertAll(trxList);
    }

    private List<DataBlock> getDataBlocks(String body) {
        List<DataBlock> dataBlocks = new ArrayList<>();
        String[] blocks = body.split("\\n");
        for (String block : blocks) {
            dataBlocks.add(gson.fromJson(block, DataBlock.class));
        }
        return dataBlocks;
    }

    private List<Trx> getUpdateList(List<DataBlock> dataBlocks) {
        List<Trx> trxList = new ArrayList<>();
        for (DataBlock dataBlock : dataBlocks) {
            trxList.add(new Trx(dataBlock));
        }
        return trxList;
    }

    private void sendCheckToOtherServer(String totalHash) throws BrokenChainException, ServerErrorException {
        String serverUrl = getServer().getUrl(CHECK_CONTEXT);
        logger.info("Sending check to " + serverUrl);
        ServerResponse serverResponse = httpPost("totalHash=" + totalHash + "&",
                serverUrl + "?totalHash=" + totalHash + "&");
        if (serverResponse.getCode() == 400) {
            logger.info(BROKEN_CHAIN);
            throw new BrokenChainException(BROKEN_CHAIN);
        } else if (serverResponse.getCode() != 200) {
            logger.info("Url " + serverUrl + " is unavailable now");
            throw new ServerErrorException(SERVER_IS_UNAVAILABLE);
        }
    }

    private Server getServer() {
        return serverDao.getServers().get(0);
    }

    private String getTotalHash(List<Trx> trxList) {
        logger.info("Getting total hash...");
        String totalHashingString = "";
        for (Trx trx : trxList) {
            totalHashingString += trx.getHash() + ";";
        }
        String totalHash = generateHash(totalHashingString);
        logger.info("Total hash: " + totalHash);
        return totalHash;
    }
}
