/*************************************************************************

 Copyright 2025 爅峫

 This file is part of KeyFinder.

 KeyFinder is free software: you can redistribute it and/or modify
 it under the terms of the GNU General Public License as published by
 the Free Software Foundation, either version 3 of the License, or
 (at your option) any later version.

 KeyFinder is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with KeyFinder.  If not, see <http://www.gnu.org/licenses/>.

 *************************************************************************/
package moye.sine.task.activity;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import moye.sine.task.R;
import moye.sine.task.adapter.TodoAdapter;
import moye.sine.task.dialog.AddDialog;
import moye.sine.task.listener.TouchScaleListener;
import moye.sine.task.model.TodoItem;
import moye.sine.task.utils.DatabaseHelper;

public class MainActivity extends AppCompatActivity {
    public RecyclerView recyclerView;
    public TodoAdapter adapter;
    public DatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        dbHelper = new DatabaseHelper(this);
        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        findViewById(R.id.menu_btn).setOnTouchListener(new TouchScaleListener());
        findViewById(R.id.menu_btn).setOnClickListener(view -> {
            Intent intent = new Intent(this, MenuActivity.class);
            startActivity(intent);
        });
        findViewById(R.id.add_btn).setOnTouchListener(new TouchScaleListener());
        findViewById(R.id.add_btn).setOnClickListener(view -> {
            AddDialog dialog = AddDialog.newInstance(0);
            dialog.show(getSupportFragmentManager(), "dialog");
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        refreshTodoList();
    }

    public void refreshTodoList() {
        List<TodoItem> todoItems = dbHelper.getAllTodos();
        if (adapter == null) {
            adapter = new TodoAdapter(this, todoItems, dbHelper);
            recyclerView.setAdapter(adapter);
        } else {
            adapter.todoItems.clear();
            adapter.todoItems.addAll(todoItems);
            adapter.notifyDataSetChanged();
        }
        adapter.sortItems();
    }

    public void removeTodo(int position) {
        TodoItem item = adapter.todoItems.get(position);

        dbHelper.deleteTodo(item.getId());

        if (item.isExpanded()) {
            int count = adapter.countVisibleChildren(item);
            adapter.todoItems.subList(position, position + count + 1).clear();
            adapter.notifyItemRangeRemoved(position, count + 1);
        } else {
            adapter.todoItems.remove(position);
            adapter.notifyItemRemoved(position);
        }

        adapter.notifyItemRangeChanged(position, adapter.getItemCount() - position);
    }
}