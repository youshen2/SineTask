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
package moye.sine.task.adapter;

import android.content.Context;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.card.MaterialCardView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import moye.sine.task.R;
import moye.sine.task.activity.MainActivity;
import moye.sine.task.dialog.TodoCardDialog;
import moye.sine.task.listener.TouchScaleListener;
import moye.sine.task.model.TodoItem;
import moye.sine.task.utils.DatabaseHelper;

public class TodoAdapter extends RecyclerView.Adapter<TodoAdapter.ViewHolder> {
    public final List<TodoItem> todoItems;
    private final Context context;
    private final DatabaseHelper dbHelper;
    private final SimpleDateFormat timeFormat = new SimpleDateFormat("MM-dd HH:mm", Locale.CHINA);

    private final Drawable expandIcon;
    private final Drawable collapseIcon;

    public TodoAdapter(Context context, List<TodoItem> todoItems, DatabaseHelper dbHelper) {
        this.context = context;
        this.todoItems = todoItems;
        this.dbHelper = dbHelper;
        this.expandIcon = ContextCompat.getDrawable(context, R.drawable.icon_expand_more);
        this.collapseIcon = ContextCompat.getDrawable(context, R.drawable.icon_expand_less);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.cell_todo, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        TodoItem item = todoItems.get(position);

        holder.checkBox.setOnCheckedChangeListener(null);
        holder.checkBox.setChecked(item.isCompleted());
        holder.contentTextView.setText(item.getContent());
        holder.timeTextView.setText(timeFormat.format(item.getTime()));

        if (item.getParentId() == 0) {
            holder.expandIcon.setVisibility(View.VISIBLE);
            holder.expandIcon.setImageDrawable(item.isExpanded() ? collapseIcon : expandIcon);
            holder.space.setVisibility(View.GONE);
            holder.cardView.setOnTouchListener(null);
        } else {
            holder.expandIcon.setVisibility(View.GONE);
            holder.space.setVisibility(View.VISIBLE);
            holder.cardView.setOnTouchListener(new TouchScaleListener());
        }

        if (item.isCompleted()) {
            holder.contentTextView.setPaintFlags(holder.contentTextView.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
            holder.timeTextView.setPaintFlags(holder.timeTextView.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
            holder.contentTextView.setAlpha(0.5f);
            holder.timeTextView.setAlpha(0.5f);
        } else {
            holder.contentTextView.setPaintFlags(holder.contentTextView.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
            holder.timeTextView.setPaintFlags(holder.timeTextView.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
            holder.contentTextView.setAlpha(1f);
            holder.timeTextView.setAlpha(0.8f);
        }

        holder.checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            item.setCompleted(isChecked);
            todoItems.get(position).setCompleted(isChecked);
            dbHelper.updateTodo(item);

            if (item.getParentId() == 0 && isChecked) {
                for (TodoItem child : item.getChildren()) {
                    child.setCompleted(true);
                    dbHelper.updateTodo(child);
                }
            }

            sortItems();
        });

        holder.cardView.setOnClickListener(v -> {
            int currentPosition = holder.getAdapterPosition();
            if (currentPosition == RecyclerView.NO_POSITION) return;

            TodoItem currentItem = todoItems.get(currentPosition);
            if (currentItem.getParentId() == 0 && !currentItem.getChildren().isEmpty()) {
                toggleExpansion(currentItem, currentPosition);
            } else {
                holder.checkBox.toggle();
            }
        });

        holder.cardView.setOnLongClickListener(v -> {
            showTodoDialog(item, position);
            return false;
        });
    }

    private void toggleExpansion(TodoItem item, int parentPosition) {
        boolean shouldExpand = !item.isExpanded();
        item.setExpanded(shouldExpand);

        int currentParentPosition = todoItems.indexOf(item);
        if (currentParentPosition == -1) return;

        if (shouldExpand) {
            List<TodoItem> flatChildren = flattenChildren(item.getChildren());
            todoItems.addAll(currentParentPosition + 1, flatChildren);
            notifyItemRangeInserted(currentParentPosition + 1, flatChildren.size());
        } else {
            int count = countVisibleChildren(item);
            todoItems.subList(currentParentPosition + 1, currentParentPosition + 1 + count).clear();
            notifyItemRangeRemoved(currentParentPosition + 1, count);
        }
        notifyItemChanged(currentParentPosition);
    }

    public void sortItems() {
        List<TodoItem> sortedList = new ArrayList<>();
        List<TodoItem> completedParents = new ArrayList<>();

        for (TodoItem item : new ArrayList<>(todoItems)) {
            if (item.getParentId() == 0) {
                if (item.isCompleted() && !item.hasUncompletedChildren()) {
                    completedParents.add(item);
                } else {
                    sortedList.add(item);
                    // 添加展开的子项
                    if (item.isExpanded()) {
                        sortedList.addAll(flattenChildren(item.getChildren()));
                    }
                }
            }
        }

        for (TodoItem parent : completedParents) {
            sortedList.add(parent);
            if (parent.isExpanded()) {
                sortedList.addAll(flattenChildren(parent.getChildren()));
            }
        }

        todoItems.clear();
        todoItems.addAll(sortedList);

        ((MainActivity) context).recyclerView.post(() -> notifyDataSetChanged());
    }

    private List<TodoItem> flattenChildren(List<TodoItem> children) {
        List<TodoItem> result = new ArrayList<>();
        for (TodoItem child : children) {
            result.add(child);
            if (child.isExpanded()) {
                result.addAll(flattenChildren(child.getChildren()));
            }
        }
        return result;
    }

    public int countVisibleChildren(TodoItem parent) {
        int count = 0;
        for (TodoItem child : parent.getChildren()) {
            count++;
            if (child.isExpanded()) {
                count += countVisibleChildren(child);
            }
        }
        return count;
    }

    private void showTodoDialog(TodoItem item, int position) {
        TodoCardDialog dialog = TodoCardDialog.newInstance(item, position);
        dialog.show(((androidx.fragment.app.FragmentActivity) context).getSupportFragmentManager(), "dialog");
    }

    @Override
    public int getItemCount() {
        return todoItems.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        MaterialCardView cardView;
        CheckBox checkBox;
        TextView contentTextView;
        TextView timeTextView;
        ImageView expandIcon;
        View space;

        public ViewHolder(View itemView) {
            super(itemView);
            cardView = itemView.findViewById(R.id.cardView);
            checkBox = itemView.findViewById(R.id.checkbox);
            contentTextView = itemView.findViewById(R.id.label);
            timeTextView = itemView.findViewById(R.id.label_time);
            expandIcon = itemView.findViewById(R.id.expand_icon);
            space = itemView.findViewById(R.id.space);
        }
    }
}