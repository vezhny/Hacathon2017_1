package server.persistence.dao.daoImpl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;
import server.persistence.dao.ServerDao;
import server.persistence.entity.Server;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.List;

@Transactional
public class ServerDaoImpl implements ServerDao {
    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @PersistenceContext
    private EntityManager em;


    @Override
    public List<Server> getServers() {
        return em.createQuery("SELECT s FROM Server s", Server.class).getResultList();
    }
}
