/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Controladores;

import Entidades.Adicional;
import Entidades.Moneda;
import Entidades.Producto;
import Entidades.Venta;
import java.time.LocalDateTime;
import java.util.ArrayList;

/**
 *
 * @author Emanuel Álvarez, Brian Hortua, Andrés Mora, Thomas Rivera
 */
public class Vending {

    private GestionProducto gestion;
    private ArrayList<Producto> catalogo;
    private ArrayList<Venta> ventasRealizadas;
    private Venta ventaActual;
    private ArrayList<Moneda> dineroAcumulado;

    //¿no se ingresan monedas en esta funcion?
    public boolean crearNuevaVenta() {
        if (this.catalogo.isEmpty()) {
            return false;
        } else {
            this.ventaActual = new Venta();
            this.ventaActual.setFechaHora(LocalDateTime.now());
            this.ventasRealizadas.add(this.ventaActual);
            return true;
        }
    }

    public boolean pagoProductoVentaActual(ArrayList<Integer> monedas) {
        Moneda aux;
        for (int moneda : monedas) {
            if(this.monedasExistentes(moneda)==-1){
                return false;
            }
        }
        if(monedas.size()==this.ventaActual.getPagoMonedas().size()){
            return true;
        }
        return false;
    }
    //Funcion punto 4
    public boolean comprarProducto(String codigo, ArrayList<String> adicionales) {
        Producto productoVendido = verificarProductoAComprar(codigo);
        ArrayList<Adicional> adi = productoVendido.disponibilidadAdicionales(adicionales);
        if ((productoVendido != null) && (adi.isEmpty() == false)) {
            if (this.verificarUnidades(productoVendido) && (productoVendido.validarObsequios() != null)) {
                productoVendido.setAdicionalesProducto(adi);
                //relacionar venta acual con producto Vendido
                this.ventaActual.setProductoVendido(productoVendido);
                //realizar conexion con adicionales seleccionados
                this.ventaActual.setAdicionalesSeleccionados(adi);
                if (this.crearNuevaVenta()) {
                    return true;
                }
            }
        }
        return false;
    }

    //funcion que valida las moendas ingresadas con el total de la venta actual
    private boolean validarMonedas() {
        if (this.totalMonedasIngresadas() <= this.valorTotalProducto()) {
            return true;
        }
        return false;
    }

    //funcion que le suma las monedas que ingreso el usuario a la lista de monedas de la maquina
    private void actualizarMonedas() {
        Moneda aux;
        for (Moneda pagoMoneda : this.ventaActual.getPagoMonedas()) {
            aux = this.buscarMonedaDenominacion(pagoMoneda.getDenominacion());
            aux.setCantidad(aux.getCantidad() + pagoMoneda.getCantidad());
        }
    }

    //funcion que llama a la funcion que le resta la cantidad a los productos y a los obsequios de los mismos
    private void actualizarProductos() {
        Producto p = this.ventaActual.getProductoVendido();
        this.restarCantidadAlProducto(p);
        this.restarCantidadAlProducto(p.validarObsequios());
        //nos dimos cuenta que los adicionales no se restaban entonces tambien decidimos restarlos
        for (Adicional not : this.ventaActual.getAdicionalesSeleccionados()) {
            not.setExistencias(not.getExistencias() - 1);
        }
    }

    //esta funcion la hicimos para no reescribir codigo
    private void restarCantidadAlProducto(Producto p) {
        int cantidadProducto = p.getUnidadesDisponibles();
        p.setUnidadesDisponibles(cantidadProducto - 1);
    }

    //total de monedas de la venta actual
    private double totalMonedasIngresadas() {
        double acum = 0;
        for (Moneda not : this.ventaActual.getPagoMonedas()) {
            acum = acum + (not.getCantidad() * not.getDenominacion());
        }
        return acum;
    }

