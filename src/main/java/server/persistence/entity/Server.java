package server.persistence.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import static server.constants.Server.HTTP;

@Entity
@Table(name = "servers")
public class Server {
    @Id
    private int id;

    @Column(name = "ip")
    private String ip;

    @Column(name = "port")
    private String port;

    @Column(name = "url")
    private String url;

    public Server() {}

    public Server(String ip, String port) {
        this.ip = ip;
        this.port = port;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public String getPort() {
        return port;
    }

    public void setPort(String port) {
        this.port = port;
    }

    public String getUrl(String context) {
        return HTTP + ip + ":" + port + "/" + url + context;
    }
}
