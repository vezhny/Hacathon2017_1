package server.persistence.dao;

import server.persistence.entity.Trx;

import java.util.List;

public interface TransactionDao {
    public Trx getLastTransaction();
    public void newTransaction(Trx trx);
    public boolean hasData();
    public List<Trx> getAllTransactions();
    public Trx findByDataAndTimestamp(String data, String timestamp);
    public void clear();
    public Trx findById(int id);
    public void update(Trx trx);
    public void delete(int id);
}
