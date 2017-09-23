package server.json;

import com.google.gson.annotations.SerializedName;
import server.persistence.entity.Trx;

public class DataBlock {
    @SerializedName("version")
    private int version;

    @SerializedName("previous_block")
    private String previousBlock;

    @SerializedName("data")
    private String data;

    @SerializedName("timestamp")
    private String timestamp;

    @SerializedName("hash")
    private String hash;

    public DataBlock() {}

    public DataBlock(int version, String previousBlock, String data, String timestamp, String hash) {
        this.version = version;
        this.previousBlock = previousBlock;
        this.data = data;
        this.timestamp = timestamp;
        this.hash = hash;
    }

    public DataBlock(Trx trx) {
        setVersion(trx.getVersion());
        setPreviousBlock(trx.getPrevBlock());
        setData(trx.getData());
        setTimestamp(trx.getTimestamp());
        setHash(trx.getHash());
    }

    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }

    public String getPreviousBlock() {
        return previousBlock;
    }

    public void setPreviousBlock(String previousBlock) {
        this.previousBlock = previousBlock;
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

    public void setHash(String hash) {
        this.hash = hash;
    }
}
