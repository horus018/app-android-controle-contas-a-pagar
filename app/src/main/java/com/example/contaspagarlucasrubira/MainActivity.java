package com.example.contaspagarlucasrubira;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private ArrayList<Categoria> categorias;
    private CustomListAdapter adapter;
    private EditText editTextCategoria;
    private static final int REQUEST_EDITAR_CATEGORIA = 1;

    int categoriaSelecionada = -1;

    private ListView listViewCategorias;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        listViewCategorias = findViewById(R.id.listViewCategorias);
        editTextCategoria = findViewById(R.id.editTextCategoria);
        Button btnAdicionar = findViewById(R.id.btnAdicionar);

        categorias = new ArrayList<>();

        if (savedInstanceState != null) {
            categorias = (ArrayList<Categoria>) savedInstanceState.getSerializable("LISTA_CATEGORIAS");
            categoriaSelecionada = savedInstanceState.getInt("SELECIONADO", -1);
            editTextCategoria.setText(savedInstanceState.getString("NOME_CATEGORIA", ""));
        }

        adapter = new CustomListAdapter();
        listViewCategorias.setAdapter(adapter);

        btnAdicionar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                adicionarCategoria();
            }
        });

        listViewCategorias.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                categoriaSelecionada = 0;
                return editarCategoria(position);
            }
        });

        listViewCategorias.setOnItemClickListener((adapterView, view, pos, id) -> {
            if (pos == categoriaSelecionada) {
                categoriaSelecionada = -1;
            } else {
                categoriaSelecionada = pos;
            }
            adapter.notifyDataSetChanged();
        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_principal, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == R.id.item_adicionar){
            adicionarCategoria();
        }else if(item.getItemId() == R.id.item_editar){
            editarCategoria(categoriaSelecionada);
        }
        else if(item.getItemId() == R.id.item_remover){
            removerCategoria();
        }
        return super.onOptionsItemSelected(item);
    }

    private class CustomListAdapter extends ArrayAdapter<Categoria> {

        CustomListAdapter() {
            super(MainActivity.this, R.layout.custom_list_item_categoria, categorias);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View itemView = convertView;
            if (itemView == null) {
                itemView = getLayoutInflater().inflate(R.layout.custom_list_item_categoria, parent, false);
            }

            Categoria categoria = categorias.get(position);

            ImageView imageViewIcon = itemView.findViewById(R.id.imageViewIcon);
            TextView textViewNome = itemView.findViewById(R.id.textViewNome);
            textViewNome.setText(categoria.getDescricao());
            TextView textViewNumeroContas = itemView.findViewById(R.id.textViewNumeroContas);
            textViewNumeroContas.setVisibility(View.VISIBLE);
            textViewNumeroContas.setText(getResources().getString(R.string.number_of_expenses) + " " + categoria.getContas().size());

            TextView textViewValorTotal = itemView.findViewById(R.id.textViewValorTotal);
            textViewValorTotal.setVisibility(View.VISIBLE);
            double valorTotal = 0;
            for (Conta conta : categoria.getContas()) {
                valorTotal += conta.getValor();
            }
            textViewValorTotal.setText(getResources().getString(R.string.total_value_before) + " " + String.format("%.2f", valorTotal));

            if (position == categoriaSelecionada) {
                itemView.setBackgroundColor( Color.LTGRAY);
            } else {
                itemView.setBackgroundColor( Color.BLACK);
            }

            return itemView;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_EDITAR_CATEGORIA && resultCode == RESULT_OK) {
            if (data != null && data.hasExtra("categoriaAtualizada") && data.hasExtra("posicaoCategoria")) {
                Categoria categoriaAtualizada = (Categoria) data.getSerializableExtra("categoriaAtualizada");
                int posicaoCategoria = data.getIntExtra("posicaoCategoria", -1);

                if (posicaoCategoria != -1) {
                    categorias.set(posicaoCategoria, categoriaAtualizada);
                    adapter.notifyDataSetChanged();

                    View itemView = listViewCategorias.getChildAt(posicaoCategoria);
                    if (itemView != null) {
                        TextView textViewNome = itemView.findViewById(R.id.textViewNome);
                        textViewNome.setText(categoriaAtualizada.getDescricao());

                        TextView textViewNumeroContas = itemView.findViewById(R.id.textViewNumeroContas);
                        textViewNumeroContas.setText(getResources().getString(R.string.number_of_expenses) + " " + categoriaAtualizada.getContas().size());

                        TextView textViewValorTotal = itemView.findViewById(R.id.textViewValorTotal);
                        double valorTotal = 0;
                        for (Conta conta : categoriaAtualizada.getContas()) {
                            valorTotal += conta.getValor();
                        }
                        textViewValorTotal.setText(getResources().getString(R.string.total_value_before) + " " + String.format("%.2f", valorTotal));
                    }
                }
            }
        }
    }

    public void adicionarCategoria() {
        String nomeCategoria = editTextCategoria.getText().toString().trim();
        if (!nomeCategoria.isEmpty()) {
            Categoria categoria = new Categoria(nomeCategoria);
            categorias.add(categoria);
            adapter.notifyDataSetChanged();
            editTextCategoria.setText("");
        } else {
            Toast.makeText(MainActivity.this, R.string.enter_category_name, Toast.LENGTH_SHORT).show();
        }
    }

    public void removerCategoria() {
        if (categoriaSelecionada > -1) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage(R.string.confirm_remove_category);
            builder.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    categorias.remove(categoriaSelecionada);
                    categoriaSelecionada = -1;
                    adapter.notifyDataSetChanged();
                }
            });
            builder.setNegativeButton(R.string.cancel, null);
            builder.show();
        } else {
            Toast.makeText(MainActivity.this, R.string.select_category_remove, Toast.LENGTH_SHORT).show();
        }
    }

    public boolean editarCategoria(int position) {
        if (categoriaSelecionada > -1) {
            categoriaSelecionada = -1;

            Categoria categoria = categorias.get(position);

            Intent intent = new Intent(MainActivity.this, EditarContaActivity.class);
            intent.putExtra("posicaoCategoria", position);
            intent.putExtra("categoriaSelecionada", categoria);
            intent.putExtra("despesasCategoria", categoria.getContas());
            startActivityForResult(intent, REQUEST_EDITAR_CATEGORIA);
        } else {
            Toast.makeText(MainActivity.this, R.string.select_category_edit, Toast.LENGTH_SHORT).show();
        }
        return true;
    }

    @Override
    public void onSaveInstanceState(Bundle dados) {
        super.onSaveInstanceState(dados);
        editTextCategoria = findViewById(R.id.editTextCategoria);
        dados.putSerializable("LISTA_CATEGORIAS", categorias);
        dados.putInt("SELECIONADO", categoriaSelecionada);
        dados.putString("NOME_CATEGORIA",editTextCategoria.getText().toString());
    }

}
