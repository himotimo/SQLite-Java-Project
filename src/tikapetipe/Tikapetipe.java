/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tikapetipe;

import java.sql.*;
import java.util.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 *
 * @author tipe_
 */
public class Tikapetipe {

    enum KOMENNOT {
        ALUSTA("Alusta",
                "Luo tietokanta."),
        SIJAINTI_LISAA("Sijainti",
                "Lisää uusi sijainti tietokantaan."),
        ASIAKAS_LISAA("Asiakas",
                "Lisää uusi asiakas tietokantaan"),
        PAKETTI_LISAA("Paketti",
                "Lisää uusi paketti tietokantaan."),
        TAPAHTUMA_LISAA("Tapahtuma",
                "Lisää uusi tapahtuma tietokantaan (Seurantakoodi, tapahtuman sijainti sekä kuvaus"),
        PAKETTI_TAPAHTUMAT("Paketti2",
                "Hae kaikki paketin tapahtumat seurantakoodin perusteella"),
        ASIAKAS_PAKETIT("Asiakas2",
                "Hae kaikki asiakkaan paketit ja niihin liittyvien tapahtumien määrä"),
        SIJAINTI_TAPAHTUMAT("Sijainti2",
                "Hae annetusta sijainnista tapahtumien määrä tiettynä päivänä"),
        TESTI("Testi",
                "Suorita tietokannan tehokkuustesti");

        private String komentoTeksti;
        private String ohjeTeksti;

        KOMENNOT(String komentoTeksti, String ohjeTeksti) {
            this.komentoTeksti = komentoTeksti;
            this.ohjeTeksti = ohjeTeksti;
        }

        public String GetKomentoTeksti() {
            return this.komentoTeksti;
        }

        public String GetOhjeTeksti() {
            return this.ohjeTeksti;
        }
    }

    public static void main(String[] args) throws SQLException {
        Scanner input = new Scanner(System.in);
        Connection db = null;
        Statement s = null;
        boolean dbOlemassa = false;

        String kaskyTeksti = null;
        KOMENNOT tamaKomento = null;
        boolean jatka = true;
        while (jatka) {
            TulostaVaihtoehdot(dbOlemassa);

            kaskyTeksti = KysyKomento(input);
            tamaKomento = ValitseKomento(kaskyTeksti);
            if (tamaKomento == null) {
                System.out.println("En ymmärrä komentoasi \"" + kaskyTeksti + "\". Tarkista kirjoitusasu.");
                continue;
            }

            switch (tamaKomento) {
                case ALUSTA:
                    if (dbOlemassa) {
                        System.out.println("Tietokanta on jo olemassa. Käskyä ei voida suorittaa.");
                        break;
                    }
                    System.out.println("Tuhoa ja tee uusi tietokanta? y/n");
                    db = DriverManager.getConnection("jdbc:sqlite:C:\\Users\\tipe_\\Documents\\Sekalaisia\\netbeans\\sqlite-tools-win32-x86-3310100\\testi.db");
                    s = db.createStatement();
                    System.out.println("Yhteys luotu.");
                    if (input.nextLine().equals("y")) {
                        Alusta(db);
                        System.out.println("Tietokanta luotu.");
                    }
                    dbOlemassa = true;
                    break;

                case SIJAINTI_LISAA:
                    SijaintiLisaa(db, input);
                    break;

                case ASIAKAS_LISAA:
                    AsiakasLisaa(db, input);
                    System.out.println("");
                    break;

                case PAKETTI_LISAA:
                    PakettiLisaa(db, input);
                    System.out.println("");
                    break;

                case TAPAHTUMA_LISAA:
                    TapahtumaLisaa(db, input);
                    System.out.println("");
                    break;

                case PAKETTI_TAPAHTUMAT:
                    PakettiTapahtumat(db, input);
                    break;

                case ASIAKAS_PAKETIT:
                    AsiakasPaketit(db, input);
                    System.out.println("");
                    break;

                case SIJAINTI_TAPAHTUMAT:
                    SijaintiTapahtumatPvm(db, input);
                    System.out.println("");
                    break;

                case TESTI:
                    Testaa(db, input);
                    System.out.println("Lopetetaan ohjelma.");
                    jatka = false;
                    break;
            }

        }

    }

