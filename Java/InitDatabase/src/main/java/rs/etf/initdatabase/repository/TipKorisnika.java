package rs.etf.initdatabase.repository;

public enum TipKorisnika {
    STUDENT, PROFESOR;

    public static TipKorisnika tip(int type) {
        switch (type) {
            case 0:
                return STUDENT;
            case 1:
                return PROFESOR;
            default:
                return null;
        }
    }
}
