package com.eribeiro.embarcadoscoleta.sqlite;

import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.eribeiro.embarcadoscoleta.servidor.Dado;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Erick on 05/12/2015.
 * err@icomp.ufam.edu.br
 */

public class DadoDAO {
    private SQLiteDatabase bancoDeDados;

    public DadoDAO(Context context){
        this.bancoDeDados = (new BancoDeDados(context).getWritableDatabase());
    }

    public boolean insert(Dado dado) {

        String sql = String.format("INSERT INTO Dados (rotulo, url, autor, modelo, fabricante) " +
                "VALUES ('%s', '%s', '%s', '%s', '%s')", dado.getRotulo(), dado.getUrl(), dado.getAutor(), dado.getModeloDispositivo(), dado.getFabricanteDispositivo());
        try{
            bancoDeDados.execSQL(sql);
            Log.d("Insercao", "Inserido com sucesso");
            return true;

        } catch (SQLException e){
            Log.d("Insercao", e.getMessage());
            return false;
        }

    }

    public List<Dado> getDados(){
        List<Dado> lista = new ArrayList();
        Dado dado;

        String sql = "SELECT * FROM Dados";
        Cursor cursor = bancoDeDados.rawQuery(sql, null);

        while (cursor.moveToNext()){
            dado = new Dado();
            dado.setId(Integer.valueOf(cursor.getString(0)));
            dado.setRotulo(cursor.getString(1));
            dado.setUrl(cursor.getString(2));
            dado.setAutor(cursor.getString(3));
            dado.setModeloDispositivo(cursor.getString(3));
            dado.setFabricanteDispositivo(cursor.getString(4));

            lista.add(dado);
        }
        return lista;

    }
    public boolean delete(int id) {

        String sql = String.format("DELETE FROM Dados WHERE id =  %s ", id);
        try{
            bancoDeDados.execSQL(sql);
            Log.d("Deletado", "Log Deletado");
            return true;

        } catch (SQLException e){
            Log.d("Deletado", e.getMessage());
            return false;
        }

    }
}
