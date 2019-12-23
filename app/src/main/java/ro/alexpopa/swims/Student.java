package ro.alexpopa.swims;

//aceasta clasa ofera date despre un elev: nume; codul de bare; numarul de sedinte achizitionate; data ultima scanare, exprimata in timp unix; luandu-le direct din variabile
public class Student implements Comparable<Student>{
    public String name;
    public int barcode, credit;
    public long lastScanDate; //timp unix in milisecunde (asa cum ii place lui Java), neaparat pe long ca altfel nu ajungem departe pana dam in overflow
    public Student(String Name, int Barcode, long LastScanDate, int Credit) {
        name = Name;
        barcode = Barcode;
        lastScanDate = LastScanDate;
        credit = Credit;
    }
    //aici propunem compararea numelor pentru a le ordona corespunzator
    @Override
    public int compareTo(Student o) {
        return name.compareTo(o.name);
    }
}
