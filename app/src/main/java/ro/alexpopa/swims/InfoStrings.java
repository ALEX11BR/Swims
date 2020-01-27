package ro.alexpopa.swims;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

//contine functii care ajut la afisarea informatiilor legate de datele scanarilor, respectiv de numarul de elevi generati si inregistrati si de sedinte ramase si de adaugat, intr-un mod elegant
//functiile sunt statice, ca sa nu fie nevoie de a crea o instanta a clasei ca sa le putem folosi
public class InfoStrings {
    public static String barcodeGenText (int Barcodes) {
        if (Barcodes == 1) {
            return "un cod de bare";
        }
        else {
            return Barcodes + " coduri de bare";
        }
    }
    //aici generam textul pentru aratarea numarului de elevi generati, plus cel de inregistrati
    public static String generatedStudentsInfo (int GeneratedStudents, int ActualStudents) {
        String reg;
        switch (GeneratedStudents) {
            case 0:
                return "Nu au fost generați elevi.";
            case 1:
                reg = (ActualStudents == 0) ? "nu" : "";
                return "A fost generat un singur elev, care " + reg + "este înregistrat.";
            default:
                switch (ActualStudents) {
                    case 0:
                        reg = "niciunul nefiind înregistrat.";
                        break;
                    case 1:
                        reg = "din care unul e înregistrat.";
                        break;
                    default:
                        reg = "din care " + ActualStudents + " sunt înregistrați.";
                }
                return "Au fost generați " + GeneratedStudents + " elevi, " + reg;
        }
    }
    public static String addCreditText (int Credit) {
        if (Credit == 1) {
            return "o ședință";
        }
        else {
            return + Credit + " ședințe";
        }
    }
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
    public static String lastTimeInfo (long LastTime) {
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
    public static String timeForDirName (Date date) {
        SimpleDateFormat df = new SimpleDateFormat("dMMMMyyyy_H:mm:ss", Locale.getDefault());
        return  df.format(date);
    }
}
