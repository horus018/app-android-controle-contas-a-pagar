package com.example.contaspagarlucasrubira;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class EditarContaActivity extends AppCompatActivity {
    private Categoria categoriaSelecionada;
    private EditText editTextNomeCategoria;
    private EditText editTextDespesa;
    private EditText editTextValor;
    private EditText editTextVencimento;
    private Button btnSalvar;
    private Button btnOk;
    private ArrayList<Conta> despesas;
    private int tamanhoLista;
    int despesaSelecionada = -1;
    private ListView listViewDespesas;
    CustomListAdapter adapter;
    boolean isEditando = false;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.editar_conta_activity);

        despesas = new ArrayList<>();

        editTextNomeCategoria = findViewById(R.id.editTextNomeCategoria);
        editTextDespesa = findViewById(R.id.editTextDespesa);
        editTextValor = findViewById(R.id.editTextValor);
        editTextVencimento = findViewById(R.id.editTextVencimento);
        btnSalvar = findViewById(R.id.btnSalvar);
        btnOk = findViewById(R.id.btnOk);

        if (savedInstanceState != null) {
             despesas = (ArrayList<Conta>) savedInstanceState.getSerializable("LISTA_DESPESAS");
            despesaSelecionada = savedInstanceState.getInt("SELECIONADO", -1);
            editTextNomeCategoria.setText(savedInstanceState.getString("NOME_CATEGORIA", ""));
            editTextDespesa.setText(savedInstanceState.getString("NOME_DESPESA", ""));
            editTextValor.setText(savedInstanceState.getString("VALOR_DESPESA", ""));
            editTextVencimento.setText(savedInstanceState.getString("DATA_DESPESA", ""));
        }else{
            adapter = new CustomListAdapter();
            ArrayList<Conta> despesasCategoria = (ArrayList<Conta>) getIntent().getSerializableExtra("despesasCategoria");
            adapter.updateData(despesasCategoria);
        }
        adapter = new CustomListAdapter();

        listViewDespesas = findViewById(R.id.listViewDespesas);
        listViewDespesas.setAdapter(adapter);

        categoriaSelecionada = (Categoria) getIntent().getSerializableExtra("categoriaSelecionada");
        if (categoriaSelecionada == null) {
            Toast.makeText(this, R.string.invalid_selected_category, Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        editTextNomeCategoria.setText(categoriaSelecionada.getDescricao());

        tamanhoLista = categoriaSelecionada.getContas().size();

        btnSalvar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String novoNomeCategoria = editTextNomeCategoria.getText().toString().trim();
                String descricao = editTextDespesa.getText().toString().trim();
                String valorStr = editTextValor.getText().toString().trim();
                String vencimentoStr = editTextVencimento.getText().toString().trim();

                if (novoNomeCategoria.isEmpty()) {
                    Toast.makeText(EditarContaActivity.this, R.string.provide_category_name, Toast.LENGTH_SHORT).show();
                    return;
                }

                if(isEditando) {
                    if(descricao.isEmpty() || valorStr.isEmpty() || vencimentoStr.isEmpty()){
                        Toast.makeText(EditarContaActivity.this, R.string.finish_editing_expense, Toast.LENGTH_SHORT).show();
                        return;
                    }
                }

                double totalDespesas = 0;
                for (Conta despesa : categoriaSelecionada.getContas()) {
                    totalDespesas += despesa.getValor();
                }

                categoriaSelecionada.setDescricao(novoNomeCategoria);
                Intent intent = new Intent();

                int posicaoCategoria = getIntent().getIntExtra("posicaoCategoria", -1);
                intent.putExtra("posicaoCategoria", posicaoCategoria);
                intent.putExtra("categoriaAtualizada", categoriaSelecionada);
                intent.putExtra("totalDespesas", totalDespesas);
                setResult(RESULT_OK, intent);
                finish();
            }
        });

        editTextVencimento.setKeyListener(null); //Não dxar usar o teclado quando clica no campo de data

        editTextVencimento.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_UP) {

                    Calendar calendar = Calendar.getInstance();
                    int ano = calendar.get(Calendar.YEAR);
                    int mes = calendar.get(Calendar.MONTH);
                    int dia = calendar.get(Calendar.DAY_OF_MONTH);

                    DatePickerDialog datePickerDialog = new DatePickerDialog(EditarContaActivity.this,
                            new DatePickerDialog.OnDateSetListener() {
                                @Override
                                public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                                    String dataSelecionada = String.format(Locale.getDefault(), "%02d/%02d/%d", dayOfMonth, month + 1, year);
                                    editTextVencimento.setText(dataSelecionada);
                                }
                            }, ano, mes, dia);

                    datePickerDialog.show();
                }
                return false;
            }
        });

        btnOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                adicionarDespesa();
            }
        });

        listViewDespesas.setOnItemClickListener((adapterView, view, pos, id) -> {
            if (pos == despesaSelecionada) {
                despesaSelecionada = -1;
            } else {
                despesaSelecionada = pos;
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
        if (item.getItemId() == R.id.item_adicionar) {
            adicionarDespesa();
        } else if (item.getItemId() == R.id.item_editar) {
            editarDespesa();
        } else if (item.getItemId() == R.id.item_remover) {
            removerDespesa();
        }
        return super.onOptionsItemSelected(item);
    }

    private Date parseDate(String dateStr) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            return sdf.parse(dateStr);
        } catch (ParseException e) {
            e.printStackTrace();
            return null;
        }
    }

    private class CustomListAdapter extends ArrayAdapter<Conta> {

        CustomListAdapter() {
            super(EditarContaActivity.this, R.layout.custom_list_item_despesa, despesas);
        }

        public void updateData(ArrayList<Conta> despesas) {
            clear();
            addAll(despesas);
            notifyDataSetChanged();
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View itemView = convertView;
            if (itemView == null) {
                itemView = getLayoutInflater().inflate(R.layout.custom_list_item_despesa, parent, false);
            }

            Conta despesa = getItem(position);

            TextView textViewNomeDespesa = itemView.findViewById(R.id.textViewNomeDespesa);
            textViewNomeDespesa.setText(despesa.getDescricao());

            TextView textViewValorDespesa = itemView.findViewById(R.id.textViewValorDespesa);
            textViewValorDespesa.setText(getResources().getString(R.string.value_before) + " " + String.format("%.2f", despesa.getValor()));

            TextView textViewVencimentoDespesa = itemView.findViewById(R.id.textViewDataVencimento);
            SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            String formattedDate = dateFormat.format(despesa.getVencimento());
            textViewVencimentoDespesa.setText(getResources().getString(R.string.date_maturity) + " " + formattedDate);

            if (position == despesaSelecionada) {
                itemView.setBackgroundColor(Color.LTGRAY);
            } else {
                itemView.setBackgroundColor(Color.BLACK);
            }

            return itemView;
        }
    }

    public void adicionarDespesa() {
        String descricao = editTextDespesa.getText().toString().trim();
        String valorStr = editTextValor.getText().toString().trim();
        String vencimentoStr = editTextVencimento.getText().toString().trim();

        if (descricao.isEmpty() || valorStr.isEmpty() || vencimentoStr.isEmpty()) {
            Toast.makeText(EditarContaActivity.this, R.string.fill_all_fields, Toast.LENGTH_SHORT).show();
            return;
        }

        String cleanValorStr = valorStr.replaceAll("[^\\d.]", "");
        double valor = Double.parseDouble(cleanValorStr);
        Date vencimento = parseDate(vencimentoStr);

        Conta novaDespesa = new Conta(descricao, vencimento, valor, categoriaSelecionada);

        if (despesas.add(novaDespesa)) {
            categoriaSelecionada.addConta(novaDespesa);
            adapter.notifyDataSetChanged();
            tamanhoLista = categoriaSelecionada.getContas().size();
        }

        tamanhoLista = categoriaSelecionada.getContas().size();

        editTextDespesa.setText("");
        editTextValor.setText("");
        editTextVencimento.setText("");
        isEditando = false;
    }

    public void removerDespesa() {
        if (despesaSelecionada > -1) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage(R.string.confirm_remove_expense);
            builder.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    categoriaSelecionada.getContas().remove(despesaSelecionada);
                    despesas.remove(despesaSelecionada);
                    despesaSelecionada = -1;
                    adapter.notifyDataSetChanged();
                }
            });
            builder.setNegativeButton(R.string.cancel, null);
            builder.show();
        } else {
            Toast.makeText(EditarContaActivity.this, R.string.select_expense_remove, Toast.LENGTH_SHORT).show();
        }
    }

    public void editarDespesa() {
        if (despesaSelecionada > -1) {
            isEditando = true;
            editTextDespesa = findViewById(R.id.editTextDespesa);
            editTextValor = findViewById(R.id.editTextValor);
            editTextVencimento = findViewById(R.id.editTextVencimento);

            editTextDespesa.setText(despesas.get(despesaSelecionada).getDescricao());;
            editTextValor.setText(String.valueOf(despesas.get(despesaSelecionada).getValor()));

            SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            String formattedDate = dateFormat.format(despesas.get(despesaSelecionada).getVencimento());
            editTextVencimento.setText(formattedDate);

            despesas.remove(despesaSelecionada);
            categoriaSelecionada.getContas().remove(despesaSelecionada);
            despesaSelecionada = -1;
            adapter.notifyDataSetChanged();

        } else {
            Toast.makeText(EditarContaActivity.this, R.string.select_expense_edit, Toast.LENGTH_SHORT).show();
        }
    }
    @Override
    public void onSaveInstanceState(Bundle dados) {
        super.onSaveInstanceState(dados);
        editTextNomeCategoria = findViewById(R.id.editTextNomeCategoria);
        editTextDespesa = findViewById(R.id.editTextDespesa);
        editTextValor = findViewById(R.id.editTextValor);
        editTextVencimento = findViewById(R.id.editTextVencimento);

        dados.putSerializable("LISTA_DESPESAS", despesas);
        dados.putInt("SELECIONADO", despesaSelecionada);
        dados.putString("NOME_CATEGORIA",editTextNomeCategoria.getText().toString());
        dados.putString("NOME_DESPESA",editTextDespesa.getText().toString());
        dados.putString("VALOR_DESPESA",editTextValor.getText().toString());
        dados.putString("DATA_DESPESA",editTextVencimento.getText().toString());
    }

}