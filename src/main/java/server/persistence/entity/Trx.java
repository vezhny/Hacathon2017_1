package server.persistence.entity;

import server.json.DataBlock;
import server.util.Tools;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

@Entity
@Table(name = "transactions")
public class Trx {
    @Id
    private int id;

    @Column(name = "version")
    private int version;

    @Column(name = "prev_block")
    private String prevBlock;

    @Column(name = "data")
    private String data;

    @Column(name = "timestamp")
    private String timestamp;

    @Column(name = "hash")
    private String hash;

    public Trx() {}

    public Trx(int version, String prevBlock, String data, String timestamp) {
        this.version = version;
        this.prevBlock = prevBlock;
        this.data = data;
        this.timestamp = timestamp;
        setHash();
    }

    public Trx(DataBlock dataBlock) {
        setVersion(dataBlock.getVersion());
        setPrevBlock(dataBlock.getPreviousBlock());
        setData(dataBlock.getData());
        setTimestamp(dataBlock.getTimestamp());
        setHash(dataBlock.getHash());
    }

    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }

    public String getPrevBlock() {
        return prevBlock;
    }

    public void setPrevBlock(String prevBlock) {
        this.prevBlock = prevBlock;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public String getHash() {
        return hash;
    }

    public void setHash() {
        hash = Tools.generateHash(version, prevBlock, data, timestamp);
    }

    private void setHash(String hash) {
        this.hash = hash;
    }

    @Override
    public String toString() {
        return version + ";" + prevBlock + ";" + data + ";" + timestamp + ";" + hash;
    }

    public int getId() {
        return id;
    }
}
