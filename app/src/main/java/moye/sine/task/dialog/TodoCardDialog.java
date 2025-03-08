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

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import moye.sine.task.R;
import moye.sine.task.activity.MainActivity;
import moye.sine.task.dialog.base.BottomDrawerDialog;
import moye.sine.task.listener.TouchScaleListener;
import moye.sine.task.model.TodoItem;

public class TodoCardDialog extends BottomDrawerDialog {
    private TodoItem item;
    private int position;

    public static TodoCardDialog newInstance(TodoItem item, int position) {
        TodoCardDialog dialog = new TodoCardDialog();
        dialog.item = item;
        dialog.position = position;
        return dialog;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_todo, container, false);
        view.findViewById(R.id.root).setOnClickListener(view1 -> close());
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        view.findViewById(R.id.add_btn).setOnTouchListener(new TouchScaleListener());
        view.findViewById(R.id.add_btn).setOnClickListener(v -> {
            AddDialog dialog = AddDialog.newInstance(item.getId());
            dialog.show(getParentFragmentManager(), "dialog");
            close();
        });
        view.findViewById(R.id.edit_btn).setOnTouchListener(new TouchScaleListener());
        view.findViewById(R.id.edit_btn).setOnClickListener(v -> {
            TodoEditDialog dialog = TodoEditDialog.newInstance(item, position);
            dialog.show(getParentFragmentManager(), "dialog");
            close();
        });
        view.findViewById(R.id.del_btn).setOnTouchListener(new TouchScaleListener());
        view.findViewById(R.id.del_btn).setOnClickListener(v -> {
            if (getActivity() != null) {
                int currentPosition = ((MainActivity) getActivity()).adapter.todoItems.indexOf(item);
                if (currentPosition != -1) {
                    ((MainActivity) getActivity()).removeTodo(currentPosition);
                }
                close();
            }
        });

        if (item.getParentId() != 0) view.findViewById(R.id.add_btn).setVisibility(View.GONE);
    }
}
