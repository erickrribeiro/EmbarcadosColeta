package com.eribeiro.embarcadoscoleta.servidor;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

/**
 * Created by eribeiro on 15/09/15.
 * email: erick.ribeiro.16@gmail.com
 */
public class Dado {
    private int id;
    private String rotulo;
    private String url;
    private Context context;
    private String autor;
    private String modeloDispositivo;
    private String fabricanteDispositivo;

    public Dado(String rotulo, String url, Context context){
        this.rotulo = rotulo;
        this.url = url;
        this.context = context;

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        this.autor = preferences.getString("editTextAutorPref", "Desconhecido");

        this.modeloDispositivo = android.os.Build.MODEL;
        this.fabricanteDispositivo = android.os.Build.MANUFACTURER;
    }

    public Dado(){
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getRotulo() {
        return rotulo;
    }

    public void setRotulo(String rotulo) {
        this.rotulo = rotulo;
    }

    public String getAutor() {
        return autor;
    }

    public void setAutor(String autor) {
        this.autor = autor;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getModeloDispositivo(){
        return modeloDispositivo;
    }

    public void setModeloDispositivo(String modeloDispositivo) {
        this.modeloDispositivo = modeloDispositivo;
    }

    public String getFabricanteDispositivo(){
        return fabricanteDispositivo;
    }

    public void setFabricanteDispositivo(String fabricanteDispositivo) {
        this.fabricanteDispositivo = fabricanteDispositivo;
    }

    @Override
    public String toString() {
        return String.format("[Rotulo: %s - Autor: %s - Url: %s - Modelo: %s - Fabricante: %s ]",
                getRotulo(), getAutor(), getUrl(), getModeloDispositivo(), getFabricanteDispositivo());
    }
}
