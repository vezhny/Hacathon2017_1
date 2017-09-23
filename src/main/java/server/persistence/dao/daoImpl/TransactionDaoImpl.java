package server.persistence.dao.daoImpl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;
import server.persistence.dao.TransactionDao;
import server.persistence.entity.Trx;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import java.util.List;

@Transactional
public class TransactionDaoImpl implements TransactionDao {
    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @PersistenceContext
    private EntityManager em;

    @Override
    public Trx getLastTransaction() {
        return em.createQuery("SELECT t FROM Trx t WHERE t.id = (SELECT MAX(t.id) FROM Trx t)", Trx.class).getSingleResult();
    }

    @Override
    public void newTransaction(Trx trx) {
        logger.info("New block: " + trx.toString());
        em.persist(trx);
    }

    @Override
    public boolean hasData() {
        int records = Integer.parseInt(String.valueOf(em.createNativeQuery("SELECT COUNT(*) FROM hacaton2017.transactions").getSingleResult()));
        logger.info("Number of records: " + records);
        if (records > 0 ) {
            return true;
        }
        return false;
    }

    @Override
    public List<Trx> getAllTransactions() {
        return em.createQuery("SELECT t FROM Trx t", Trx.class).getResultList();
    }

    @Override
    public Trx findByDataAndTimestamp(String data, String timestamp) {
        try {
            List<Trx> trxList = em.createQuery("SELECT t FROM Trx t WHERE t.data = :data AND t.timestamp = :timestamp", Trx.class).
                    setParameter("data", data).setParameter("timestamp", timestamp).getResultList();
            return trxList.get(trxList.size() - 1);
        } catch (NoResultException e) {
            return null;
        } catch (IndexOutOfBoundsException e) {
            return null;
        }
    }

    @Override
    public void clear() {
        logger.info("Clearing transactions");
        em.createNativeQuery("DELETE FROM hacaton2017.transactions").executeUpdate();
        em.flush();
        em.clear();
    }

//    @Override
//    public void insertAll(List<Trx> trxList) {
//        for (Trx trx : trxList) {
//            em.persist(trx);
//        }
//    }
}
