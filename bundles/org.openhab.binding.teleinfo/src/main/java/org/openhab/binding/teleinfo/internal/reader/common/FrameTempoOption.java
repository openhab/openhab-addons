package org.openhab.binding.teleinfo.internal.reader.common;

public interface FrameTempoOption {

    public static enum CouleurDemain {
        Bleu,
        Blanc,
        Rouge
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

}
