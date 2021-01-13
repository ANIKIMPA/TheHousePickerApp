package com.design2net.the_house.models;

import java.text.DecimalFormat;

public class Orden {
    public int id;
    public String orderNumber;
    public int cantidadProductos;
    public int productsCompleted;
    public int not_available_products;
    public String user;
    private double porcientoCompletado;
    public String nombreCliente;
    public String horaDesde;
    public String horaHasta;
    public String tipo_entrega;
    public double status;
    public String bagType;
    public String tarea;
    public int netAvailableFlag;

    public Orden(int id, String orderNumber, int cantidadProductos, int productsCompleted, int not_available_products, double porcientoCompletado, String user, String nombreCliente, String horaDesde, String horaHasta, String tipo_entrega, double status, String bagType, String tarea) {
        this.id = id;
        this.orderNumber = orderNumber;
        this.cantidadProductos = cantidadProductos;
        this.productsCompleted = productsCompleted;
        this.not_available_products = not_available_products;
        this.porcientoCompletado = porcientoCompletado;
        this.user = user;
        this.nombreCliente = nombreCliente;
        this.horaDesde = horaDesde;
        this.horaHasta = horaHasta;
        this.tipo_entrega = tipo_entrega;
        this.status = status;
        this.bagType = bagType;
        this.tarea = tarea;
    }

    public double getPorcientoCompletado() {
        DecimalFormat df = new DecimalFormat("#.#");
        return Double.valueOf(df.format(porcientoCompletado));
    }

    public void setPorcientoCompletado(double porcientoCompletado) {
        this.porcientoCompletado = porcientoCompletado;
    }
}
