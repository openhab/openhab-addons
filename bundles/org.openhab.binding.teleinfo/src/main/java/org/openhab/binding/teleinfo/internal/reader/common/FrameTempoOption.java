package org.openhab.binding.teleinfo.internal.reader.common;

public interface FrameTempoOption {

    public static enum CouleurDemain {
        Bleu,
        Blanc,
        Rouge
    }

    public static enum ProgrammeCircuit1 {
        A,
        B,
        C
    }

    public static enum ProgrammeCircuit2 {
        P0,
        P1,
        P2,
        P3,
        P4,
        P5,
        P6,
        P7
    }

    int getBbrhpjr();

    void setBbrhpjr(int bbrhpjr);

    int getBbrhcjr();

    void setBbrhcjr(int bbrhcjr);

    int getBbrhpjw();

    void setBbrhpjw(int bbrhpjw);

    int getBbrhcjw();

    void setBbrhcjw(int bbrhcjw);

    int getBbrhpjb();

    void setBbrhpjb(int bbrhpjb);

    int getBbrhcjb();

    void setBbrhcjb(int bbrhcjb);

    CouleurDemain getDemain();

    void setDemain(CouleurDemain couleurDemain);

    Hhphc getHhphc();

    void setHhphc(Hhphc hhphc);

    ProgrammeCircuit1 getProgrammeCircuit1();

    void setProgrammeCircuit1(ProgrammeCircuit1 programmeCircuit1);

    ProgrammeCircuit2 getProgrammeCircuit2();

    void setProgrammeCircuit2(ProgrammeCircuit2 programmeCircuit2);
}