    private double totalAdicionales() {
        double acum = 0;
        for (Adicional adi : this.ventaActual.getAdicionalesSeleccionados()) {
            acum += adi.getPrecio();
        }
        return acum;
    }

    //retorna el valor del producto de la venta actual
    private double valorProducto() {
        return this.ventaActual.getProductoVendido().getPrecio();
    }

    //retorna el valor del producto mas el valor de los adicionales
    private double valorTotalProducto() {
        return this.valorProducto() + this.totalAdicionales();
    }

    //Funcion privada que verifica si hay unidades disponibles del producto
    private boolean verificarUnidades(Producto productoActual) {
        return productoActual.getUnidadesDisponibles() > 0;
    }
    //retorna la cantidad de monedas que hay en la maquina de una denominacion
    public int monedasExistentes(int denominacion) {
        Moneda aux;
        if ((denominacion == 50) || (denominacion == 100) || (denominacion == 200) || (denominacion == 500) || (denominacion == 1000)) {
            aux=this.ventaActual.buscarMonedaDenominacionVenta(denominacion);
            if (aux != null) {
                aux.setCantidad(buscarMonedaDenominacion(denominacion).getCantidad() + 1);
            } else {
                Moneda m = new Moneda(denominacion, 1);
                this.ventaActual.getPagoMonedas().add(m);
            }
            aux=this.buscarMonedaDenominacion(denominacion);
            aux.setCantidad(buscarMonedaDenominacion(denominacion).getCantidad() + 1);
            return aux.getCantidad();
        } else {
            return -1;
        }
    }

    //busca en la lista por denominacion y retorna la cantidad actual
    public Moneda buscarMonedaDenominacion(int denominacion) {
        for (Moneda moneda : dineroAcumulado) {
            if (moneda.getDenominacion() == denominacion) {
                return moneda;
            }
        }
        return null;
    }

    //Funcion que muestra los adicionales del producto
    /*public ArrayList<String> adicionalesProducto(String codigo){
        if((this.verificarProductoAComprar(codigo)==null)||(this.verificarProductoAComprar(codigo).adicionalesDisponibles().isEmpty())){
            return null;
        }
        else{
            return this.verificarProductoAComprar(codigo).adicionalesDisponibles();
        }
    }*/
    //esta funcion toca revisarla :'v
    //en esta funcion decia que era booleana pero nos parecio mas facil retornar el producto para no reescribir codigo
    private Producto verificarProductoAComprar(String codigo) {
        for (Producto producto : catalogo) {
            if (producto.getCodigo().equals(codigo)) {
                return producto;
            }
        }
        return null;
    }

    //CONSTRUCTORES
    public Vending() {
        this.catalogo = new ArrayList<>();
        this.gestion = new GestionProducto();
        this.catalogo = this.gestion.crearProductos();
        this.ventaActual = new Venta();
        this.ventasRealizadas = new ArrayList<>();
        this.dineroAcumulado=this.gestion.dineroAcumulado();
    }

    //MODIFICADORES
    public GestionProducto getGestion() {
        return gestion;
    }

    public void setGestion(GestionProducto gestion) {
        this.gestion = gestion;
    }

    public ArrayList<Producto> getCatalogo() {
        return catalogo;
    }

    public void setCatalogo(ArrayList<Producto> catalogo) {
        this.catalogo = catalogo;
    }

    public ArrayList<Venta> getVentasRealizadas() {
        return ventasRealizadas;
    }

    public void setVentasRealizadas(ArrayList<Venta> ventasRealizadas) {
        this.ventasRealizadas = ventasRealizadas;
    }

    public Venta getVentaActual() {
        return ventaActual;
    }

    public void setVentaActual(Venta ventaActual) {
        this.ventaActual = ventaActual;
    }

    public ArrayList<Moneda> getDineroAcumulado() {
        return dineroAcumulado;
    }

    public void setDineroAcumulado(ArrayList<Moneda> dineroAcumulado) {
        this.dineroAcumulado = dineroAcumulado;
    }
}
