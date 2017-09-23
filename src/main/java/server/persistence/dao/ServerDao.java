package server.persistence.dao;

import server.persistence.entity.Server;

import java.util.List;

public interface ServerDao {
    public List<Server> getServers();
}
