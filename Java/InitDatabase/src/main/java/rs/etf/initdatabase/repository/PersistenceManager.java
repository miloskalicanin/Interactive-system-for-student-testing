/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package rs.etf.initdatabase.repository;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

/**
 *
 * @author milos
 */
public class PersistenceManager {

    private static PersistenceManager persistenceManager = null;

    public static PersistenceManager getInstance() {
        if (persistenceManager == null) {
            persistenceManager = new PersistenceManager();
        }
        return persistenceManager;
    }

    private EntityManagerFactory emFactory;

    private PersistenceManager() {

        // "jpa-example" was the value of the name attribute of the
        // persistence-unit element.
        emFactory = Persistence.createEntityManagerFactory("teststudent");

    }

    public EntityManager getEntityManager() {

        return emFactory.createEntityManager();

    }

    public void close() {

        emFactory.close();

    }

}