    private static void TulostaVaihtoehdot(boolean dbOlemassa) {
        System.out.println("Kirjoita haluamasi komento. Komennot:");
        for (KOMENNOT k : KOMENNOT.values()) {
            if (k == KOMENNOT.ALUSTA && dbOlemassa) {
                continue;
            }
            System.out.println(k.GetKomentoTeksti() + ": " + k.GetOhjeTeksti());
        }
    }

    private static String KysyKomento(Scanner input) {
        String kaskyTeksti = input.nextLine();
        return kaskyTeksti;
    }

    private static KOMENNOT ValitseKomento(String s) {
        KOMENNOT tamakomento = null;
        for (KOMENNOT k : KOMENNOT.values()) {
            if (k.GetKomentoTeksti().equals(s)) {
                tamakomento = k;
                break;
            }
        }
        return tamakomento;
    }

    private static void Alusta(Connection db) throws SQLException {
        Statement s = db.createStatement();
        try {
            s.execute("DROP TABLE Asiakkaat");
        } catch (Exception E) {
            System.out.println("asiakkaita ei poistettu");
        }

        try {
            s.execute("DROP TABLE Paketit");
        } catch (Exception E) {
            System.out.println("Paketteja ei poistettu");
        }
        try {
            s.execute("DROP TABLE Sijainnit");
        } catch (Exception E) {
            System.out.println("Sijainteja ei poistettu");
        }
        try {
            s.execute("DROP TABLE Tapahtumat");
        } catch (Exception E) {
            System.out.println("Tapahtumia ei poistettu");
        }

        s.execute("BEGIN TRANSACTION");
        s.execute("PRAGMA foreign_keys = ON");
        s.execute("CREATE TABLE Asiakkaat ("
                + "id INTEGER PRIMARY KEY, "
                + "nimi TEXT UNIQUE NOT NULL"
                + ")");
        s.execute("CREATE TABLE Paketit "
                + "(id INTEGER PRIMARY KEY, "
                + "asiakasid TEXT REFERENCES Asiakkaat(id) ON DELETE SET DEFAULT, "
                + "seurantakoodi text UNIQUE NOT NULL"
                + ")");
        s.execute("CREATE TABLE Sijainnit ("
                + "id INTEGER PRIMARY KEY, "
                + "nimi TEXT UNIQUE NOT NULL"
                + ") ");
        s.execute("CREATE TABLE Tapahtumat ("
                + "id INTEGER PRIMARY KEY, "
                + "sijainti_id INTEGER REFERENCES Sijainnit(id) ON DELETE SET DEFAULT, "
                + "paketti_id INTEGER REFERENCES Paketit(id) ON DELETE SET DEFAULT, "
                + "kuvaus TEXT NOT NULL, "
                + "lisayshetki TEXT NOT NULL"
                + ") ");
        s.execute("CREATE INDEX idx_paketti_id ON Tapahtumat (paketti_id)");
        s.execute("CREATE INDEX id_asiakas_id ON Paketit (asiakasid)");
        s.execute("COMMIT");

    }

    private static void SijaintiLisaa(Connection db, Scanner input) throws SQLException {
        // Lisää uusi sijainnin tietokantaan, kun annetaan sijainnin nimi.
        Statement s = db.createStatement();
        System.out.println("Tahdot siis lisätä sijainnin.");
        System.out.println("Anna sijainnin nimi.");
        String kayttajaKomento = input.nextLine();

        // Ei hyvä ratkaisu syötteen testaukseen!!
        try {
            s.execute("BEGIN TRANSACTION");
            s.execute("INSERT INTO Sijainnit (nimi) VALUES ('" + kayttajaKomento + "')");
            s.execute("COMMIT");
            System.out.println("Sijainti lisätty!");
        } catch (Exception E) {
            System.out.println("Sijainti on jo olemassa!");
        }
        System.out.println("Sijainnit:");

        ResultSet r = s.executeQuery("SELECT * FROM Sijainnit");
        while (r.next()) {
            System.out.println("id: " + r.getInt("id") + " | "
                    + "nimi: " + r.getString("nimi") + " | ");
        }
    }

