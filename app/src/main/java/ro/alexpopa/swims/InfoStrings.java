package ro.alexpopa.swims;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

//contine functii care ajut la afisarea informatiilor legate de datele scanarilor, respectiv de numarul de sedinte ramase, intr-un mod elegant
//functiile sunt statice, ca sa nu fie nevoie de a crea o instanta a clasei ca sa le putem folosi
public class InfoStrings {
    //returneaza o propozitie care arata frumos cate sedinte ramase mai sunt, pentru activitatea baza de date si cea elev
    public static String creditInfo (int Credit) {
        switch (Credit) {
            case 0:
                return "Elevul nu are ședințe rămase.";
            case  1:
                return "Elevul mai are o ședință rămasă.";
            default:
                return "Elevul mai are " + Credit + " ședințe rămase.";
        }
    }
    //returneaza o propozitie care arata data ultimei scanari (data in format unix pe milisecunde), sau lipsa acesteia pentru val. 0; luna e aratata numeric
    public  static String lastTimeInfo (long LastTime) {
        if (LastTime == 0) {
            return "Elevul nu a mai fost scanat până acum.";
        }
        else {
            Date date = new Date(LastTime);
            SimpleDateFormat df = new SimpleDateFormat("d.M.yyyy, H:mm", Locale.getDefault());
            return  "Elevul a fost scanat ultima oară pe " + df.format(date) + ".";
        }
    }
    public static String timeInfo (long Time) {
        Date date = new Date(Time);
        SimpleDateFormat df = new SimpleDateFormat("d MMMM yyyy, H:mm", Locale.getDefault());
        return  df.format(date);
    }
}
