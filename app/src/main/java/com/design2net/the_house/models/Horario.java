package com.design2net.the_house.models;

public class Horario {

    public String horasDesdeShort;
    public String horaHastaShort;
    public String horasDesdeLong;
    public String horaHastaLong;

    public Horario(String horasDesdeShort, String horaHastaShort, String horasDesdeLong, String horaHastaLong) {
        this.horasDesdeShort = horasDesdeShort;
        this.horaHastaShort = horaHastaShort;
        this.horasDesdeLong = horasDesdeLong;
        this.horaHastaLong = horaHastaLong;
    }
}