    private static void AsiakasLisaa(Connection db, Scanner input) throws SQLException {
        //Lisää uusi asiakas tietokantaan, kun annetaan asiakkaan nimi.
        Statement s = db.createStatement();
        System.out.println("Tahdot siis lisätä uuden asiakkaan.");
        System.out.println("Anna asiakkaan nimi.");
        String kayttajaKomento = input.nextLine();

        // Ei hyvä ratkaisu syötteen testaukseen!!
        try {
            s.execute("BEGIN TRANSACTION");
            s.execute("INSERT INTO Asiakkaat (nimi) VALUES ('" + kayttajaKomento + "')");
            s.execute("COMMIT");
            System.out.println("Asiakas lisätty!");
        } catch (Exception E) {
            System.out.println("Asiakas on jo olemassa!");
        }
        System.out.println("Asiakkaat:");

        ResultSet r = s.executeQuery("SELECT * FROM Asiakkaat");
        while (r.next()) {
            System.out.println("id: " + r.getInt("id") + " | "
                    + "nimi: " + r.getString("nimi") + " | ");
        }
    }

    private static void PakettiLisaa(Connection db, Scanner input) throws SQLException {
        //Lisää uusi paketti tietokantaan, 
        //kun annetaan paketin seurantakoodi ja asiakkaan nimi. 
        //Asiakkaan tulee olla valmiiksi tietokannassa.
        Statement s = db.createStatement();
        System.out.println("Tahdot siis lisätä uuden paketin.");
        System.out.println("Asiakkaan nimi: ");
        String pakettinimi = input.nextLine();

        PreparedStatement p = db.prepareStatement("SELECT id FROM Asiakkaat Where nimi =?");
        p.setString(1, pakettinimi);
        ResultSet r = p.executeQuery();
        if (r.next()) {
            System.out.println("Asiakas löytyi.");
            System.out.println(r.getInt("id"));
        } else {
            System.out.println("Asiakasta ei löytynyt.");
            return;
        }

        System.out.println("Anna paketin seurantakoodi: ");
        String seurantakoodi = input.nextLine();

// Ei hyvä ratkaisu syötteen testaukseen!!
        try {
            s.execute("BEGIN TRANSACTION");
            s.execute("INSERT INTO Paketit (asiakasid, seurantakoodi) VALUES ('" + r.getInt("id") + "', '" + seurantakoodi + "')");
            s.execute("COMMIT");
            System.out.println("Paketti lisätty!");
        } catch (Exception E) {
            System.out.println("Paketti (seurantakoodi) on jo olemassa!");
        }
        System.out.println("Paketit:");

        r = s.executeQuery("SELECT * FROM Paketit");
        while (r.next()) {
            System.out.println("id: " + r.getInt("id") + " | "
                    + "asiakasid: " + r.getInt("asiakasid") + " | "
                    + "seurantakoodi: " + r.getString("seurantakoodi"));
        }

    }

