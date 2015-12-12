package com.eribeiro.embarcadoscoleta;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.util.TypedValue;
import android.widget.Toast;

import com.baoyz.swipemenulistview.SwipeMenu;
import com.baoyz.swipemenulistview.SwipeMenuCreator;
import com.baoyz.swipemenulistview.SwipeMenuItem;
import com.baoyz.swipemenulistview.SwipeMenuListView;
import com.eribeiro.embarcadoscoleta.customizado.ListViewDados;
import com.eribeiro.embarcadoscoleta.servidor.AudioManagerCostumizado;
import com.eribeiro.embarcadoscoleta.servidor.Dado;
import com.eribeiro.embarcadoscoleta.servidor.HttpConnection;
import com.eribeiro.embarcadoscoleta.sqlite.DadoDAO;

import java.io.File;

public class HistoricoActivity extends ActionBarActivity implements SwipeRefreshLayout.OnRefreshListener, SwipeMenuListView.OnMenuItemClickListener{

    private DadoDAO dadoDAO;
    ListViewDados listViewDados;
    private SwipeRefreshLayout swipeRefreshLayout;
    private SwipeMenuListView listView;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_historico);

        this.swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipe_refresh_layout);
        this.swipeRefreshLayout.setOnRefreshListener(this);

        /**
         * Showing Swipe Refresh animation on activity create
         * As animation won't start on onCreate, post runnable is used
         */
        this.swipeRefreshLayout.post(new Runnable() {
                                         @Override
                                         public void run() {
                                             swipeRefreshLayout.setRefreshing(true);
                                             fetchMovies();
                                         }
                                     }
        );


        this.dadoDAO = new DadoDAO(getApplicationContext());
        listView = (SwipeMenuListView) findViewById(R.id.listView);

        listViewDados = new ListViewDados(getApplicationContext(), dadoDAO.getDados());
        listView.setAdapter(listViewDados);

        inicializaSwipeMenuListView();
        listView.setOnMenuItemClickListener(this);
    }


    private void inicializaSwipeMenuListView(){
        SwipeMenuCreator creator = new SwipeMenuCreator() {

            @Override
            public void create(SwipeMenu menu) {
                // create "open" item
                SwipeMenuItem openItem = new SwipeMenuItem(
                        getApplicationContext());
                // set item background
                openItem.setBackground(new ColorDrawable(Color.rgb(0xC9, 0xC9,
                        0xCE)));
                // set item width
                openItem.setWidth(dp2px(100));
                // set item title
                openItem.setTitle("Enviar");
                // set item title fontsize
                openItem.setTitleSize(18);
                // set item title font color
                openItem.setTitleColor(Color.WHITE);
                // add to menu
                menu.addMenuItem(openItem);

                // create "delete" item
                SwipeMenuItem deleteItem = new SwipeMenuItem(
                        getApplicationContext());
                // set item background
                deleteItem.setBackground(new ColorDrawable(Color.rgb(0xF9,
                        0x3F, 0x25)));
                // set item width
                deleteItem.setWidth(dp2px(100));
                // set a icon
                deleteItem.setIcon(R.drawable.ic_delete);
                // add to menu
                menu.addMenuItem(deleteItem);
            }
        };

        // set creator
        listView.setMenuCreator(creator);
        // Left
        listView.setSwipeDirection(SwipeMenuListView.DIRECTION_LEFT);
    }

    @Override
    public boolean onMenuItemClick(int position, SwipeMenu menu, int index) {
        Dado dado = (Dado) listView.getItemAtPosition(position);

        switch (index) {
            case 0:
                AudioManagerCostumizado costumizado = new AudioManagerCostumizado(this);
                if (costumizado.sendAudioToServer(dado.getUrl())) {
                    Log.d("Dados enviados", dado.toString());
                    HttpConnection.sendJson(dado);


                    DadoDAO dadoDAO = new DadoDAO(getApplicationContext());
                    File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM) + "/Sensors_log/" + dado.getUrl());

                    if (file.isFile()) {
                        file.delete();
                    }

                    dadoDAO.delete(dado.getId());

                    listViewDados.remove(dado);
                    listViewDados.notifyDataSetChanged();
                    Toast.makeText(getApplicationContext(), "Dados da Atividade foram Enviados.", Toast.LENGTH_SHORT).show();
                }else{

                }

                //Toast.makeText(getApplicationContext(), "Enviar", Toast.LENGTH_SHORT).show();
                break;
            case 1:

                DadoDAO dadoDAO = new DadoDAO(getApplicationContext());
                File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM) + "/Sensors_log/" + dado.getUrl());

                if (file.isFile()) {
                    file.delete();
                }

                dadoDAO.delete(dado.getId());

                listViewDados.remove(dado);
                listViewDados.notifyDataSetChanged();
                Toast.makeText(getApplicationContext(), "Dados da Atividade foram deletados.", Toast.LENGTH_SHORT).show();

                break;
        }

        // false : close the menu; true : not close the menu
        return false;
    }

    /**
     * This method is called when swipe refresh is pulled down
     */
    @Override
    public void onRefresh() {
        fetchMovies();
    }

    /**
     * Fetching movies json by making http call
     */
    private void fetchMovies() {

        // showing refresh animation before making http call
        swipeRefreshLayout.setRefreshing(true);
        Toast.makeText(this, "ListView do Histórico Atualizado.", Toast.LENGTH_LONG).show();
        swipeRefreshLayout.setRefreshing(false);
    }

    private int dp2px(int dp) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp,
                getResources().getDisplayMetrics());
    }
}
