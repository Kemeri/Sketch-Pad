package com.example.sketch_pad;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import static android.content.Context.MODE_PRIVATE;
import static com.example.sketch_pad.MainActivity.Sketch_position;
import static com.example.sketch_pad.MainActivity.id_Sketch;

class SketchAdapter extends RecyclerView.Adapter<SketchAdapter.SketchHolder> {

    private List<Sketch> sketches = new ArrayList();
    //создает ViewHolder и инициализирует views для списка
    @NonNull
    @Override
    public SketchHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new SketchHolder(
                LayoutInflater.from(parent.getContext()).inflate(R.layout.sketch_item, parent, false)
        );
    }

    //связывает views с содержимым
    @Override
    public void onBindViewHolder(@NonNull SketchHolder holder, int position) {
        holder.itemView.setOnClickListener((v) -> {
            Context context = v.getContext();
            Intent intent = new Intent(context, EditAddFormActivity.class);
            id_Sketch = sketches.get(position).getId(); // запоминает id записи
            Sketch_position = position; // запоминает позицию записи
            intent.putExtra("mode", (byte) 1);
            context.startActivity(intent);
        });
        holder.itemView.setOnLongClickListener((i) -> {
            Context context = i.getContext();
            openDeleteSketchDialog(context, position);
            return true;
        });
        holder.bind(sketches.get(position));
    }

    // Вызов диалогового окна
    private void openDeleteSketchDialog(Context context, int position){
        Dialog dialog = new Dialog(context);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);   // Скрывает заголовок диалогового окна
        dialog.setContentView(R.layout.delete_sketch_dialog); // Путь к макету диалогового окна
        dialog.setCancelable(false);    // окно нельзя закрыть кнопкой назад

        Button button_yes = dialog.findViewById(R.id.btn_delete_sketch_dialog_yes);
        Button button_no = dialog.findViewById(R.id.btn_delete_sketch_dialog_no);

        button_no.setOnClickListener((v) -> dialog.dismiss());  // закрываем диалоговое окно
        button_yes.setOnClickListener((v) -> {
            SQLiteDatabase db = context.openOrCreateDatabase("sketch.db", MODE_PRIVATE, null);
            Cursor query = db.rawQuery("DELETE FROM sketches WHERE id_sketch = "
                    + sketches.get(position).getId() + ";", null);
            query.moveToNext(); // выполняет SQL-инструкцию
            query.close();  // закрывает курсор
            db.close(); // закрывает базу данных
            sketches.remove(position);
            refreshSketches(sketches);
            dialog.dismiss();
        });
        dialog.show();  // Показать диалоговое окно
    }



    @Override
    public int getItemCount() {
        return sketches.size();
    }

    //передает данные и оповещает адаптер о необходимости обновления списка
    void refreshSketches(List<Sketch> sketches){
        this.sketches = sketches;
        notifyDataSetChanged();
    }

    //внутренний класс ViewHolder описывает элементы представления списка и привязку их к RecyclerView
    class SketchHolder extends RecyclerView.ViewHolder{
        TextView sketch_data;
        TextView sketch_label;
        public SketchHolder(@NonNull View itemView) {
            super(itemView);
            sketch_data = itemView.findViewById(R.id.sketch_data);
            sketch_label = itemView.findViewById(R.id.sketch_label);
        }
        void bind(Sketch sketch){
            sketch_data.setText(sketch.getDate_time());
            sketch_label.setText(sketch.getLabel_sketch());
        }
    }
}