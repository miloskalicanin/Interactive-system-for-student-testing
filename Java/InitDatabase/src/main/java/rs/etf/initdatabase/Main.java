/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package rs.etf.initdatabase;

import java.io.BufferedReader;
import java.io.FileReader;
import javax.persistence.EntityManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import static rs.etf.initdatabase.PBKDF2.createHash;
import rs.etf.initdatabase.repository.Korisnik;
import rs.etf.initdatabase.repository.PersistenceManager;
import rs.etf.initdatabase.repository.TipKorisnika;

/**
 *
 * @author milos
 */
public class Main {

    private static final Logger logger = LoggerFactory.getLogger(PBKDF2.class);

    private static final String FIRST_NAME_PATTERN = "^[A-Z][a-z]+$";
    private static final String LAST_NAME_PATTERN = "^[A-Z][a-z]+([ '-][A-Z][a-z]+)*$";
    private static final String EMAIL_PATTERN = "^[_A-Za-z0-9-]+@(student\\.)?etf\\.(bg\\.ac\\.)?rs$";
    //private static final String EMAIL_PATTERN = "^[_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-]+)*@[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$";
    private static final String PASSWORD_PATTERN = "^[A-Za-z0-9@#$%^&+=]{4,}$";

    /*
    
     */
    private static EntityManager entityManager = null;

    public static void main(String[] args) {
        if (args == null || args.length != 1) {
            logger.error("Invalid input arguments");
            return;
        }

        String fileName = args[0];
        if (fileName == null || fileName.isEmpty()) {
            logger.error("Invalid file name");
            return;
        }

        String fileData = null;
        try (BufferedReader br = new BufferedReader(new FileReader(fileName));) {
            StringBuilder sb = new StringBuilder();
            String line = br.readLine();

            while (line != null) {
                sb.append(line);
                sb.append(" ");
                line = br.readLine();
            }
            fileData = sb.toString();
        } catch (Exception e) {
            return;
        }

        if (fileData == null || fileData.isEmpty()) {
            logger.error("Invalid file content");
            return;
        }

        String[] data = fileData.split("\\s+");
        if (data == null || data.length == 0) {
            logger.error("Invalid file content");
            return;
        }

        int i = 0;
        String email;
        String password;
        String name;
        String lastName;
        int type;
        try {
            Korisnik korisnik;
            TipKorisnika tip;
            initDB();

            while (i < data.length) {
                name = data[i];
                i++;
                if (name == null || !name.matches(FIRST_NAME_PATTERN)) {
                    logger.error("Invalid first name format");
                    return;
                }

                if (i >= data.length) {
                    logger.error("Invalid person data");
                    return;
                }

                lastName = data[i];
                i++;
                if (lastName == null || !lastName.matches(LAST_NAME_PATTERN)) {
                    logger.error("Invalid last name format");
                    return;
                }

                while (i < data.length && data[i] != null && data[i].matches(LAST_NAME_PATTERN)) {
                    lastName += " " + data[i];
                    i++;
                }

                if (i >= data.length) {
                    logger.error("Invalid person data");
                    return;
                }

                email = data[i];
                i++;

                if (email == null || !email.matches(EMAIL_PATTERN)) {
                    logger.error("Invalid email format");
                    return;
                }

                if (i >= data.length) {
                    logger.error("Invalid person data");
                    return;
                }

                password = data[i];
                i++;
                if (password == null || !password.matches(PASSWORD_PATTERN)) {
                    logger.error("Invalid password format");
                    return;
                }
                password = PBKDF2.createHash(password);

                if (i >= data.length) {
                    logger.error("Invalid person data");
                    return;
                }

                type = Integer.parseInt(data[i]);
                i++;
                tip = TipKorisnika.tip(type);

                if (tip == null) {
                    logger.error("Invalid person type format");
                    return;
                }

                korisnik = new Korisnik();
                korisnik.setIme(name);
                korisnik.setPrezime(lastName);
                korisnik.setEmail(email);
                korisnik.setPassword(password);
                korisnik.setTip(tip.name());

                logger.info("Spreman za unos: " + korisnik);

                entityManager.getTransaction().begin();
                entityManager.persist(korisnik);
                entityManager.getTransaction().commit();

                logger.info("Unet korisnik: " + korisnik);
                //error.error(Level.INFO, "Unet korisnik: " + name + " " + lastName);
            }
        } catch (Exception e) {
            logger.error("Failed to parse person data", e);
            return;
        } finally {
            closeDB();
        }

    }

    private static void initDB() throws Exception {
        entityManager = PersistenceManager.getInstance().getEntityManager();
    }

    private static void closeDB() {
        try {
            entityManager.close();
            PersistenceManager.getInstance().close();
        } catch (Exception e) {

        }
    }
}