    private static void TapahtumaLisaa(Connection db, Scanner input) throws SQLException {
        //Lisää uusi tapahtuma tietokantaan, kun annetaan paketin seurantakoodi, 
        //tapahtuman sijainti sekä kuvaus. 
        //Paketin ja sijainnin tulee olla valmiiksi tietokannassa.
        Statement s = db.createStatement();
        System.out.println("Tahdot siis lisätä uuden tapahtuman.");

        System.out.println("Paketin seurantakoodi: ");
        String pakettikoodi = input.nextLine();

        PreparedStatement p = db.prepareStatement("SELECT id FROM Paketit Where seurantakoodi =?");
        p.setString(1, pakettikoodi);
        ResultSet r = p.executeQuery();
        if (r.next()) {
            System.out.println("Seurantakoodi löytyi.");
            System.out.println(r.getInt("id"));
        } else {
            System.out.println("Seurantakoodia ei löytynyt.");
            return;
        }
        int paketti_id = r.getInt("id");

        System.out.println("Sijainnin nimi: ");
        String sijaintinimi = input.nextLine();

        p = db.prepareStatement("SELECT id FROM Sijainnit Where nimi =?");
        p.setString(1, sijaintinimi);
        r = p.executeQuery();
        if (r.next()) {
            System.out.println("Sijainti löytyi.");
            System.out.println(r.getInt("id"));
        } else {
            System.out.println("Sijaintia ei löytynyt.");
            return;
        }
        int sijainti_id = r.getInt("id");

        System.out.println("Anna tapahtuman kuvaus: ");
        String kuvaus = input.nextLine();

        // ajan haku
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern(("dd/MM/yyyy"));
        LocalDateTime now = LocalDateTime.now();
        String stringnow = dtf.format(now);
// Ei hyvä ratkaisu syötteen testaukseen!!
        try {
            s.execute("BEGIN TRANSACTION");
            s.execute("INSERT INTO Tapahtumat (paketti_id, sijainti_id, kuvaus, lisayshetki) VALUES ('" + paketti_id + "', '" + sijainti_id + "', '" + kuvaus + "', '" + stringnow + "')");
            s.execute("COMMIT");
            System.out.println("Tapahtuma lisätty!");
            System.out.println("Lisäysaika: " + stringnow);
        } catch (Exception E) {
            System.out.println("Tapahtuman lisäys epäonnistui.");
        }
        System.out.println("Tapahtumat:");

        r = s.executeQuery("SELECT * FROM Tapahtumat");
        while (r.next()) {
            System.out.println("id: " + r.getInt("id") + " | "
                    + "paketti_id: " + r.getInt("paketti_id") + " | "
                    + "sijainti_id: " + r.getString("sijainti_id") + " | "
                    + "kuvaus: " + r.getString("kuvaus") + " | "
                    + "lisayshetki: " + r.getString("lisayshetki"));
        }
    }

    private static void PakettiTapahtumat(Connection db, Scanner input) throws SQLException {
        //Hae kaikki paketin tapahtumat seurantakoodin perusteella.
        Statement s = db.createStatement();
        System.out.println("Tahdot siis hakea paketin tapahtumat seurantakoodilla.");

        System.out.println("Anna paketin seurantakoodi: ");
        String pakettikoodi = input.nextLine();

        PreparedStatement p = db.prepareStatement("SELECT * FROM Tapahtumat Where paketti_id = "
                + "(SELECT id FROM Paketit Where seurantakoodi = '" + pakettikoodi + "')");

        ResultSet r = p.executeQuery();
        if (r.isBeforeFirst()) {
            System.out.println("Tapahtumat löytyivät.");
        } else {
            System.out.println("Tapahtumia ei löytynyt.");
            return;
        }

        while (r.next()) {
            System.out.println("id: " + r.getInt("id")
                    + " | sijainti_id: " + r.getInt("sijainti_id")
                    + " | paketti_id: " + r.getInt("paketti_id")
                    + " | kuvaus: " + r.getString("kuvaus")
                    + " | lisayshetki: " + r.getString("lisayshetki"));
        }

    }

