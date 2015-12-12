package com.eribeiro.embarcadoscoleta.customizado;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.eribeiro.embarcadoscoleta.R;
import com.eribeiro.embarcadoscoleta.servidor.Dado;

import java.util.List;

/**
 * Created by Erick on 05/12/2015.
 * err@icomp.ufam.edu.br
 */
public class ListViewDados extends BaseAdapter {

    LayoutInflater layoutInflater;
    Context context;
    List<Dado> lista;

    public ListViewDados(Context context, List<Dado> lista){
        this.context = context;
        this.lista = lista;
        layoutInflater = LayoutInflater.from(context);
    }

    @Override
    public int getCount() {
        return lista.size();
    }

    @Override
    public Object getItem(int position) {
        return lista.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    public boolean remove(Dado dado){
        for(int i=0; i<lista.size();i++){
            if(lista.get(i).getId() == dado.getId()){
                return (lista.remove(i)== null)? false: true;
            }
        }
        return  false;
    }
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null){
            LayoutInflater mInflater = (LayoutInflater) context.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
            convertView = mInflater.inflate(R.layout.dados_linha, null);
        }

        TextView link, data;
        link = (TextView) convertView.findViewById(R.id.dados_linha_link);
        data = (TextView) convertView.findViewById(R.id.dados_linha_data);

        link.setText(lista.get(position).getRotulo());
        data.setText(lista.get(position).getUrl());

        return convertView;
    }
}
