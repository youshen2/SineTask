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
package moye.sine.task.dialog;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import moye.sine.task.R;
import moye.sine.task.activity.MainActivity;
import moye.sine.task.dialog.base.BottomDrawerDialog;
import moye.sine.task.model.TodoItem;

public class TodoEditDialog extends BottomDrawerDialog {
    private TodoItem editItem;
    private int editPosition;

    public static TodoEditDialog newInstance(TodoItem item, int position) {
        TodoEditDialog dialog = new TodoEditDialog();
        dialog.editItem = item;
        dialog.editPosition = position;
        return dialog;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_todo_edit, container, false);
        view.findViewById(R.id.root).setOnClickListener(v -> close());
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        EditText editText = view.findViewById(R.id.content);
        editText.setText(editItem.getContent());

        view.findViewById(R.id.submit_btn).setOnClickListener(v -> {
            String newContent = editText.getText().toString().trim();
            if (!newContent.isEmpty()) {
                // 更新数据库和列表
                if (getActivity() != null) {
                    MainActivity activity = (MainActivity) getActivity();
                    editItem.setContent(newContent);
                    activity.dbHelper.updateTodoContent(editItem);
                    activity.adapter.notifyItemChanged(editPosition);
                    close();
                }
            }
        });
    }
}