    private static void AsiakasPaketit(Connection db, Scanner input) throws SQLException {
        //Hae kaikki asiakkaan paketit ja niihin liittyvien tapahtumien määrä.
        Statement s = db.createStatement();
        System.out.println("Tahdot siis hakea asiakkaan paketit ja niiden tapahtumien määrät.");

        System.out.println("Anna asiakkaan nimi: ");
        String asiakasnimi = input.nextLine();

        PreparedStatement p = db.prepareStatement("SELECT p.seurantakoodi sk, count(t.id) maara "
                + "FROM Paketit p, Tapahtumat t "
                + "WHERE p.asiakasid = ("
                + "   SELECT id "
                + "   FROM Asiakkaat "
                + "   WHERE nimi = ?) "
                + "   AND t.paketti_id = p.id "
                + "GROUP BY p.seurantakoodi");
        p.setString(1, asiakasnimi);

        ResultSet r = p.executeQuery();
        if (r.isBeforeFirst()) {
            System.out.println("Paketit löytyivät.");
        } else {
            System.out.println("Paketteja ei löytynyt.");
            return;
        }

        while (r.next()) {
            System.out.println("seurantakoodi: " + r.getString("sk")
                    + " | tapahtumia: " + r.getInt("maara"));
        }

    }

    private static void SijaintiTapahtumatPvm(Connection db, Scanner input) throws SQLException {
        //Hae annetusta sijainnista tapahtumien määrä tiettynä päivänä.
        Statement s = db.createStatement();
        System.out.println("Tahdot siis hakea tapahtumien määrän tiettynä päivänä.");

        System.out.println("Anna sijainnin nimi: ");
        String sijainti = input.nextLine();
        System.out.println("Anna päivä (dd/mm/yyyy):");
        String pvm = input.nextLine();

        PreparedStatement p = db.prepareStatement("SELECT s.nimi snimi, count(t.id) tmaara, t.lisayshetki tpvm "
                + "FROM "
                + "   (SELECT * "
                + "   FROM Sijainnit"
                + "   WHERE nimi = ?) s, "
                + "   (SELECT * "
                + "   FROM Tapahtumat "
                + "   WHERE lisayshetki = ?) t "
                + "WHERE t.sijainti_id = s.id "
                + "GROUP BY s.nimi");
        p.setString(1, sijainti);
        p.setString(2, pvm);

        ResultSet r = p.executeQuery();
        if (r.isBeforeFirst()) {
            System.out.println("Tapahtumat löytyivät.");
        } else {
            System.out.println("Tapahtumia ei löytynyt.");
            return;
        }

        while (r.next()) {
            System.out.println("Sijainti: " + r.getString("snimi")
                    + " | määrä: " + r.getInt("tmaara")
                    + " | päivämäärä: " + r.getString("tpvm"));
        }

    }

    private static void Testaa(Connection db, Scanner input) throws SQLException {
        //Suorita tietokannan tehokkuustesti (tästä lisää alempana).        
        System.out.println("Tahdot siis suorittaa tietokannan tehokkuustestin.");
        Testi1(db, input);
        Testi2(db, input);
        Testi3(db, input);
        Testi4(db, input);
        Testi5(db, input);
        Testi6(db, input);

    }

    private static void Testi1(Connection db, Scanner input) throws SQLException {
        Statement s = db.createStatement();
        System.out.println("Lisätään tuhat sijaintia tietokantaan.");
        long aika1 = System.nanoTime();
        long aika2 = 0;
        s.execute("BEGIN TRANSACTION;");
        PreparedStatement p = db.prepareStatement("INSERT INTO Sijainnit (nimi) VALUES (?)");
        for (int i = 1; i <= 1000; i++) {
            p.setString(1, "P" + i);
            p.executeUpdate();
        }
        s.execute("COMMIT");
        aika2 = System.nanoTime();
        System.out.println("Tuhat sijaintia lisätty.");
        System.out.println("Aikaa kului: " + (aika2 - aika1) / 1e9 + " sekuntia");

    }

    private static void Testi2(Connection db, Scanner input) throws SQLException {
        Statement s = db.createStatement();
        System.out.println("Lisätään tuhat asiakasta tietokantaan.");
        long aika1 = System.nanoTime();
        long aika2 = 0;
        s.execute("BEGIN TRANSACTION;");
        PreparedStatement p = db.prepareStatement("INSERT INTO Asiakkaat (nimi) VALUES (?)");
        for (int i = 1; i <= 1000; i++) {
            p.setString(1, "A" + i);
            p.executeUpdate();
        }
        s.execute("COMMIT");
        aika2 = System.nanoTime();
        System.out.println("Tuhat asiakasta lisätty.");
        System.out.println("Aikaa kului: " + (aika2 - aika1) / 1e9 + " sekuntia");

    }

