package server.json;

import com.google.gson.annotations.SerializedName;

public class Transaction {
    @SerializedName("data")
    private String data;

    @SerializedName("timestamp")
    private String timestamp;

    public Transaction() {

    }

    public Transaction(String data, String timestamp) {
        setData(data);
        setTimestamp(timestamp);
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
}
