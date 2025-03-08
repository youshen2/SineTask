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

public class AddDialog extends BottomDrawerDialog {
    private long parentId;

    public static AddDialog newInstance(long parentId) {
        AddDialog dialog = new AddDialog();
        Bundle args = new Bundle();
        args.putLong("parentId", parentId);
        dialog.setArguments(args);
        return dialog;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            parentId = getArguments().getLong("parentId", 0);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_add, container, false);
        view.findViewById(R.id.root).setOnClickListener(v -> close());
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        view.findViewById(R.id.submit_btn).setOnClickListener(v -> {
            EditText contentET = view.findViewById(R.id.content);
            String content = contentET.getText().toString().trim();

            if (!content.isEmpty() && getActivity() != null) {
                ((MainActivity) getActivity()).dbHelper.addTodo(content, parentId);
                ((MainActivity) getActivity()).refreshTodoList();
                close();
            }
        });
    }
}