    private static void Testi3(Connection db, Scanner input) throws SQLException {
        Statement s = db.createStatement();
        System.out.println("Lisätään tuhat pakettia, jokaiselle asiakas tietokantaan.");
        long aika1 = System.nanoTime();
        long aika2 = 0;
        s.execute("BEGIN TRANSACTION;");
        PreparedStatement p = db.prepareStatement("INSERT INTO Paketit (seurantakoodi, asiakasid) VALUES (?,?)");
        for (int i = 1; i <= 1000; i++) {
            p.setString(1, "KOODI" + i);
            p.setInt(2, i);
            p.executeUpdate();
        }
        s.execute("COMMIT");
        aika2 = System.nanoTime();
        System.out.println("Tuhat pakettia lisätty.");
        System.out.println("Aikaa kului: " + (aika2 - aika1) / 1e9 + " sekuntia");

    }

    private static void Testi4(Connection db, Scanner input) throws SQLException {
        Statement s = db.createStatement();
        System.out.println("Lisätään miljoona tapahtumaa, jokaiselle paketti tietokantaan.");
        long aika1 = System.nanoTime();
        long aika2 = 0;
        s.execute("BEGIN TRANSACTION;");
        PreparedStatement p = db.prepareStatement("INSERT INTO Tapahtumat (sijainti_id, paketti_id, kuvaus, lisayshetki) VALUES (?,?,?,?)");
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        String time = dtf.format(now);
        for (int i = 1; i <= 1000000; i++) {
            p.setInt(1, i % 1000);
            p.setInt(2, i % 1000);
            p.setString(3, "K" + i);
            p.setString(4, time);
            p.executeUpdate();
        }
        s.execute("COMMIT");
        aika2 = System.nanoTime();
        System.out.println("Miljoona tapahtumaa lisätty.");
        System.out.println("Aikaa kului: " + (aika2 - aika1) / 1e9 + " sekuntia");

    }

    private static void Testi5(Connection db, Scanner input) throws SQLException {
        Statement s = db.createStatement();
        System.out.println("Suoritetaan tuhat kyselyä asiakkaiden paketeista.");
        long aika1 = System.nanoTime();
        long aika2 = 0;
        s.execute("BEGIN TRANSACTION;");
        PreparedStatement p = db.prepareStatement("SELECT Count(p.id) "
                + "FROM Paketit p "
                + "WHERE p.asiakasid = ?");
        for (int i = 1; i <= 1000; i++) {
            p.setInt(1, i);
            p.execute();
        }
        s.execute("COMMIT");
        aika2 = System.nanoTime();
        System.out.println("Tuhat pakettien määrää haettu.");
        System.out.println("Aikaa kului: " + (aika2 - aika1) / 1e9 + " sekuntia");

    }

    private static void Testi6(Connection db, Scanner input) throws SQLException {
        Statement s = db.createStatement();
        System.out.println("Suoritetaan tuhat kyselyä joissa haetaan pakettien tapahtumien määrä.");
        long aika1 = System.nanoTime();
        long aika2 = 0;
        s.execute("BEGIN TRANSACTION;");
        PreparedStatement p = db.prepareStatement("SELECT Count(t.id) "
                + "FROM Tapahtumat t "
                + "WHERE t.paketti_id = ?");
        for (int i = 1; i <= 1000; i++) {
            p.setInt(1, i);
            p.execute();
        }
        s.execute("COMMIT");
        aika2 = System.nanoTime();
        System.out.println("Tuhat tapahtumien määrää paketille haettu.");
        System.out.println("Aikaa kului: " + (aika2 - aika1) / 1e9 + " sekuntia");

    }

}
